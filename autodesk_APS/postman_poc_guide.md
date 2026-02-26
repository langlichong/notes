# Postman POC 技术指南：使用“万用厨师”修改参数

本指南演示如何利用官方预编译好的 **UpdateParam** AppBundle（万用逻辑包），在不需要编写代码的情况下通过 Postman 修改 Inventor 模型参数。

## 0. 核心背景与引擎对比
> [!NOTE]
> **Autodesk** 是公司（就像 Microsoft）。**Inventor**、**3ds Max**、**AutoCAD** 是它的子产品。
> - **Inventor**: 专业的 3D 参数化机械设计软件。APS 的设计自动化核心就是基于它的引擎。
> - **SolidWorks**: 竞争对手达索（Dassault）的产品。由于版权原因，APS 云端无法提供其原生的运行环境。
> - **万用逻辑包 (UpdateParam)**：一个通用的 Inventor 插件。只要模型里有参数名，它就能改，无需针对每个模型重新写代码。

## 1. 准备工作

---

## 2. 步骤 0：上传自定义模型 (Data Management)

在修改参数前，您需要先将本地的 `.ipt` 文件上传到服务器。

### 0.1 创建存储桶 (Bucket)
**POST** `https://developer.api.autodesk.com/oss/v2/buckets`
**Body**: `{"bucketKey":"{{your_bucket_name}}", "policyKey":"transient"}`
*注：`bucketKey` 建议使用全小写。*

### 0.2 获取上传 URL
**GET** `https://developer.api.autodesk.com/oss/v2/buckets/{{your_bucket_name}}/objects/{{your_file_name.ipt}}/signeds3upload?useV4=true`
- 该接口会返回一个 `urls` 数组。

### 0.3 上传文件 (最重要的一步)
- **Method**: `PUT`
- **URL**: 使用上一步返回的第一个 `url` 字符串。
- **Headers**: 保持为空。
- **Body**: 选择 **binary**，然后选择您的本地 `.ipt` 文件。
- **响应**: 看到 200 OK 即为上传成功。

### 0.4 完成上传并确认 objectId
**POST** `https://developer.api.autodesk.com/oss/v2/buckets/{{your_bucket_name}}/objects/{{your_file_name.ipt}}/signeds3upload?useV4=true`
- **Body**: 使用第 0.2 步返回的所有响应 JSON 对象（主要是 `uploadKey`）。
- **响应结果**: 您会得到 `objectId`（如 `urn:adsk.objects:os.object:modely/inventor_sample.ipt`）。

---

## 3. 准备工作：获取模型的 URN
在进行任何修改前，您需要将得到的 `objectId` 转换为 Base64 编码的 **URN**。
- **操作**: 将 `objectId` 字符串进行 Base64 编码（例如使用 [base64encode.org](https://www.base64encode.org/)）。
- **结果**: 编码后的字符串即为您的 `:urn`。 (请注意：在 URL 路径中传递时请确保**不带**结尾的等号 `=`)

---

## 2. 步骤 0：参数发现阶段 (确认模型参数)

### 0.1 触发转换 (Translation)
**POST** `https://developer.api.autodesk.com/modelderivative/v2/designdata/job`
**Body**: `{"input":{"urn":"{{urn}}"},"output":{"formats":[{"type":"svf","views":["2d","3d"]}]}}`

### 0.2 获取元数据 GUID
**GET** `https://developer.api.autodesk.com/modelderivative/v2/designdata/{{urn}}/metadata`
- 记下返回的 `guid`（通常是具有 `role: "3d"` 的那个）。

### 0.3 获取参数列表
**GET** `https://developer.api.autodesk.com/modelderivative/v2/designdata/{{urn}}/metadata/{{guid}}/properties`
- 在响应中搜索 `"User Parameters"` 或 `"width"`，确认存在可修改的参数名。

---

## 3. 部署“万用工具包” (AppBundle)

1. **下载逻辑包**：[UpdateIPTParam.bundle.zip (官方预编译包)](https://github.com/Autodesk-Forge/learn.forge.designautomation/raw/master/sample%20appbundle/UpdateIPTParam.bundle.zip)
2. **获取上传 URL**: `GET https://developer.api.autodesk.com/da/us-east/v3/appbundles/UpdateParamBundle/uploadurl`
3. **上传 Zip**: 使用返回的 S3 链接，执行 **PUT** 请求，Body 选择 **Binary** 并提交下载好的 zip 文件。
4. **创建别名**: `POST https://developer.api.autodesk.com/da/us-east/v3/appbundles/UpdateParamBundle/aliases` (Body: `{"id":"dev","version":1}`)

---

## 4. 定义活动 (Activity)

**POST** `https://developer.api.autodesk.com/da/us-east/v3/activities`
**Body**:
```json
{
  "id": "UpdateParamActivity",
  "commandLine": [ "$(engine.path)\\inventorcoreconsole.exe /i \"$(args[inputPart].path)\" /al \"$(args[appBundle].path)\"" ],
  "parameters": {
    "inputPart": { "zip": false, "ondemand": false, "verb": "get", "required": true },
    "inputParams": { "zip": false, "ondemand": false, "verb": "get", "localName": "params.json" },
    "outputPart": { "zip": false, "ondemand": false, "verb": "put", "required": true, "localName": "output.ipt" }
  },
  "engine": "Autodesk.Inventor+24",
  "appbundles": [ "{{your_nickname}}.UpdateParamBundle+dev" ]
}
```

---

## 5. 执行修改 (WorkItem)

**params.json 机制**：您不需要手动准备文件。在 `inputParams` 中使用 `data:` 方案，云端会自动为您生成 `params.json`。

**POST** `https://developer.api.autodesk.com/da/us-east/v3/workitems`
**Body**:
```json
{
  "activityId": "{{your_nickname}}.UpdateParamActivity+dev",
  "arguments": {
    "inputPart": {
      "url": "https://developer.api.autodesk.com/oss/v2/buckets/modely/objects/inventor_sample.ipt",
      "headers": { "Authorization": "Bearer {{token}}" }
    },
    "inputParams": {
      "url": "data:application/json,{\"width\":\"50 in\", \"height\":\"20 in\"}" 
    },
    "outputPart": {
      "url": "https://developer.api.autodesk.com/oss/v2/buckets/modely/objects/output_modified.ipt",
      "verb": "put",
      "headers": { "Authorization": "Bearer {{token}}" }
    }
  }
}
```

---

## 6. 结果与验证
1. **监控状态**: `GET https://developer.api.autodesk.com/da/us-east/v3/workitems/:id`
2. **下载校验**: 状态为 `success` 后，从 OSS 下载 `output_modified.ipt`。
3. **SolidWorks 兼容性建议**: 若设计师使用 SW，需先利用 Inventor 的 AnyCAD 功能进行一次映射转换，将 SW 尺寸映射为 Inventor 参数后再上传。

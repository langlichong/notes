# Postman POC 技术指南：使用“万用厨师”修改参数

本指南演示如何利用官方预编译好的 **UpdateParam** AppBundle（万用逻辑包），在不需要编写代码的情况下通过 Postman 修改 Inventor 模型参数。

## 0. 核心背景与引擎对比
> [!NOTE]
> **Autodesk** 是公司（就像 Microsoft）。**Inventor**、**3ds Max**、**AutoCAD** 是它的子产品。
> - **Inventor**: 专业的 3D 参数化机械设计软件。APS 的设计自动化核心就是基于它的引擎。
> - **SolidWorks**: 竞争对手达索（Dassault）的产品。由于版权原因，APS 云端无法提供其原生的运行环境。
> - **万用逻辑包 (UpdateParam)**：一个通用的 Inventor 插件。只要模型里有参数名，它就能改，无需针对每个模型重新写代码。

## 1. 准备工作

### 1.1 获取 Access Token
**Endpoint**: `POST https://developer.api.autodesk.com/authentication/v2/authenticate`
**Headers**: `Content-Type: application/x-www-form-urlencoded`
**Body**:
- `client_id`: `{{your_client_id}}`
- `client_secret`: `{{your_client_secret}}`
- `grant_type`: `client_credentials`
- `scope`: `bucket:create bucket:read data:read data:write code:all`

### 1.2 验证可用引擎 (获取正确 Engine String)
**GET** `https://developer.api.autodesk.com/da/us-east/v3/engines`
- 在响应中找到您想使用的引擎 ID（例如 `Autodesk.Inventor+24`）。
- **注意**: 如果引擎名称不对，后续注册 AppBundle 会报错 400。

### 1.3 创建用户别名 (Nickname) - **极其重要**
这是绝大多数开发者卡住的地方。在创建 AppBundle 之前，您**必须**先为您的应用注册一个全局唯一的 Nickname。
**POST** `https://developer.api.autodesk.com/da/us-east/v3/forgeapps/me/nickname`
**Body**: `{"nickname":"您的唯一名称"}`
- **注意**: 如果您已经创建过 Nickname，再次调用会报 409 或 403。如果您不创建，后续创建出的 Activity ID 将会是一长串 Client ID，非常难以维护。

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

报 404 的原因是因为该 AppBundle **尚未注册**。您必须先通过 `POST` 注册定义，才能获得上传链接。

### 0.1 注册并获取上传参数
**POST** `https://developer.api.autodesk.com/da/us-east/v3/appbundles`
**Body**: `{"id": "UpdateParamBundle", "engine": "Autodesk.Inventor+24"}`
- **关键**: 该接口会返回 `uploadParameters`。记录其中的 `endpointURL`。

### 0.2 上传 ZIP 文件
- **Method**: `POST` (S3 上传通常用 POST)
- **URL**: 使用上一步返回的 `endpointURL`。
- **Body**: 选择 **form-data**，按照响应结果中的 `formData` 填入所有字段（如 `key`, `policy` 等），**最后**添加名为 `file` 的文件字段并选择您的 zip。
- **响应**: 204 No Content 代表成功。

### 0.3 创建别名 (Alias)
**POST** `https://developer.api.autodesk.com/da/us-east/v3/appbundles/UpdateParamBundle/aliases`
**Body**: `{"id":"dev","version":1}`

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

## 5. 执行修改 (WorkItem) - **Direct-to-S3 模式**

> [!WARNING]
> **重要更新**：如果您遇到 `403 Forbidden: Legacy endpoint is deprecated`，说明云端已强制要求使用 **Signed S3 URL** 方案，不再允许通过传统 OSS URL 进行“代理下载/上传”。

### 5.1 生成带签名的下载/上传链接 (关键)

在发起 WorkItem 之前，您需要先为输入和输出文件获取临时授权链接：

1. **获取 inputPart 的签名下载地址**：
   **GET** `https://developer.api.autodesk.com/oss/v2/buckets/modely/objects/inventor_sample.ipt/signeds3download`
   - 记录响应中的 `url`（这是一个包含令牌的长 AWS 链接）。

2. **获取 outputPart 的签名上传地址**：
   **GET** `https://developer.api.autodesk.com/oss/v2/buckets/modely/objects/output_modified.ipt/signeds3upload`
   - 记录响应中的 `urls[0]`。

### 5.2 提交 WorkItem (下达订单)

**POST** `https://developer.api.autodesk.com/da/us-east/v3/workitems`
**Body**:
```json
{
  "activityId": "{{your_nickname}}.UpdateParamActivity+dev",
  "arguments": {
    "inputPart": {
      "url": "第 5.1 步获取的长 S3 下载链接",
      "headers": {} 
    },
    "inputParams": {
      "url": "data:application/json,{\"width\":\"50 in\", \"height\":\"20 in\"}" 
    },
    "outputPart": {
      "url": "第 5.1 步获取的长 S3 上传链接",
      "verb": "put",
      "headers": {}
    }
  }
}
```
> [!IMPORTANT]
> **核心变化**：
> 1. **URL**：必须使用以 `https://...amazonaws.com/...` 开头的签名链接，而不是 `.../oss/v2/...`。
> 2. **Headers**：当使用签名链接时，`headers` **必须为空** `{}`。因为链接本身已包含身份验证，再传 Bearer Token 会导致 S3 报错。

---

---

## 7. 常见错误：Could not find *.bundle in AppPackage

如果您在 WorkItem 的报告中看到这个错误，说明您上传的 **AppBundle ZIP 压缩包结构不对**。

### 正确的 ZIP 结构要求：
APS 云端要求 ZIP 包内必须有一个以 `.bundle` 结尾的文件夹。

**错误的结构**（直接压缩了文件）：
```
UpdateIPTParam.bundle.zip
├── PackageContents.xml  <-- (直接在根目录，错误)
└── Contents/
```

**正确的结构**（多一层 .bundle 文件夹）：
```
UpdateIPTParam.bundle.zip
└── UpdateIPTParam.bundle/  <-- (必须有这一层)
    ├── PackageContents.xml
    └── Contents/
        └── UpdateIPTParam.dll
```

### 如何修复：
1. 在本地新建一个文件夹，命名为 `UpdateIPTParam.bundle`。
2. 将 `PackageContents.xml` 和 `Contents` 文件夹放入其中。
3. **右键点击 `UpdateIPTParam.bundle` 文件夹进行压缩**。
4. 在 VS Code 插件中对 `Owned App Bundles` 下的 `UpdateIPTParameter` 右键选择 **`Upload...`**（或删除后重新创建）上传这个新的 ZIP。
5. **别忘了更新 Alias**：上传新版本后，确保您的 `updp` 别名指向最新的 `Version 2`（或 3）。

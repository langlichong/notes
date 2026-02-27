跑一次 WorkItem 要花多少钱？
我们来算一笔账：

Inventor 引擎费率：6 个云点 / 每处理小时。
简单的秒级任务耗时：处理阶段大约耗时 6 秒（6/3600 小时）。
计算公式：6 秒 / 3600 秒 * 6 点 * 22 元 ≈ 0.22 元。
结论：跑一次这种参数化修改任务，成本大约只需要 2 角 2 分钱。这比雇一个工程师手动打开软件去改参数要便宜得多，而且速度是秒级的


在 APS 的计费体系中，确实是遵循**“重计算、轻存储”**的原则。

针对您列出的整套流程，费用的分布如下：

1. 免费（或暂不计费）的部分：
上传 .ipt 模型 (OSS)：目前 APS 并不根据存储文件的容量（GB）或上传流量收费。您可以自由上传、更新模型。
创建 AppBundle：这是免费的。您可以上传多个版本的代码包。
创建 Activity：这是免费的。这只是定义一个任务模板。
获取签名链接 (Signed URL)：这也是免费的 API 调用。

2. 主要收费的部分：
提交 WorkItem (核心收费点)：
这是您唯一需要重点关注的支出。
它是按实际运行时间计费的（按秒计费，如 Inventor 引擎 6 个云点/小时）。

3. 一个容易忽略的额外收费点：
Model Derivative (模型转译)：
如果您希望在网页前端（Viewer）直接看到修改后的 3D 模型，就需要对生成出的新模型调用“转译 (Translate)”接口。
费用标准：
复杂模型 (如 Revit/Navisworks)：通常是 1.5 个点/个模型。
普通模型 (您的 Inventor .ipt)：通常是 0.2 个点/个模型（折合人民币约 4-5 元）。

注意：如果您只是下载模型回本地 SolidWorks，不在线查看，那么这笔费用也是 0

<img width="987" height="693" alt="image" src="https://github.com/user-attachments/assets/fa2fc572-9b4d-4c6b-a1d0-22bc001ad08b" />
<img width="1025" height="316" alt="image" src="https://github.com/user-attachments/assets/3a201ef4-fc0f-4e29-bc9c-2c78038ece09" />


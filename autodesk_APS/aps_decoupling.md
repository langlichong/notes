# APS AppBundle 与 Activity 的解耦（黑盒问题）

你提出的观察非常深刻：**AppBundle (DLL) 与 Activity (JSON 定义) 之间存在隐含的依赖协议。**

如果你手里只有一个别人的 `AppBundle` 二进制文件，而没有源码或文档，你确实无法知道它内部是否在“偷偷”读取一个叫 `params.json` 的文件。这在软件工程中被称为“黑盒问题”。

### 1. 为什么官方示例这么写？

官方入门示例为了降低理解门槛，通常采用 **“约定优于配置” (Convention over Configuration)** 的原则。
- **优点**：简单，代码行数少。
- **缺点**：就是你发现的——**强耦合**。如果 Activity 定义时把 `localName` 改了，程序直接崩溃。

---

### 2. 如何实现真正的解耦？

为了让 `AppBundle` 更通用（即：我不管你叫 `params.json` 还是 `config.txt`，只要你告诉我路径就行），专业开发者通常采用以下方案：

#### 方案 A：通过命令行参数 (Command Line Arguments) 传递路径

在定义 `Activity` 时，你可以利用 APS 的占位符（如 `$(args[xxx].path)`）动态传递文件路径：

**Activity 的命令行定义：**
```json
"commandLine": [
  "$(engine.path)\\InventorCoreConsole.exe /al $(appbundles[UpdateProj].path) /j $(args[parameters].path)"
]
```
这里的 `/j` 就是一个通用的开关，告诉插件：*“接下来的路径就是你要读的 JSON”*。

**C# 插件内部逻辑：**
```csharp
// 在插件启动时捕获命令行参数
string[] args = System.Environment.GetCommandLineArgs();
// 寻找 "/j" 开关并提取后面的路径
string jsonPath = ParsePathFromArgs(args, "/j"); 

// 此时读取的就是 Activity 动态指定的任意路径，不再写死
var jsonContent = System.IO.File.ReadAllText(jsonPath);
```

#### 方案 B：使用标准环境变量 (Environment Variables)

APS 允许在 `Activity` 中设置环境变量。你可以约定一个变量名（如 `SETTINGS_FILE`），让 `AppBundle` 去读这个变量。

---

### 3. 如何解决“不知道”的问题？

目前 APS 平台本身**并没有**像 Swagger (OpenAPI) 那样的自动发现机制来列出 AppBundle 到底需要哪些输入。

**解决办法建议：**
1. **AppBundle + Activity 捆绑发布**：通常一个功能模块会同时提供 DLL 和与其对应的 Activity 定义文件。
2. **README 约定**：在发布 AppBundle 时，务必提供一个 MD 文件说明：
   - *“本插件要求一个输入参数，ID 为 `jsonParams`，内部读取的 localName 必须为 `params.json`。”*
3. **内置帮助开关**：在命令行运行 `Console.exe /al my.dll /help` 时，让插件在日志里打印出它对输入参数的需求。

### 总结
你看到的 demo 代码是一个**简易实现**。在生产环境下，**必须通过 CommandLine 动态传递路径**，而不是在代码里写死文件名。这样，Activity 的创建者就可以自由决定文件的名字，而 AppBundle 只负责“接收命令”。

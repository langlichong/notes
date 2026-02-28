# APS 参数传递：命令行 vs 数据文件

除了使用 `params.json` 这种“文件挂载”方式，APS 确实支持更多样、更直接的参数传递手段。

### 1. 直接通过命令行参数 (Command-Line Arguments)

如果你的参数非常少（比如仅一个“宽度”或“高度”），完全没必要生成 JSON 文件。

**Activity 定义：**
```json
"commandLine": [
  "$(engine.path)\\InventorCoreConsole.exe /al $(appbundles[MyBundle].path) /width $(args[w].value) /height $(args[h].value)"
]
```
这里的 `$(args[w].value)` 会直接提取请求中的数值。

**WorkItem 请求：**
```json
"arguments": {
  "w": "1500",
  "h": "800"
}
```

- **优点**：极快，无需文件读写，AppBundle 层面完全解耦。
- **缺点**：不适合传递复杂对象、数组或超过 10 个以上的参数。命令行长度在 Windows 上是有上限的。

---

### 2. 使用环境变量 (Environment Variables)

你可以在 Activity 中将某个输入参数映射为环境变量。

**Activity 定义：**
```json
"settings": {
  "env": {
    "PART_WIDTH": "$(args[width].value)"
  }
}
```

**C# 插件代码：**
```csharp
string width = System.Environment.GetEnvironmentVariable("PART_WIDTH");
```

- **优点**：非常干净，代码中不需要解析复杂的命令行字符串。
- **缺点**：也是仅限于简单的 Key-Value 对。

---

### 3. 直接传递文本 (String Payload)

即使在 `arguments` 层面，APS 允许你直接发送字符串内容（即使你命名它为文件）。

**WorkItem 技巧：**
```json
"arguments": {
  "jsonParams": {
    "url": "data:application/json,{\"width\":1500,\"height\":800}" // 👈 直接传内容，不传 URL
  }
}
```
APS 引擎会自动把这串 Base64 或纯文本转成物理文件。这介于“命令行”和“云端文件”之间，适合参数不多不少的情况。

---

### 4. 最佳实践建议：组合使用 (Hybrid)

在真实的生产项目中，我们推荐**混合模式**：

| 参数类型 | 推荐方式 | 理由 |
| :--- | :--- | :--- |
| **元数据/控制开关** | 命令行参数 (`/debug`, `/mode:fast`) | 易于在日志中排查问题。 |
| **基础单体数值** | 环境变量 | 性能最高，读取最简单。 |
| **复杂工程数据** | JSON 文件 (`params.json`) | 支持嵌套结构，易于序列化/反序列化。 |

### 总结
你完全可以不使用 `params.json`。如果你希望 AppBundle 对外展示更清晰的“接口”，**使用命令行参数 (`CommandLine`) 是最标准的方式**。

这样，AppBundle 就像一个普通的 EXE 程序：
`InventorCoreConsole.exe /width 100 /color Red`

使用者只需要看一眼命令行定义（Command Line），就知道该传哪些参数，而不需要去猜 JSON 的内部结构。

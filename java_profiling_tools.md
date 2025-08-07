| 技术 / API                                       | 类比 Python 的哪个功能             | 是否推荐      | 说明                   |
| ---------------------------------------------- | --------------------------- | --------- | -------------------- |
| **Java Agent + Instrumentation API**           | `sys.monitoring` + 字节码 hook | ✅ 强烈推荐    | 支持方法级、类加载时字节码插桩      |
| **JVMTI（Java Virtual Machine Tool Interface）** | C/C++级运行时 hook              | 🟡 高级别    | 用于 native agent，难度较高 |
| **JFR（Java Flight Recorder）**                  | `sys.monitoring` + 性能分析     | ✅ 非侵入式    | 官方性能监控利器             |
| **JMX（Java Management Extensions）**            | 模块级状态监控                     | ✅ 标准方案    | 用于运行时状态暴露与管理         |
| **Debug Agent (JDWP)**                         | Python `sys.settrace`       | 🟡 调试用途为主 | 用于远程调试器              |
| **AspectJ / ByteBuddy / ASM**                  | `sys.settrace` + 插桩         | ✅ 高可控性    | 方法调用/返回 hook 支持      |



| 技术                             | 用途         | 特点                         |
| ------------------------------ | ---------- | -------------------------- |
| **JFR (Java Flight Recorder)** | 性能分析       | 无需修改代码，记录事件，低开销            |
| **JMX**                        | 运行状态暴露     | 常与 Spring Boot Actuator 联用 |
| **JVMTI**                      | Native 层监控 | C/C++ 编写，监控 GC、线程、类加载等     |
| **JDWP (调试协议)**                | 远程调试       | IDE 调试器底层使用协议              |
| **ASM / BCEL**                 | 字节码操作      | 可手动修改类结构，非常底层              |



- 使用 ByteBuddy 快速实现 hook

<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/7488e602-665f-4483-b7e7-a695f481d694" />

```text
自定义的MBean  →  注册到 PlatformMBeanServer
PlatformMBeanServer ← ManagementFactory.getPlatformMBeanServer() 获取
PlatformMBeanServer + JMX Connector (jmxremote) → 远程暴露监控和管理
类比：

MBeanServer = “管理信息登记处”

PlatformMBeanServer = “已经自带系统信息的官方登记处”

jmxremote = “把登记处通过网络广播出去的扩音器”
```

默认情况下，ManagementFactory.getPlatformMBeanServer() 创建/获取的 平台 MBeanServer 只会在 本地 JVM 内部 暴露，也就是说：

同一个 JVM 里的代码（包括你自己的应用代码、Java Agent、附加到 JVM 的监控工具等）可以直接通过 MBeanServer 或 ManagementFactory 访问监控数据。

不会自动通过网络暴露，外部机器是不能直接访问的

默认本地访问的原因
Java 的设计是 本地 MBeanServer 默认是安全隔离的：

没有启动 com.sun.management.jmxremote 相关的 JVM 参数 → 不会启动 JMX 远程连接器。

没有暴露 JMXConnectorServer → 外部没有入口。

这样本地工具（比如 jconsole、jvisualvm、mission control）可以通过 Attach API 直接连接 JVM 并访问 MBeans，但这实际上是通过本地进程间通信（IPC），不是 TCP 网络。


### JMX 在 Java 里确实有两条主要“接入口”——JMXConnector 和 Attach API，它们能访问到同一套 MBean，但机制和使用场景不同。

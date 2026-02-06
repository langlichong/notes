##  JVM Unified Logging Framework

- ref https://www.tianxiaohui.com/index.php/Java%E7%9B%B8%E5%85%B3/JVM-%E7%BB%9F%E4%B8%80%E7%9A%84%E6%97%A5%E5%BF%97%E6%A1%86%E6%9E%B6.html

> JVM 统一的日志框架(JVM Unified Logging Framework)是 JDK 9 引入的一个中心化的日志框架, 通过这个日志框架, 我们可以观测 JVM 的 类加载(class loading), 线程(thread), 垃圾收集(GC), 模块系统(Module System) 等相关信息. 我们可以通过设置设置 JVM 启动参数 `-Xlog` 来与这个统一的日志框架交互, 从而让这个统一的日志框架输出不同组建的, 不同层级(level)的日志到我们指定的日志文件.
>
> 除了通过 `-Xlog` 这个启动参数外, 我们也可以在运行时实时修改统一日志框架的输出. 一种方法是通过 jcmd 命令的 `VM.log` 子命令来修改, 另外一种方法是通过修改对应的 MBean. `-Xlog` 是在应用启动应用时候修改, 后面2种都是运行时动态修改.
>
> 不同于应用本身的日志

- **3种方法与 JVM 统一的日志框架交互**

1. 启动参数 `-Xlog` 适用于启动时候设置

2. 使用 `jcmd <pid> VM.log` 可以动态调整日志的输出

3. 使用 MBean, 可以动态调整, 没有 jcmd 命令方便

   ```tex
   打印日志的选项主要有4个:
   
   what: 选择标签(tags)和日志层级
   output: 选择输出到哪里 stdout, stderr 或者文件
   output_options: 如果选择输出到文件, 文件的一些选项, 比如大小和rotate的多少
   decorators: 日志行除了日志内容外, 还要加哪些信息.
   
   如果想查看帮助和所有的选项内容, 可以尝试下面的命令:
   
   $ jcmd <pid> help VM.log
   $ jcmd <pid> VM.log list
   ```

   

### demos

- ```bash
  #所有的日志都通过 jcmd VM.log 的方式输出到 /tmp/my.log
  jcmd 3499 VM.log output="file=/tmp/my.log" what="all=trace"
  ```

- ```bash
  #写到文件系统的, 一般都能设置rotate, 就是文件轮转. 比如最多写5个文件, 每个文件最大6M,文件大小的单位可以是 K, M, G
  jcmd 3499 VM.log output="stdout" what="all=trace" output_options="filecount=5,filesize=6M"
  ```

- 日志内容及范围

  ```bash
  #`what="all=trace"`, 这部分选项告诉日志系统, 什么东西要写到日志文件里面
  #这个统一的日志框架可以输出不同模块的日志,比如GC, 线程, classload等模块
  #为了让我们有选择的输出, 需要一个选择标准, 这里使用的就是标签. 日志框架在输出日志的时候, 给每一条日志都加了一#个或多个标签, 我们可以选择不同的标签组合, 选择不同的日志内容输出到日志文件.
  
  #命令来看当前的 JVM 支持哪些标签
  jcmd 3499 VM.log list
  Available log levels: off, trace, debug, info, warning, error
  Available log decorators: time (t), utctime (utc), uptime (u), timemillis (tm), uptimemillis (um), timenanos (tn), uptimenanos (un), hostname (hn), pid (p), tid (ti), level (l), tags (tg)
  Available log tags: add, age, alloc, annotation, aot, arguments, attach 略
  
  #what 所表达的含义, 它通过 tags 和日志 level 一起来选择什么(what)日志应该被输出出来. 比如:
  gc=debug, jfr=trace, init=info
  ```

  
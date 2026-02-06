### docs

- https://docs.oracle.com/en/java/javase/21/troubleshoot/toc.htm



##### help

- java -X : 打印所有标准的兼容的jvm选项

- java -Xlog:help: 查看 Xlog 用法

- java -XX:+PrintFlagsFinal: 打印所有可用的扩展选项

- 

- ```bash
  java -XX:+PrintFlagsFinal -version
  ```

### jvm 启动参数

- -XX:+PrintClassHistogram

- **-XX:+HeapDumpOnOutOfMemoryError**

- **-XX:+PrintGCDetails** :This argument prints detailed information about garbage collection, including the type of GC, the number of collections, and the time spent in GC

- **-XX:+PrintHeapAtGC**: This argument prints the heap layout at each garbage collection, which can help identify memory-related issues.

- **-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/dump/file -XX:+PrintGCDetails**

- ```
  JAVA_OPTS: 简化配置
  ```

### jdk tools

- man jcmd

- man jstack

- man jfr 

- man jhsdb

  > hsdb : hotspot debugger

- jhsdb jstack
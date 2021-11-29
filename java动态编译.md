
jvm 规范：https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html

类似于菜鸟java在线工具的效果：https://c.runoob.com/compile/10
https://www.cnblogs.com/andysd/p/10081443.html

重点需要了解的概念是：
JavaFileManage、JavaFileObject


JavaCompiler类使用：一般需要jdk中tools.jar 的支持

参考Aviator的实现原理


如果我们要自己直接输出二进制格式的字节码，在完成这个任务前，必须先认真阅读JVM规范第4章，详细了解class文件结构。估计读完规范后，两个月过去了。
所以，第一种方法，自己动手，从零开始创建字节码，理论上可行，实际上很难。
第二种方法，使用已有的一些能操作字节码的库，帮助我们创建class。
```
Enhancer e = new Enhancer();
e.setSuperclass(...);
e.setStrategy(new DefaultGeneratorStrategy() {
  protected ClassGenerator transform(ClassGenerator cg) {
      return new TransformingGenerator(cg,
          new AddPropertyTransformer(new String[]{ "foo" },
                  new Class[] { Integer.TYPE }));
  }});
Object obj = e.create();
```
目前，能够操作字节码的开源库主要有CGLib和Javassist两种，它们都提供了比较高级的API来操作字节码，最后输出为class文件。
比自己生成class要简单，但是，要学会它的API还是得花大量的时间，并且，上面的代码很难看懂对不对.

换一个思路，如果我们能创建UserProxy.java这个源文件，再调用Java编译器，直接把源码编译成class，再加载进虚拟机，任务完成！
毕竟，创建一个字符串格式的源码是很简单的事情，就是拼字符串嘛，高级点的做法可以用一个模版引擎。
如何编译？
Java的编译器是javac，但是，在很早很早的时候，Java的编译器就已经用纯Java重写了，自己能编译自己，行业黑话叫“自举”。从Java 1.6开始，编译器接口正式放到JDK的公开API中，于是，我们不需要创建新的进程来调用javac，而是直接使用编译器API来编译源码.
```
  JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
  int compilationResult = compiler.run(null, null, null, '/path/to/Test.java');
```
这么写编译是没啥问题，问题是我们在内存中创建了Java代码后，必须先写到文件，再编译，最后还要手动读取class文件内容并用一个ClassLoader加载。
其实Java编译器根本不关心源码的内容是从哪来的，你给它一个String当作源码，它就可以输出byte[]作为class的内容。
所以，我们需要参考Java Compiler API的文档，让Compiler直接在内存中完成编译，输出的class内容就是byte[]。
```
  Map<String, byte[]> results;
  JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
  StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
  try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
    JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
    CompilationTask task = compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
    if (task.call()) {
        results = manager.getClassBytes();
    }
  }
```
上述代码的几个关键在于：
用MemoryJavaFileManager替换JDK默认的StandardJavaFileManager，以便在编译器请求源码内容时，不是从文件读取，而是直接返回String；
用MemoryOutputJavaFileObject替换JDK默认的SimpleJavaFileObject，以便在接收到编译器生成的byte[]内容时，不写入class文件，而是直接保存在内存中。
最后，编译的结果放在Map<String, byte[]>中，Key是类名，对应的byte[]是class的二进制内容。
为什么编译后不是一个byte[]呢？
因为一个.java的源文件编译后可能有多个.class文件！只要包含了静态类、匿名类等，编译出的class肯定多于一个。
如何加载编译后的class呢？
加载class相对而言就容易多了，我们只需要创建一个ClassLoader，覆写findClass()方法：
```
  class MemoryClassLoader extends URLClassLoader {

    Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

    public MemoryClassLoader(Map<String, byte[]> classBytes) {
        super(new URL[0], MemoryClassLoader.class.getClassLoader());
        this.classBytes.putAll(classBytes);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] buf = classBytes.get(name);
        if (buf == null) {
            return super.findClass(name);
        }
        classBytes.remove(name);
        return defineClass(name, buf, 0, buf.length);
    }
  }

动态编译除了写ORM用之外，还能干什么？
可以用它来做一个Java脚本引擎。实际上本文的代码主要就是参考了Scripting项目的源码。
完整的源码呢？

在这里：https://github.com/michaelliao/compiler，连Maven的包都给你准备好了！
springboot应用打包后，springboot打的jar包的classpath和jarPath都变了(jar in jar形式)， springboot重写了URLClassLoader(LaunchedURLClassLoader)， 而JavaCompiler无法引用springboot 打包后jar中的类， 所以我覆写了MemoryJavaFileManager的getClassLoader方法， 返回了自己写的LaunchedURLClassLoader(参照springboot)，但就算是编译过了， 在task.call()时，根据没有调用getJavaFileForOutput，所以就没有写入Map<String, byte[]> classBytes，导致无法加载类， 请问有什么解决办法吗？ 如果楼主大神或哪位大神有解决办法？？？？

     在spring容器中不能使用clazz.newInstance()进行类的加载，而是要采用Spring的IoC方式创建类的实例，例如： applicationContext.getAutowireCapableBeanFactory().createBean(clazz);

```

Java Agent技术
Java 8 provides an API for creating Javac plugins.：https://www.baeldung.com/java-build-compiler-plugin
```
  plugin: com.sun.source.util.Plugin interface
Plugin Lifecycle
A plugin is called by the compiler only once, through the init() method.

To be notified of subsequent events, we have to register a callback. These arrive before and after every processing stage per source file:

PARSE – builds an Abstract Syntax Tree (AST)
ENTER – source code imports are resolved
ANALYZE – parser output (an AST) is analyzed for errors
GENERATE – generating binaries for the target source file
```

https://github.com/michaelliao/compiler

http://janino-compiler.github.io/janino/
https://www.jianshu.com/p/fcb7f7ba6bf5
https://github.com/OpenHFT/Java-Runtime-Compiler
https://www.freesion.com/article/3093664440/

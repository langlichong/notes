## 1、 Janino 简介
```
Janino 是一个极小、极快的 开源Java 编译器（Janino is a super-small, super-fast Java™ compiler.）。
Janino 不仅可以像 JAVAC 一样将 Java 源码文件编译为字节码文件，
还可以编译内存中的 Java 表达式、块、类和源码文件，加载字节码并在 JVM 中直接执行。
Janino 同样可以用于静态代码分析和代码操作。

编译表达式, 编译脚本, 编译类

Kettle使用案列：http://www.uml.org.cn/sjjmck/201910101.asp?artid=22506
```
 由于Kettle使用Janino框架为自定义Java转换步骤类动态定义了类名，并指定父类为TransformClassBase，
 所以在撰写代码时，只需要提供类的内容即可，无需class声明.
```

e.g:  Pass a string containing Java code to return an object

重要用途：Using dynamically compiled classes to replace Java proxy classes can avoid all reflections,
so performance can be significantly improved。

Janino Not a development tool, but an embedded compiler at runtime,
such as a translator for expression evaluation or a server-side page engine similar to JSP.

blog: https://www.programmersought.com/article/79415492358/

项目地址：https://github.com/janino-compiler/janino
其中主要的Evaluator:
   Expression Evaluator ,Script Evaluator,Class Body Evaluator,Simple Compiler,Janino as a Compiler
```
## 2、ScriptEvaluator执行java代码

 示例 1：
 ```
       ScriptEvaluator se = new ScriptEvaluator();
         se.cook(
                 ""
                         + "static void method1() {\n"
                         + "    System.out.println(1);\n"
                         + "}\n"
                         + "\n"
                         + "method1();\n"
                         + "method2();\n"
                         + "\n"
                         + "static void method2() {\n"
                         + "    System.out.println(2);\n"
                         + "}\n"
         );

         se.evaluate(new Object[0]);

 ```

 示例2：脚本传值
 ```
      ScriptEvaluator se = new ScriptEvaluator();
      se.setParameters(new String[] { "arg1", "arg2" }, new Class[] { String.class, int.class });
      se.cook(
              ""
                      + "System.out.println(arg1);\n"
                      + "System.out.println(arg2);\n"
                      + "\n"
                      + "static void method1() {\n"
                      + "    System.out.println(\"run in method1()\");\n"
                      + "}\n"
                      + "\n"
                      + "public static void method2() {\n"
                      + "    System.out.println(\"run in method2()\");\n"
                      + "}\n"
                      + "\n"
                      + "method1();\n"
                      + "method2();\n"
                      + "\n"

      );
      //传参
      se.evaluate(new Object[]{"aaa",22});

 ```


 示例 3：尝试执行一个完整的类代码

 首先在Idea中定义了如下一个测试类(先保证该类可以正常运行)：
 ```
    package com.huhu.dymamic.compile.janino;
    import java.util.Enumeration;
    import java.util.Properties;

    public class WalkTreeTest {
      public static void walkCurrentDir(){

          final Properties properties = System.getProperties();
          final Enumeration<?> enumeration = properties.propertyNames();
          for(;enumeration.hasMoreElements();){
              final String name = enumeration.nextElement().toString();
              System.out.println( name + " = " + properties.getProperty(name));
          }
          System.out.println("java.vm.version =" + System.getProperty("java.vm.version"));
      }
    }
    注意： 类中没有定义main入口方法,该类可能将来就是在某个界面中书写的片段代码，然后提交给后台去执行的
   ```
   目标： 通过Janino的ScriptEvaluator来编译并执行该文件，为此定义一个测试主类：
   ```
   public class JaninoMain {

    public static void main(String[] args) throws Exception {
       // classBody 是上述的WalkTreeTest类，去掉package语句, 保留 import语句，去掉类声明，保留方法walkCurrentDir全部代码
       // 同时在walkCurrentDir方法定义的后面，增加了调用语句(是不是觉得很不常规路？？)
        String classBody =
                "import java.util.Enumeration;\n" +
                "import java.util.Properties;\n" +
                "    public static void walkCurrentDir(){ \n" +
                "        final Properties properties = System.getProperties();\n" +
                "        final Enumeration<?> enumeration = properties.propertyNames();\n" +
                "        for(;enumeration.hasMoreElements();){\n" +
                "            final String name = enumeration.nextElement().toString();\n" +
                "            System.out.println( name + \" = \" + properties.getProperty(name));\n" +
                "        }\n" +
                "        System.out.println(\"java.vm.version =\" + System.getProperty(\"java.vm.version\"));\n" +
                "    }\n" +
                "\n" +
                "walkCurrentDir();";

        ScriptEvaluator se = new ScriptEvaluator();
        se.cook(classBody);
        se.evaluate(null);
    }
  }

 ```
## 3、简单表达式
```
      ExpressionEvaluator e = new ExpressionEvaluator();
      e.cook("3 + 4");
      System.out.println(e.evaluate(null));

      // 三元表达式
       Object[] arguments = { new Double(args[0]) };
       IExpressionEvaluator ee = CompilerFactoryFactory.getDefaultCompilerFactory().newExpressionEvaluator();
       ee.setExpressionType(double.class);
       ee.setParameters(new String[] { "total" }, new Class[] { double.class });
       ee.cook("total >= 100.0 ? 0.0 : 7.95");

       // Evaluate expression with actual parameter values.
       Object res = ee.evaluate(arguments);
```
## 4、参数化表达式计算
```
       ExpressionEvaluator ee = new ExpressionEvaluator();
       // 两个参数，名称分别为a , b  , 并指定参数类型
       ee.setParameters(new String[] { "a", "b" }, new Class[] { int.class, int.class });
       // 指定表达式计算结果的类型
       ee.setExpressionType(int.class);
       // cook 是该库的固定写法，执行表达式: a + b
       ee.cook("a + b");
       int result = (Integer) ee.evaluate(new Object[] { 19, 23 });
       System.out.println(result);
```
## 5、ClassBody
```
       // 示例1： Compile the class body.
       IClassBodyEvaluator cbe = CompilerFactoryFactory.getDefaultCompilerFactory().newClassBodyEvaluator();
       cbe.cook(classBody);
       Class<?> c = cbe.getClazz();

       // Invoke the "public static main(String[])" method.
       Method m           = c.getMethod("main", String[].class);
       Object returnValue = m.invoke(null, (Object) arguments);

       // If non-VOID, print the return value.
       if (m.getReturnType() != void.class) {
           System.out.println(
               returnValue instanceof Object[]
               ? Arrays.toString((Object[]) returnValue)
               : String.valueOf(returnValue)
           );
       }


    // 示例2：实现一个空接口
    public class JaninoTester06 {

      // 定义一个接口，一个bar操作
    	public interface Foo {
    	    int bar(int a, int b);
    	}

    	public static void main(String[] args) throws Exception {

    		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    		final IClassBodyEvaluator bodyEvaluator = CompilerFactoryFactory.getDefaultCompilerFactory(contextClassLoader).newClassBodyEvaluator();
    		//bodyEvaluator.setExtendedClass(Foo.class);
        //设置代码片段要实现的接口（由于接口中只有一个方法，所以crateInstance片段中只有一个bar的实现，
        //若接口中有多个方法，则createInstance的代码片段必须包含所有方法的实现，否则无法创建实例。
    		bodyEvaluator.setImplementedInterfaces(new Class[]{Foo.class});
    		Foo instance = (Foo) bodyEvaluator.createInstance(new StringReader("public int bar(int a, int b) { return a + b; }"));
    		System.out.println("1 + 2 = " + instance.bar(1, 2));

    	}

  }
```
## 6、ScriptEvaluator调用已有的类
```
  // 定义一个类，及其子类（定义类时候不一定是这种关系）
  package com.huhu.dymamic.compile.janino.caller;
  public class BaseClass {
  	private String baseId;
  	public BaseClass(String baseId) {
  		super();
  		this.baseId = baseId;
  	}

  	@Override
  	public String toString() {
  		return "BaseClass [baseId=" + baseId + "]";
  	}
  }

 // 子类
 package com.huhu.dymamic.compile.janino.caller;
  public class DerivedClass extends BaseClass {
  	private String name;
  	public DerivedClass(String baseId, String name) {
  		super(baseId);
  		this.name = name;
  	}

  	@Override
  	public String toString() {
  		return super.toString() + "DerivedClass [name=" + name + "]";
  	}
  }

  // 使用如上定义的类，注意 import 语句
  public static void main(String[] args) {
		try {
			IScriptEvaluator se = new ScriptEvaluator();
			se.setReturnType(String.class);
			se.cook("import com.huhu.dymamic.compile.janino.caller.BaseClass;\n"
					+ "import com.huhu.dymamic.compile.janino.caller.DerivedClass;\n"
					+ "BaseClass o=new DerivedClass(\"1\",\"join\");\n"
					+ "return o.toString();\n");
			Object res = se.evaluate(new Object[0]);
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

```
## 7、作为编译器使用
```
    // 编译不同包中源码，并调用执行
    public static void main(String[] args) throws Exception{
         final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
         final ICompilerFactory compilerFactory = CompilerFactoryFactory.getDefaultCompilerFactory(contextClassLoader);
         ICompiler compiler = compilerFactory.newCompiler();

         // Store generated .class files in a Map:
         Map<String, byte[]> classes = new HashMap<String, byte[]>();
         compiler.setClassFileCreator(new MapResourceCreator(classes));
         // Now compile two units from strings:
         compiler.compile(new Resource[] {
                 new StringResource(
                         "pkg1/A.java",
                         "package pkg1; public class A { public static int meth() { return pkg2.B.meth(); } }"
                 ),
                 new StringResource(
                         "pkg2/B.java",
                         "package pkg2; public class B { public static int meth() { return 77; } }"
                 ),
         });

        // Set up a class loader that uses the generated classes.
         ClassLoader cl = new ResourceFinderClassLoader(new MapResourceFinder(classes), ClassLoader.getSystemClassLoader() );

         final Object meth = cl.loadClass("pkg1.A").getDeclaredMethod("meth").invoke(null);
         // result is: 77
         System.out.println(meth);

     }
```
## 8、JavaSourceClassLoader作为类加载使用
```
 // 类A 其所在的包的物理路径：D:\\zhangwei\\sourceCode\\Base_Line\\709\\report\\ureport-demo\\src\\main\\java\\com\\huhu\\dymamic\\compile\\janino\\classloader

 package com.huhu.dymamic.compile.janino.classloader;
  public class A extends B {
  }

  //类B
  public class B implements Runnable {
    @Override
    public void run() {
        System.out.println("HELLO");
    }
  }

  // 测试类
  public static void main(String[] args) throws Exception {

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        // 类包所在的文件夹
        String sourcePath = "D:\\zw\\sourceCode\\Base_Line\\709\\report\\ureport-demo\\src\\main\\java\\com\\huhu\\dymamic\\compile\\janino\\classloader";

        // 方式1： 指定类所在的物理路径 或者使用方式2
        final JavaSourceClassLoader cl = new JavaSourceClassLoader(contextClassLoader, new File[]{Paths.get(sourcePath).toFile()},null);

        // 方式2 final JavaSourceClassLoader cl = new JavaSourceClassLoader(contextClassLoader);

        /*ClassLoader cl = new JavaSourceClassLoader(
                contextClassLoader,  // parentClassLoader
                new File[] { new File("srcdir") }, // 可选的源码路径
                (String) null                // 可选的编码
        );*/

        // Load class A from "srcdir/pkg1/A.java", and also its superclass
        // B from "srcdir/pkg2/B.java":
        Object o = cl.loadClass("com.huhu.dymamic.compile.janino.classloader.A").newInstance();

        ((Runnable) o).run(); // Prints "HELLO" to "System.out".

    }
```
## 9、静态代码分析
```
  除了编译代码之外，Janino还可以当做一个静态代码分析工具（基于AST）
```

## 10、安全沙箱
```
由于 JANINO 生成的字节码可以完全访问 JRE，如果正在编译和执行的表达式、脚本、类主体或编译单元包含用户输入，则会出现安全问题。
安全防范：
  依赖java本身的 Java security manager：
    JANINO includes a very easy-to-use security API, which can be used to lock expressions, scripts, class bodies and compilation units into a "sandbox", which is guarded by a Java security manager。

  使用如下：

  public static void
    main(String[] args) throws Exception {

        // Create a JANINO script evaluator. The example, however, will work as fine with
        // ExpressionEvaluators, ClassBodyEvaluators and SimpleCompilers.
        ScriptEvaluator se = new ScriptEvaluator();
        se.setDebuggingInformation(true, true, false);

        // Now create a "Permissions" object which allows to read the system variable
        // "foo", and forbids everything else.
        Permissions permissions = new Permissions();
        permissions.add(new PropertyPermission("foo", "read"));

        // Compile a simple script which reads two system variables - "foo" and "bar".
        PrivilegedAction<!--?--> pa = se.createFastEvaluator((
            "System.getProperty(\"foo\");\n" +
            "System.getProperty(\"bar\");\n" +
            "return null;\n"
        ), PrivilegedAction.class, new String[0]);

        // Finally execute the script in the sandbox. Getting system property "foo" will
        // succeed, and getting "bar" will throw a
        //    java.security.AccessControlException: access denied (java.util.PropertyPermission bar read)
        // in line 2 of the script. Et voila!
        Sandbox sandbox = new Sandbox(permissions);
        sandbox.confine(pa);
    }

  Java 安全管理器主要包含如下控制：
    Accept a socket connection from a specified host and port number
    Modify a thread (change its priority, stop it, and so on)
    Open a socket connection to a specified host and port number
    Create a new class loader
    Delete a specified file
    Create a new process
    Cause the application to exit
    Load a dynamic library that contains native methods
    Wait for a connection on a specified local port number
    Load a class from a specified package (used by class loaders)
    Add a new class to a specified package (used by class loaders)
    Access or modify system properties
    Access a specified system property
    Read from a specified file
    Write to a specified file

```

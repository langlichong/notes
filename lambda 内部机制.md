源码
 └── lambda表达式：s -> doSomething(s)
       ↓
编译器
 └── 生成 invokedynamic 指令
       ↓
JVM 运行时第一次执行
 └── 调用 BootstrapMethod（通常是 LambdaMetafactory.metafactory）
       ↓
LambdaMetafactory
 └── 生成实现类 + 用 MethodHandle 绑定
       ↓
返回一个接口实现（如 Function 实例）


- 反编译lambda 验证: `javap -c -v -p YourClass.class`

- 动态生成 Lambda 的代码：使用LambdaMetafactory动态生成(编译器 及 jvm内部就是如此工作)
  ```java
  import java.lang.invoke.*;
import java.util.function.Function;

public class LambdaDemo {

    // 要被调用的方法
    public static String greet(String name) {
        return "Hello, " + name;
    }

    public static void main(String[] args) throws Throwable {
        // 获取用于查找方法句柄的 lookup 对象
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // 获取 greet(String) 方法的 MethodHandle
        MethodHandle targetMethod = lookup.findStatic(
            LambdaDemo.class,
            "greet",
            MethodType.methodType(String.class, String.class)
        );

        // 使用 LambdaMetafactory 构造 CallSite
        CallSite site = LambdaMetafactory.metafactory(
            lookup,
            "apply", // 接口方法名
            MethodType.methodType(Function.class), // 期望的函数接口类型
            MethodType.methodType(Object.class, Object.class), // 传入Object返回Object的函数（擦除签名）
            targetMethod, // 目标 MethodHandle
            MethodType.methodType(String.class, String.class) // 实际签名
        );

        // 获取函数式接口实现实例
        Function<String, String> fn = (Function<String, String>) site.getTarget().invoke();

        // 调用
        System.out.println(fn.apply("world"));  // 输出: Hello, world
    }
}


  ```

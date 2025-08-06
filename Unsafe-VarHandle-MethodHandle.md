- AtomicInteger 能实现的，VarHandle 也可以实现，只不过更啰嗦
| 方面    | `AtomicInteger`                              | `VarHandle`               |
| ----- | -------------------------------------------- | ------------------------- |
| 封装程度  | 高：提供了方法封装（如 `incrementAndGet`）               | 低：需要手动处理字段、偏移等            |
| 使用复杂度 | 简单，直接调用                                      | 啰嗦，需要获取 `VarHandle`、传入实例等 |
| 类型安全  | 限定为 int                                      | 通用，可以是任意字段                |
| 性能    | 与 `VarHandle` 非常接近（底层是 `Unsafe`/`VarHandle`） | 极高，可避免额外包装                |
| 使用场景  | 普通业务逻辑、多线程计数等                                | 高性能组件、自定义原子类框架            |
| 灵活性   | 固定为 `int` 封装                                 | 可用于任何对象字段、数组、甚至静态字段       |


- Java 中的 VarHandle 与 MethodHandle，它们是现代 Java（Java 9+ 和 Java 7+）提供的更安全、标准的替代 Unsafe 的方式，用于进行底层操作（字段访问、方法调用等）

```text
脚本引擎的核心能力 = 动态绑定 + 快速调用
而 MethodHandle + invokedynamic 正是为此目的设计的。

这也是为什么：

Nashorn（旧 JavaScript 引擎）

GraalVM（现代多语言平台）

Kotlin Script

JRuby / Jython

等脚本运行时都大量使用 MethodHandle 作为内部函数调用机制
```
- 通过 LambdaMetafactory 把 MethodHandle 转成动态接口（几乎为零开销的脚本调用）
  ```text
  MethodHandles 获取方法句柄 , 使用 LambdaMetafactory 将其包装为一个 Java 接口（如 Function<String, String>）,模拟脚本调用（动态字符串方法名 + 参数绑定)

  使用 MethodHandle + LambdaMetafactory 生成并缓存一个 Function
  ```
  - LambdaMetafactory 会生成一个匿名类实现 Function<T, R>,内部代码就像这样:
  ```java
  new Function<String, String>() {
    public String apply(String s) {
        return (String) methodHandle.invokeExact(s);
    }
}

  ```

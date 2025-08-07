# Java 7 到 JDK 24 重要特性与API补充全览

---

## Java 7 (2011)

- **语言特性**  
  - Switch 支持 String  
  - 二进制字面量（`0b1010`）  
  - 数字字面量中允许下划线分隔（`1_000_000`）  
  - Try-with-resources（自动关闭资源）  
  - 多重异常捕获（catch 多异常）  
  - 泛型钻石操作符 `<>` 简化泛型实例化  

- **新增API**  
  - `java.nio.file` (NIO.2) 文件操作新API：`Path`, `Files`, `FileSystem`，极大改善文件操作能力  
  - **`java.lang.invoke.MethodHandle` 与 `MethodHandles.Lookup`**：支持动态语言调用，替代传统反射，性能更优  
  - Fork/Join 框架（`java.util.concurrent`）用于并行任务分解执行，核心高性能并发框架  
  - 新增并发工具类：`Phaser`（可变阶段同步器）、`RecursiveTask`、`RecursiveAction`  
  - `java.util.Objects` 辅助类（`requireNonNull` 等）  

---

## Java 8 (2014)

- **语言特性**  
  - Lambda表达式与闭包  
  - 方法引用（`ClassName::methodName`）  
  - 函数式接口（`@FunctionalInterface`）  
  - 接口默认方法和静态方法（接口演进重要手段）  
  - 重复注解  
  - 类型注解（JSR 308）  

- **新增API**  
  - **Stream API**（`java.util.stream`）支持函数式风格集合操作，支持串行与并行  
  - 丰富的 **Collectors**，支持 `groupingBy`, `partitioningBy`, `toMap` 等多种聚合策略  
  - 新的日期时间API（`java.time` 包，JSR 310） — 更现代且线程安全  
  - **Optional 类**：消除 null 引发的 NPE，支持链式处理  
  - **CompletableFuture**：强大的异步任务框架，支持链式、组合、异常处理  
  - Base64 编码解码工具  
  - Nashorn JavaScript引擎（替代Rhino）  

---

## Java 9 (2017)

- **语言特性**  
  - **模块系统（JPMS）**：模块化 JDK 与应用，严格控制包访问，支持版本管理  
  - 私有接口方法  
  - 集合工厂方法：`List.of()`, `Set.of()`, `Map.of()` 提供不可变集合创建方便方法  
  - **`java.lang.invoke.VarHandle`**：灵活的变量访问句柄，提供原子性和内存顺序控制，替代 `Unsafe`  

- **新增API**  
  - **Flow API**（响应式流）接口，用于构建异步、非阻塞流处理  
  - Process API增强，支持进程树、PID查询  
  - StackWalker API — 更高效、灵活的栈帧访问  
  - HTTP/2客户端早期实验（后续版本完善）  
  - 多版本兼容JAR支持  

- **性能**  
  - Compact Strings（内部字符串编码优化）  
  - Class Data Sharing（CDS）增强  

---

## Java 10 (2018)

- **语言特性**  
  - 局部变量类型推断（`var`关键字）  
  - lambda 参数支持局部变量语法  

- **新增API**  
  - 应用数据大小统计增强  
  - 垃圾回收接口优化  
  - 容器感知的JVM特性（内存限制识别等）  
  - Application Class-Data Sharing（AppCDS）提升启动速度  

---

## Java 11 (2018)

- **语言特性**  
  - 支持 lambda 表达式中使用局部变量语法（`var`）  

- **新增API**  
  - 标准 HttpClient（替代老旧 HttpURLConnection），支持 HTTP/2 和异步请求  
  - String 新增方法：`isBlank()`, `lines()`, `repeat()`, `strip()`, `stripLeading()`, `stripTrailing()`  
  - Files新增辅助方法  
  - Flight Recorder开源，应用性能监控利器  
  - TLS 1.3 支持  

---

## Java 12 (2019)

- **语言特性**  
  - Switch表达式（预览），支持作为表达式返回值，配合 `yield` 使用  
  - 更友好的垃圾回收日志  

- **新增API**  
  - JVM常量动态加载（ConstantDynamic）支持动态常量  
  - Shenandoah GC（低暂停垃圾收集器）实验性引入  
  - JIT和内存分配优化  

---

## Java 13 (2019)

- **语言特性**  
  - 文本块（Text Blocks）预览，方便多行字符串定义（如 JSON、SQL、HTML）  

- **新增API**  
  - 动态 CDS 归档改进  
  - Socket API增强（支持 socket 配置）  

---

## Java 14 (2020)

- **语言特性**  
  - Switch表达式正式  
  - **Records 预览**（不可变数据载体类，减少模板代码）  
  - Pattern Matching for `instanceof` 预览，简化类型判断与转换  
  - NullPointerException 详尽提示（精准指出空指针来源）  

- **新增API**  
  - 非易失性内存（NVM）支持  
  - 内存访问API（早期外部内存访问支持）  

---

## Java 15 (2020)

- **语言特性**  
  - Records 正式引入  
  - Sealed Classes 预览（限制继承和实现）  
  - Hidden Classes 引入，支持动态生成类，用于框架和代理  

- **新增API**  
  - ZGC 垃圾收集器正式发布，低延迟、高并发  
  - Text Blocks 标准化  

---

## Java 16 (2021)

- **语言特性**  
  - Pattern Matching for `instanceof` 正式  
  - Records完善（支持局部变量等）  
  - Sealed Classes 继续预览  
  - 更强的封装JDK内部API  

- **新增API**  
  - Unix Domain Socket支持  
  - Vector API 预览 — SIMD 矢量化计算加速  
  - Foreign Linker API 实验性支持，简化调用本地代码  

---

## Java 17 (2021, LTS)

- **语言特性**  
  - Sealed Classes 正式  
  - Records、Pattern Matching 持续完善  

- **新增API**  
  - macOS/AArch64 原生支持  
  - 新伪随机数生成器（Enhanced PRNG，JEP 356）  
  - 高质量垃圾收集与性能提升  

---

## Java 18 (2022)

- **语言特性**  
  - UTF-8 默认字符集（国际化更统一）  
  - 简单的内置 HTTP 服务器（开发测试方便）  

- **新增API**  
  - Vector API增强  
  - Foreign Function & Memory API 预览升级  

---

## Java 19 (2022)

- **语言特性**  
  - 虚拟线程（Virtual Threads）预览（Project Loom）  
  - 结构化并发（Structured Concurrency）预览  
  - Pattern Matching for switch 预览  

- **新增API**  
  - Foreign Function & Memory API 强化  
  - Record Patterns 预览  

---

## Java 20 (2023)

- **语言特性**  
  - 结构化并发预览增强  
  - Record Patterns增强  
  - Pattern Matching for switch增强  

- **新增API**  
  - 虚拟线程相关API完善  

---

## Java 21 (2023, LTS)

- **语言特性**  
  - 虚拟线程（Virtual Threads）正式  
  - 结构化并发（Structured Concurrency）正式预览  
  - Record Patterns 完善  
  - String Templates 预览  

- **新增API**  
  - Sequenced Collections 接口（保证顺序访问的集合）  
  - 泛型推断改进  
  - String Templates  

---

## Java 22 - 24 (2024-2025，预期与实验特性)

- **语言特性**  
  - 模式匹配更广泛应用（switch、解构）  
  - String Templates 更完善  
  - Value Types（Project Valhalla）持续推进，提高性能，减少装箱  
  - 并发和内存访问API升级  

- **新增API**  
  - 外部函数接口（FFM API）进一步完善  
  - 结构化并发相关工具  
  - 低延迟GC和性能优化  

---

# 跨版本重要API和高级特性补充

- **CompletableFuture（Java 8起）**  
  支持强大的异步任务编排、链式调用、异常处理，极大简化复杂异步逻辑。

- **Optional（Java 8）**  
  避免 NullPointerException，鼓励显式判断和处理缺失值。

- **ForkJoinPool（Java 7）**  
  适合大规模并行分解任务执行，构建高效并行计算。

- **VarHandle（Java 9）**  
  替代 `Unsafe`，支持安全的原子操作、内存屏障，适合构建底层高性能同步结构。

- **Vector API（Java 16+预览）**  
  利用 SIMD 指令集加速批量计算，适合科学计算和图像处理等场景。

- **Foreign Function & Memory API（Java 16+实验）**  
  简化调用本地 C 函数，安全访问本地内存，未来替代 JNI。

- **Flight Recorder**  
  Java 内建的低开销性能和事件监控工具，从 Java 11 开源。

- **JFR Event Streaming（Java 17+）**  
  实时事件流监控，便于运维和性能分析。

---

# 重要实用代码示例

```java
// CompletableFuture 异步串联示例 (Java 8)
CompletableFuture.supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")
    .thenAccept(System.out::println);

// VarHandle 原子更新示例 (Java 9)
class Counter {
    private volatile int value = 0;
    private static final VarHandle VALUE_HANDLE;
    static {
        try {
            VALUE_HANDLE = MethodHandles.lookup()
                .findVarHandle(Counter.class, "value", int.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
    public void increment() {
        int oldVal;
        do {
            oldVal = (int) VALUE_HANDLE.getVolatile(this);
        } while (!VALUE_HANDLE.compareAndSet(this, oldVal, oldVal + 1));
    }
}

// Vector API 示例（Java 16+ 预览）
VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
int[] array = new int[1000];
// 处理数组时利用SIMD向量计算，提升性能
for (int i = 0; i < array.length; i += SPECIES.length()) {
    IntVector vec = IntVector.fromArray(SPECIES, array, i);
    vec = vec.add(1);
    vec.intoArray(array, i);
}

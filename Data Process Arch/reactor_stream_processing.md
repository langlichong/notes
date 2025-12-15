# 为什么 Reactor / RxJava 是流处理 DSL 的最佳“平替”？

如果你喜欢 Flink 那种 `source.filter(...).map(...).sink(...)` 的优雅链式写法，但又不想部署庞大的 Flink 集群，**Project Reactor (Spring WebFlux 的核心)** 是在 Java 单体应用中实现这种范式的**终极答案**。

它不仅长得像流处理，它的**内核**（背压、异步、事件驱动）其实和 Flink 是一样的。

---

## 1. 代码对比：Flink vs. Reactor

也就是“换个马甲”的事，逻辑几乎一模一样。

### Flink (分布式流引擎)
```java
DataStream<SensorData> stream = env.addSource(mqttSource);

stream
    .filter(data -> data.getValue() > 50)             // 过滤
    .map(data -> enrichWithMetadata(data))            // 转换/关联
    .keyBy(data -> data.getDeviceId())                // 分组
    .window(TumblingEventTimeWindows.of(Time.seconds(10))) // 窗口
    .process(new AlertFunction())                     // 告警
    .addSink(new DatabaseSink());                     // 输出
```

### Project Reactor (Java 本地库)
```java
// 在你的 Spring Boot 应用里直接写
Flux<SensorData> stream = mqttInboundAdapter.inboundFlux(); // 获取流

stream
    .filter(data -> data.getValue() > 50)             // 过滤：完全一样
    .map(data -> enrichWithMetadata(data))            // 转换：直接调用本地方法
    .groupBy(data -> data.getDeviceId())              // 分组：逻辑一样
    .flatMap(groupedFlux ->                           // 对每个分组处理
        groupedFlux.window(Duration.ofSeconds(10))    // 窗口：支持时间窗口
                   .flatMap(window -> window.collectList()) // 聚合窗口数据
                   .doOnNext(list -> calculateAlert(list))  // 触发告警
    )
    .subscribe(); // 启动流 (相当于 Flink execute)
```

---

## 2. 为什么它是最推荐的？

### A. 相同的思维模型 (The Mental Model)
Reactor 是 **Reactive Streams** 规范的实现。它的操作符（Operators）设计初衷就是为了处理**异步数据流**。
*   你需要过滤？用 `.filter()`。
*   你需要缓冲？用 `.buffer(Duration.ofSeconds(1))`。
*   你需要从慢速数据库获取数据但不阻塞主线程？用 `.flatMap(id -> dbRepo.findById(id))`。

### B. 强大的“背压” (Backpressure)
这是 Reactive 编程最厉害的地方。
**场景**：MQTT 瞬间涌入 10,000 条消息，但你的数据库每秒只能写 500 条。
*   **普通 Java (List/Queue)**：内存直接爆掉 (OOM)。
*   **Reactor**：下游会告诉上游“慢点发，我处理不过来了”。它会自动根据消费能力调整上游的读取速度，或者按照策略丢弃/缓冲数据。这就是 Flink 这种引擎的核心能力，Reactor 在单机上也完美实现了。

### C. 真正的“非阻塞” (Non-blocking)
如果你的 enrich 过程需要查数据库（I/O 操作）。
*   **普通循环**：线程卡住等待数据库返回。1000 个请求就要 1000 个线程，CPU 上下文切换爆炸。
*   **Reactor**：线程发起查询后立刻去处理下一条消息。当数据库返回时，再回调回来继续处理。**一个线程就能处理成千上万并发**。

---

## 3. 实战建议

如果你的项目已经是 **Spring Boot**：
1.  引入 `spring-boot-starter-webflux` (它自带 Reactor)。
2.  不要再写 `while(true) { queue.take() }` 这种原始代码了。
3.  创建一个 `Flux` 或 `Sinks.Many` 作为你的数据总线，把 MQTT 消息 `tryEmitNext` 进去。
4.  然后就可以快乐地写链式 DSL 了。

**结论**：在单机环境，Reactor 提供了 Flink 90% 的编程体验和流控能力，但**运维成本是 0**（因为它只是一个 jar 包依赖）。

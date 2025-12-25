# Scatter-Gather (分散-聚合模式)

当一个业务请求需要从多个独立的数据源或服务获取信息时，通过**并行发起多个子请求**，然后**等待所有结果返回并聚合**，从而将原本串行的多次调用转化为并发执行，大幅缩短总响应时间。

## 1. 痛点：串行调用的"累加延迟"

在微服务或分布式系统中，一个页面可能需要调用多个后端服务：

**灾难场景**：
```
用户请求: 查询"北京到上海"的机票
系统需要查询:
  1. 东航 API (耗时 2 秒)
  2. 国航 API (耗时 2 秒)
  3. 南航 API (耗时 2 秒)
```

**串行调用**：
```java
List<Flight> result = new ArrayList<>();
result.addAll(callEasternAirline());  // 等 2 秒
result.addAll(callAirChina());        // 再等 2 秒
result.addAll(callChinaSouthern());   // 再等 2 秒
return result; // 总耗时: 6 秒
```

**后果**：
*   用户要盯着转圈圈 6 秒才能看到结果。在移动端，这几乎是不可接受的。
*   如果其中一个航司的 API 超时（10 秒），总耗时会变成 14 秒。

**本质问题**：这三个 API 互相独立，没有依赖关系，完全可以同时调用。但串行代码强行让它们排队。

---

## 2. 解决方案：并行扇出，集中汇总

Scatter-Gather 将一个请求"打散"为多个子请求，并发执行，然后"聚合"结果：

### 运行逻辑
```
[Client Request]
    |
    v
[Main Thread] --> (Scatter) 并发发起:
                    +---> [Service A] (2秒)
                    +---> [Service B] (2秒)
                    +---> [Service C] (2秒)
    |
    v
[Wait for All] --> (Gather) 聚合结果
    |
    v
[Return Combined Result]
总耗时: max(2, 2, 2) = 2 秒 (而不是 6 秒)
```

---

## 3. 实现策略

### 方式 A: Java CompletableFuture (推荐)
```java
public List<Flight> searchFlights(String from, String to) {
    CompletableFuture<List<Flight>> task1 = 
        CompletableFuture.supplyAsync(() -> callEasternAirline(from, to));
    
    CompletableFuture<List<Flight>> task2 = 
        CompletableFuture.supplyAsync(() -> callAirChina(from, to));
    
    CompletableFuture<List<Flight>> task3 = 
        CompletableFuture.supplyAsync(() -> callChinaSouthern(from, to));
    
    // 等待所有任务完成
    CompletableFuture<Void> allOf = 
        CompletableFuture.allOf(task1, task2, task3);
    
    allOf.join(); // 阻塞等待
    
    // 聚合结果
    List<Flight> result = new ArrayList<>();
    result.addAll(task1.join());
    result.addAll(task2.join());
    result.addAll(task3.join());
    
    return result;
}
```

### 方式 B: Spring WebFlux (响应式)
```java
public Mono<List<Flight>> searchFlights(String from, String to) {
    Mono<List<Flight>> task1 = webClient.get()
        .uri("http://eastern-airline/flights?from={from}&to={to}", from, to)
        .retrieve().bodyToMono(new ParameterizedTypeReference<List<Flight>>() {});
    
    Mono<List<Flight>> task2 = // 类似 task1
    Mono<List<Flight>> task3 = // 类似 task1
    
    return Mono.zip(task1, task2, task3)
        .map(tuple -> {
            List<Flight> combined = new ArrayList<>();
            combined.addAll(tuple.getT1());
            combined.addAll(tuple.getT2());
            combined.addAll(tuple.getT3());
            return combined;
        });
}
```

---

## 4. 关键挑战

### 挑战 A: 超时控制
如果其中一个服务极慢（10 秒），我们不应该傻等。

**解决方案**：设置超时。
```java
CompletableFuture<List<Flight>> task1 = 
    CompletableFuture.supplyAsync(() -> callEasternAirline(from, to))
        .orTimeout(3, TimeUnit.SECONDS) // 超过 3 秒自动失败
        .exceptionally(ex -> {
            log.warn("东航 API 超时");
            return Collections.emptyList(); // 返回空列表，不影响其他结果
        });
```

### 挑战 B: 部分失败处理
如果 3 个服务中有 1 个挂了，是全部失败还是返回部分结果？
*   **策略 1（严格）**：只要有一个失败，整个请求失败。
*   **策略 2（宽容,推荐）**：收集成功的结果，失败的服务跳过。用户至少能看到 2/3 的数据。

---

## 5. 性能优化建议

*   **线程池配置**：`CompletableFuture.supplyAsync()` 默认使用 `ForkJoinPool.commonPool()`。对于 IO 密集型任务（HTTP 调用），建议使用自定义线程池。
    ```java
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CompletableFuture.supplyAsync(() -> callAPI(), executor);
    ```
*   **连接复用**：使用 `RestTemplate` 或 `WebClient` 时，确保开启连接池，避免每次请求都建立新的 TCP 连接。

---

## 6. 适用场景

*   **机票/酒店比价网站**。
*   **搜索引擎 (Google)**：并发查询网页索引、图片索引、新闻索引，然后聚合展示。
*   **数据大屏**：一个页面需要从 10 个微服务拉取不同的统计数据。
*   **推荐系统**：并发调用多个推荐算法，聚合结果后排序。

---

## 7. 与 BFF 的关系

*   **BFF (Backend for Frontend)**: 为特定前端定制接口，负责数据裁剪和聚合。
*   **Scatter-Gather**: BFF 内部实现聚合的一种技术手段。
*   **组合使用**：前端调用 BFF 的 `/search` 接口，BFF 内部用 Scatter-Gather 并发调用多个微服务。

## 8. 总结
Scatter-Gather 是将 **"排队买票"** 变成 **"多窗口同时办理"** 的效率革命。
*   **信条**：能并发的绝不串行，能异步的绝不同步。
*   **心法**：总耗时 = 最慢的那个子任务耗时，而不是所有任务耗时之和。

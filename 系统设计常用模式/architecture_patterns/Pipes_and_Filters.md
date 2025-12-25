# Pipes and Filters (管道过滤器模式)

一种将复杂的数据处理任务分解为一系列独立、可复用的处理单元（Filter），并通过管道（Pipe）串联起来的流式处理模式。它广泛应用于 ETL、日志处理、图像处理等领域。

## 1. 痛点：庞大单体的数据处理逻辑

在没有管道过滤器时，数据处理往往是一个巨大的单体函数：

**灾难场景**：
```java
public void processLog(String rawLog) {
    // 1. 解析 JSON
    LogEntry log = parseJson(rawLog);
    
    // 2. 规范化时间格式
    log.timestamp = normalizeTimestamp(log.timestamp);
    
    // 3. 过滤敏感词
    log.message = filterKeywords(log.message);
    
    // 4. IP 归属地识别
    log.location = getLocation(log.ip);
    
    // 5. 聚合统计
    updateStatistics(log);
    
    // 6. 存入数据库
    save(log);
}
```

**问题爆发**：
1.  **难以复用**：如果另一个场景也需要"过滤敏感词"，你必须把代码复制一份。
2.  **难以扩展**：新增一个"用户画像标注"步骤，必须改这个巨大的函数。
3.  **难以并行**：所有步骤都在一个线程里串行执行，无法利用多核。
4.  **难以测试**：测试"过滤敏感词"功能时，必须走完整的处理流程。

**本质问题**：所有处理逻辑耦合在一个函数里，违反了"单一职责原则"。

---

## 2. 解决方案：流水线式处理

将处理逻辑拆分为多个独立的 Filter，通过 Pipe 连接：

### 运行流程
```
[原始日志]
    | (Pipe: Kafka Topic)
    v
[Filter 1: JSON 解析器]
    | (Pipe: 内存队列 / Kafka)
    v
[Filter 2: 时间规范化]
    | (Pipe)
    v
[Filter 3: 敏感词过滤]
    | (Pipe)
    v
[Filter 4: IP 归属地]
    | (Pipe)
    v
[Filter 5: 存储]
```

### 核心概念
*   **Filter (过滤器)**：一个独立的处理单元。只做一件事，接收输入，产出输出。
*   **Pipe (管道)**：连接两个 Filter 的数据通道（可以是内存队列、Kafka Topic、Unix Pipe）。

---

## 3. 实现策略

### 方式 A: 同进程内流式处理 (Java Stream API)
```java
public class LogProcessor {
    public void process(List<String> rawLogs) {
        rawLogs.stream()
            .map(this::parseJson)           // Filter 1
            .map(this::normalizeTime)       // Filter 2
            .map(this::filterKeywords)      // Filter 3
            .map(this::enrichLocation)      // Filter 4
            .forEach(this::save);           // Filter 5 (terminal)
    }
}
```

*   **优点**：简单直接，适合单机处理。
*   **缺点**：无法跨进程，扩展性差。

### 方式 B: 分布式流处理 (Apache Kafka + Kafka Streams)
```java
// Filter 1: 解析器（独立微服务）
@KafkaListener(topics = "raw-logs")
public void parseLog(String rawLog) {
    LogEntry parsed = JSON.parse(rawLog);
    kafkaTemplate.send("parsed-logs", parsed);
}

// Filter 2: 规范化器（另一个独立微服务）
@KafkaListener(topics = "parsed-logs")
public void normalize(LogEntry log) {
    log.timestamp = normalize(log.timestamp);
    kafkaTemplate.send("normalized-logs", log);
}

// ...以此类推
```

*   **优点**：每个 Filter 可以独立部署、独立扩容、用不同语言实现。
*   **缺点**：架构复杂，消息延迟增加。

### 方式 C: 流式计算框架 (Apache Flink / Spark Streaming)
```java
DataStream<String> rawLogs = env.addSource(new FlinkKafkaConsumer<>(...));

rawLogs
    .map(new JsonParser())        // Filter 1
    .map(new TimeNormalizer())    // Filter 2
    .filter(new KeywordFilter())  // Filter 3
    .map(new IPEnricher())        // Filter 4
    .addSink(new DatabaseSink()); // Filter 5
```

*   **优点**：高性能、支持状态管理、窗口聚合、精确一次语义。
*   **适用**：大数据实时处理场景。

---

## 4. 核心优势

1.  **可复用性 (Reusability)**：每个 Filter 是独立模块，可以被多条 Pipeline 复用。
2.  **可组合性 (Composability)**：像搭积木一样组装 Filter，灵活应对不同需求。
3.  **可测试性 (Testability)**：每个 Filter 可以单独测试（输入 Mock 数据，验证输出）。
4.  **可并行性 (Parallelism)**：不同的 Filter 可以运行在不同的线程/进程/机器上。
5.  **容错性 (Fault Tolerance)**：某个 Filter 挂了，不影响其他 Filter。可以重启单个 Filter。

---

## 5. 关键设计原则

### 原则 A: Filter 必须无状态或状态外部化
*   **错误做法**：Filter 内部维护一个 `count` 变量统计处理数量。
*   **后果**：如果 Filter 重启，count 清零，统计不准。
*   **正确做法**：把 count 存到 Redis 或数据库。

### 原则 B: 幂等性
由于网络抖动，一条消息可能被重复处理。Filter 必须能处理重复输入。

### 原则 C: 单一职责
每个 Filter 只做一件事。不要在"解析器"里顺便做"过滤"。

---

## 6. 注意事项与挑战

*   **调试困难**：一条数据经过 10 个 Filter，中间某个 Filter 出错了，很难追踪问题。
    *   **解决方案**：给每条数据加唯一 ID，并在每个 Filter 打日志。
*   **端到端延迟**：数据要经过多个 Pipe，总延迟 = 各 Filter 延迟之和 + 各 Pipe 传输延迟。
*   **背压 (Backpressure)**：如果某个 Filter 处理速度慢，会导致上游 Pipe 积压。
    *   **解决方案**：使用支持背压的 Pipe（如 Akka Streams、Reactive Streams）。

---

## 7. 适用场景

*   **ETL 数据处理**：从数据源抽取 -> 转换 -> 加载到数仓。
*   **日志分析**：原始日志 -> 解析 -> 过滤 -> 聚合 -> 告警。
*   **图像处理**：上传图片 -> 压缩 -> 水印 -> 格式转换 -> 存储。
*   **视频转码**：原始视频 -> 解码 -> 调整码率 -> 编码 -> 分片切片。
*   **实时推荐**：用户行为 -> 特征提取 -> 召回 -> 排序 -> 个性化。

---

## 8. 与责任链模式的区别

| 对比维度 | Pipes and Filters        | Chain of Responsibility (责任链) |
| :------- | :----------------------- | :------------------------------- |
| 目的     | 数据变换（Transform）    | 请求处理（Handle）               |
| 数据流   | 每个 Filter 都处理并传递 | 某个 Handler 处理后可能就终止    |
| 关注点   | **数据处理**             | **业务决策**                     |
| 典型场景 | ETL、流式计算            | 审批流程、权限校验               |

## 9. 总结
Pipes and Filters 是数据处理的 **"工业流水线"**。
*   **信条**：把大任务拆成小任务，每个工位只干一件事。
*   **心法**：通过组合简单的 Filter，构建出复杂而灵活的数据处理系统。

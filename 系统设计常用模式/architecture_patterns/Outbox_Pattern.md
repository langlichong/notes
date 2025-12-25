# Transactional Outbox Pattern (事务性发件箱模式)

在分布式系统中，解决 **“数据库事务与消息发送一致性”** 问题的终极解决方案。

## 1. 痛点：著名的“双写一致性”问题

在微服务开发中，我们经常遇到这样的代码逻辑：

```java
@Transactional
void placeOrder(Order order) {
    // 1. 写数据库 (DB Transaction)
    orderRepository.save(order);
    
    // 2. 发消息给 Kafka (Network IO)
    kafka.send("order-created", order);
}
```

这里存在一个无法通过简单的代码顺序解决的死局（Dual Write Problem）：

*   **情况 A (先存库后发消息)**：
    *   数据库事务提交成功了。
    *   **但在发消息前一瞬间**，进程挂了，或者网络超时导致 Kafka 发送失败。
    *   **后果**：数据库里有订单，但下游系统（如库存、积分）永远收不到消息，导致**数据不一致**。
*   **情况 B (先发消息后存库/提交事务)**：
    *   Kafka 消息发送成功了。
    *   **但在提交事务时**，数据库挂了或者因为违反约束回滚了。
    *   **后果**：数据库没订单，但下游系统却收到了“订单已创建”的消息，导致**空头支票**。

**核心矛盾**：数据库事务（JDBC）仅限于本地 DB，无法覆盖到外部的网络调用（Kafka）。我们缺乏高性能的全局分布式事务（如 XA/2PC）。

---

## 2. Outbox 解决方案：把“发消息”变成“写数据库”

Outbox 模式的核心思想是：**与其试图跨网络做原子操作，不如把所有操作都限制在同一个本地数据库事务中。**

### 第一步：写库 (The "Outbox")

我们在业务表所在的同一个数据库中，创建一张专门的 **Outbox 表**（发件箱表）。

在同一个 `@Transactional` 方法里：
1.  保存业务数据（Orders 表）。
2.  保存消息数据到 **Outbox 表**。

```java
@Transactional
void placeOrder(Order order) {
    // 1. 业务操作
    orderRepository.save(order);
    
    // 2. 存入发件箱 (Outbox)
    // 关键点：这一步和上一步是在同一个 JDBC 事务里的！
    // 只要事务提交，业务数据和消息数据要么同时成功，要么同时失败。
    // 这就保证了 100% 的原子性。
    outboxRepository.save(new OutboxEvent("order-created", toJson(order)));
}
```

### 第二步：搬运 (The "Relay")

现在消息已经安全地躺在数据库里了。我们需要一个独立的异步进程（Relay），负责把 Outbox 表里的数据搬运到消息中间件（Kafka/RabbitMQ）。

通常有两种搬运策略：

#### 策略 A: 轮询 (Polling Publisher) ——最简单
*   **实现**：写一个定时任务（比如每秒执行一次）。
*   **逻辑**：
    1.  `SELECT * FROM outbox WHERE status = 'PENDING'`
    2.  发送给 Kafka。
    3.  发送成功后，`UPDATE outbox SET status = 'SENT'`（或直接删除）。
*   **优点**：实现简单，不依赖特殊数据库功能。
*   **缺点**：有延迟（取决于轮询间隔），频繁轮询对数据库有压力。

#### 策略 B: 事务日志挖掘 (CDC / Transaction Log Tailing) ——最强大
*   **实现**：使用 **Debezium** 或 **Canal** 等 CDC 工具。
*   **逻辑**：
    1.  工具伪装成数据库的 Slave，监听数据库的 **Binlog (MySQL)** 或 **WAL (Postgres)**。
    2.  一旦监测到 Outbox 表有 `INSERT` 操作，立即自动抓取。
    3.  推送到 Kafka。
*   **优点**：实时性极高，不给数据库增加查询压力，此时应用本身甚至不需要写 Relay 代码。

---

## 3. Spring Modulith 的原生支持

手动实现 Outbox 模式（建表、写定时任务）比较繁琐。好消息是，**Spring Modulith 原生内置了 Outbox 模式的支持**（官方称为 Event Externalization）。

### 如何使用
1.  **引入依赖**：加入 `spring-modulith-starter-jdbc`（或其他持久层支持）。
2.  **配置开启**：在 `application.properties` 中开启相关配置。
3.  **代码编写**：
    *   你依然只需要在业务代码里调用 `ApplicationEventPublisher.publishEvent(event)`。
    *   Spring Modulith 会**自动拦截**这个事件，发现事务未提交，就会把它序列化并存入 `EVENT_PUBLICATION` 表（框架自动维护这张表）。
4.  **自动搬运**：
    *   Spring Modulith 会在后台启动线程，自动把表里的事件发送给配置好的 Message Broker（Kafka/RabbitRMQ）。
    *   如果发送失败，它会自动重试。

### 总结
Outbox 模式是微服务数据一致性的基石。

> **任何涉及“不仅要存库，还要通知别人”的关键业务（特别是涉及资金、核心状态流转），都应该使用 Outbox 模式，而不是直接在业务代码中调用消息中间件 API。**
1.  **一致性保障**：确保了“业务操作”与“消息发送”的最终一致性。
2.  **解耦**：业务代码不再需要关心 Kafka 是否连得上，只需关心数据库事务。

# Publisher-Subscriber (发布-订阅模式)

事件驱动架构的核心模式。通过在服务之间引入一个事件总线（Event Bus / Message Broker），将消息的发布者与订阅者完全解耦，实现"一对多"的异步通信和服务间的松耦合协作。

## 1. 痛点：服务间的"强耦合调用链"

在没有发布订阅模式时，服务间通过直接 RPC 调用通信：

**灾难场景**：
```java
@Service
public class OrderService {
    @Autowired InventoryService inventoryService;
    @Autowired PointsService pointsService;
    @Autowired SmsService smsService;
    
    public void completeOrder(Order order) {
        order.setStatus("PAID");
        orderRepository.save(order);
        
        // 直接调用 3 个下游服务
        inventoryService.shipGoods(order.getId());      // 发货
        pointsService.addPoints(order.getUserId(), 100); // 加积分
        smsService.sendNotification(order.getUserId());  // 发短信
    }
}
```

**问题爆发**：
1.  **紧耦合**：Order Service 必须显式依赖 3 个下游服务。如果新增"发送 App 推送"需求，必须改 Order 的代码。
2.  **单点故障**：如果 SmsService 挂了（超时），整个 `completeOrder` 方法会卡住或报错，影响主流程。
3.  **性能瓶颈**：3 个调用是串行的（即使可以并行，也需要手动写 `CompletableFuture`）。
4.  **事务边界模糊**：如果积分服务失败，订单状态是否要回滚？如果要回滚，就需要复杂的分布式事务。

**本质问题**：Order Service 不应该"知道"有谁对"订单完成"这个事件感兴趣。它只应该专注于自己的逻辑。

---

## 2. 解决方案：引入事件总线

通过消息中间件（Kafka / RabbitMQ / EventBus）作为中介：

### 运行流程
```
[Order Service (发布者)]
    |
    v (发布事件: ORDER_COMPLETED)
[Event Bus / Kafka]
    |
    +---> [Inventory Service (订阅者)] -> 发货
    +---> [Points Service (订阅者)]    -> 加积分
    +---> [SMS Service (订阅者)]       -> 发短信
    +---> [App Push Service (新增，无需改 Order)] -> 推送
```

### 代码改造
**发布者 (Order Service)**：
```java
@Service
public class OrderService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;  // Spring 内置
    
    public void completeOrder(Order order) {
        order.setStatus("PAID");
        orderRepository.save(order);
        
        // 发布事件（不关心谁在监听）
        eventPublisher.publishEvent(new OrderCompletedEvent(order.getId(), order.getUserId()));
    }
}
```

**订阅者 (Points Service)**：
```java
@Service
public class PointsEventListener {
    
    @EventListener  // Spring 注解
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("收到订单完成事件: {}", event.getOrderId());
        pointsService.addPoints(event.getUserId(), 100);
    }
}
```

**订阅者 (SMS Service)** - 类似实现。

---

## 3. 实现策略

### 方式 A: 应用内事件总线 (Spring Events)
*   **适用**：单体应用或同一个 JVM 内的模块解耦。
*   **特点**：默认同步（可通过 `@Async` 改为异步）。
*   **限制**：无法跨进程，事件不持久化。

### 方式 B: 分布式消息队列 (Kafka / RabbitMQ)
*   **适用**：微服务架构。
*   **特点**：异步、持久化、可重放、跨语言。
*   **实现**：
    ```java
    @Service
    public class OrderService {
        @Autowired KafkaTemplate<String, String> kafkaTemplate;
        
        public void completeOrder(Order order) {
            orderRepository.save(order);
            
            kafkaTemplate.send("order-events", 
                new OrderCompletedEvent(order).toJson());
        }
    }
    
    @KafkaListener(topics = "order-events", groupId = "points-service")
    public void consume(String message) {
        OrderCompletedEvent event = parse(message);
        pointsService.addPoints(event.getUserId(), 100);
    }
    ```

### 方式 C: Spring Cloud Stream (抽象层)
*   **好处**：屏蔽底层 MQ 差异（Kafka/RabbitMQ 切换只改配置，不改代码）。

---

## 4. 核心优势

1.  **完全解耦**：发布者不知道有谁在订阅。新增订阅者时，**发布者代码零改动**。
2.  **异步非阻塞**：发布事件后立即返回，不等待处理结果。提升响应速度。
3.  **容错性强**：某个订阅者挂了，不影响其他订阅者和发布者。
4.  **易于扩展**：新增业务需求（如"订单完成后发优惠券"），只需加一个新的订阅者。

---

## 5. 关键挑战

### 挑战 A: 消息顺序
*   Kafka 可以通过 Partition Key 保证同一个 Key 的消息顺序。
*   RabbitMQ 需要配置单一消费者或使用 Priority Queue。

### 挑战 B: 消息丢失
*   **解决方案**：使用 **Outbox 模式**。先把事件写入数据库（与业务在同一事务），再由 CDC 工具推送到 MQ。

### 挑战 C: 重复消费
*   **解决方案**：订阅者必须实现**幂等性处理**（如记录已处理的事件 ID）。

### 挑战 D: 调试困难
*   **问题**：事件驱动是异步的。订单完成了但积分没加，很难追踪问题在哪个环节。
*   **解决方案**：引入 **分布式链路追踪**（如 SkyWalking / Jaeger），给每个事件加 Trace ID。

---

## 6. 与直接调用的对比

| 对比维度   | 直接 RPC 调用            | 发布-订阅模式              |
| :--------- | :----------------------- | :------------------------- |
| 耦合度     | 强（调用方需知道被调方） | 弱（发布者不知道订阅者）   |
| 响应速度   | 快（同步）               | 慢（异步，有延迟）         |
| 容错性     | 差（一方挂全挂）         | 强（订阅者挂不影响发布者） |
| 事务一致性 | 强（ACID 或分布式事务）  | 弱（最终一致性）           |
| 适用场景   | 实时查询                 | 状态变更通知、日志、审计   |

---

## 7. 适用场景

*   **状态变更广播**：订单支付成功、用户注册、商品上架。
*   **日志与审计**：所有关键操作发布事件，审计服务统一订阅并记录。
*   **跨域协作**：DDD 中不同 Bounded Context 之间通过事件通信。
*   **数据同步**：主数据库变更后，通过事件同步到搜索引擎（ES）、缓存（Redis）。

## 8. 总结
Publisher-Subscriber 是微服务解耦的 **"无线电广播"**。
*   **信条**：我只管喊，谁爱听谁听。
*   **心法**：用"事件"替代"调用"，用"最终一致性"换取"高可用性"。

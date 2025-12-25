# Database per Service (数据库隔离模式)

微服务架构的核心原则之一。要求每个微服务拥有且仅拥有自己的私有数据库（或 Schema），其他服务**绝对禁止**通过 SQL 直接访问它的表。跨服务数据交互必须通过 API 或事件完成。

## 1. 痛点：共享数据库的"隐形耦合"

当多个微服务共享同一个数据库时：

**灾难场景**：
1.  **表结构耦合**：Order Service 和 Inventory Service 都直接读写 `products` 表。
    *   某天 Inventory 团队给 `products` 加了个字段 `warehouse_id`。
    *   Order 团队不知道。他们的查询因为没处理这个字段而报错。
2.  **锁竞争**：Order Service 在做大促结算（写数据）。Inventory Service 在做库存盘点（读数据）。它们在同一个数据库上互相抢 CPU 和锁，导致双双卡死。
3.  **无法独立扩容**：Inventory 的数据量暴涨需要分库分表。但 Order 还在用同一个 DB。没法单独改 Inventory 的表结构。
4.  **技术栈锁定**：所有服务被迫使用同一种数据库（都是 MySQL）。即使某个服务更适合用 MongoDB，也没法换。

**本质问题**：共享数据库让微服务在数据层失去了"自治权"。这违背了微服务的初衷。

---

## 2. 解决方案：数据库主权独立

每个服务独占自己的数据存储：

### 核心原则
1.  **私有化 (Private)**：`Order Service` 拥有 `order_db`。其他服务**严禁**直接连接这个库。
2.  **异构化 (Heterogeneous)**：
    *   Order 用 PostgreSQL（强事务）。
    *   Product Catalog 用 MongoDB（文档灵活）。
    *   Session 用 Redis（内存高速）。
3.  **API 唯一入口 (API Only)**：如果 Inventory 需要知道某个订单的状态，它必须调用 Order Service 的 HTTP API，而不是去查 `order_db`。

---

## 3. 跨服务数据一致性问题

这是该模式最大的代价：**放弃了跨库 JOIN 和分布式事务**。

### 挑战与解决方案
*   **场景**：创建订单时，需要同时扣减库存。两个操作分别在不同的数据库。
*   **错误做法**：用 XA 分布式事务（2PC）。性能极差且复杂。
*   **正确做法**：使用 **Saga 模式** 或 **Event-Driven 最终一致性**。
    *   Order Service 创建订单 -> 发事件 `ORDER_CREATED`。
    *   Inventory Service 监听事件 -> 扣减库存。
    *   如果扣减失败 -> 发事件 `INVENTORY_INSUFFICIENT`。
    *   Order Service 监听后 -> 取消订单（补偿）。

---

## 4. 数据冗余是必然的

为了避免频繁跨服务调用，服务之间会有意冗余少量数据：

*   **示例**：Order Service 虽然不该存产品的详细描述，但可以存一个 `product_name` 快照（用于展示订单）。
*   **同步策略**：通过订阅 Product Service 的 `PRODUCT_UPDATED` 事件来更新这个快照。

---

## 5. 实现指南

### 1. 物理隔离
*   **方式 A（推荐）**: 每个服务一个独立的数据库实例。
*   **方式 B（成本低）**: 同一个 DB 实例，但不同的 Schema。通过数据库权限控制禁止跨 Schema 访问。

### 2. 服务启动时的数据库初始化
每个服务自带数据库迁移脚本（如 Flyway / Liquibase）。服务启动时自动创建表结构。

---

## 6. 常见挑战

*   **报表查询**：BI 团队想做跨 10 个服务的联合报表。由于数据分散，无法一条 SQL 搞定。
    *   **解决方案**：建立专门的 **数据仓库**，通过 CDC (Canal / Debezium) 将各服务数据同步到 OLAP 数据库（如 ClickHouse）。
*   **开发期调试困难**：开发人员本地要起 10 个数据库容器。
    *   **解决方案**：使用 Docker Compose 一键启动全套环境。

## 7. 总结
Database per Service 是微服务 **"服务自治"** 的物理保障。
*   **信条**：我的数据我做主，你想要数据，请走 API。
*   **代价**：放弃了传统 RDBMS 的便利（JOIN、全局事务），换取了团队独立演进的自由和系统的弹性扩展能力。

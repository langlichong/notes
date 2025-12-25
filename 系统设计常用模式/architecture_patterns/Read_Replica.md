# Read Replica (读写分离模式)

通过建立数据库主从架构（Master-Slave Replication），将 **"写操作"** 全部导向主库（Master），将 **"读操作"** 分散到多个从库（Slave/Replica），从而实现读性能的线性扩展和系统整体吞吐量的倍增。

## 1. 痛点：读占用 95%，却和写抢同一份资源

在绝大多数 Web 应用中，读写比例严重失衡：

**典型场景**：
1.  **新闻网站**：每天发布 100 篇文章（写），但有 1000 万次页面浏览（读）。读写比 = **10 万 : 1**。
2.  **电商商品详情页**：商家修改商品描述（写）一天 10 次，用户查看商品（读）一天 100 万次。
3.  **社交媒体**：用户发帖（写）占 5%，刷 Feed 流（读）占 95%。

**单库困境**：
*   所有读请求和写请求共享同一个数据库的 CPU、连接池、磁盘 IO。
*   写操作由于需要加锁、维护索引、写 Binlog，天生比读慢得多。
*   **结果**：1000 万次读查询把数据库的连接池占满，导致写操作也被拖慢甚至超时。

---

## 2. 解决方案：主写从读，各司其职

### 架构设计
```
[Application]
    |
    +--- Write Requests ---> [Master DB] (唯一写入点)
    |                            |
    +--- Read Requests  -----+   | (Async Replication via Binlog)
                             |   v
                             +---> [Slave 1]
                             +---> [Slave 2]
                             +---> [Slave 3]
```

### 核心逻辑
1.  **Master (主库)**：只处理 `INSERT`, `UPDATE`, `DELETE`。
2.  **Slave (从库)**：接收 Master 通过 Binlog 同步过来的数据变更，只处理 `SELECT`。
3.  **负载均衡**：读请求按轮询或权重分配到 3 个从库。

---

## 3. 实现策略

### 方式 A: 代码层面硬编码（不推荐）
```java
@Service
public class OrderService {
    @Autowired DataSource masterDataSource;
    @Autowired DataSource slaveDataSource;
    
    public void createOrder(Order order) {
        // 写操作
        masterDataSource.execute("INSERT INTO orders ...");
    }
    
    public Order getOrder(Long id) {
        // 读操作
        return slaveDataSource.query("SELECT * FROM orders WHERE id = ?", id);
    }
}
```
*   **缺点**：代码侵入性强，容易出错。

### 方式 B: 中间件自动路由（推荐）
使用 **ShardingSphere-JDBC** 或 **MyCAT**：
*   开发者只写一份代码，访问一个逻辑数据源。
*   中间件自动识别 SQL 类型：
    *   `SELECT` -> 发往 Slave。
    *   `INSERT/UPDATE/DELETE` -> 发往 Master。

---

##4. 关键挑战：主从延迟（Replication Lag）

主从复制是**异步**的。Master 写入后，需要几毫秒到几秒才能同步到 Slave。

**问题场景**：
1.  **写后立刻读**：用户刚提交了订单（写 Master）。
2.  页面立刻跳转到"订单详情页"（读 Slave）。
3.  由于主从延迟，从库还没同步到这条订单。
4.  **用户看到**："您的订单不存在"。😱

### 解决方案
*   **强制读主库 (Read Your Own Writes)**：对于刚写完的数据，强制从 Master 读取。
    ```java
    @Transactional(readOnly = false) // 强制走主库
    public Order getOrderAfterCreate(Long id) { ... }
    ```
*   **延迟提示**：在页面显示"数据正在同步中，请稍候...（1-3秒）"。
*   **缓存层 (Cache Aside)**：写完后直接写缓存。读时先查缓存，避开数据库。

---

## 5. 从库数量的选择

*   **起步**：1 主 1 从。读性能提升约 50%。
*   **中等规模**：1 主 3 从。读性能提升约 200%。
*   **大规模**：1 主 10+ 从 (Amazon Aurora 可以支持 15 个只读副本)。

**注意**：从库越多，主库同步 Binlog 的网络开销越大。通常不超过 10 个。

---

## 6. 与 CQRS 的关系

*   **读写分离**：物理层面的优化（同一个数据模型，但分了两个库）。
*   **CQRS**：逻辑层面的分离（写模型和读模型可能完全不同）。
*   **组合使用**：写模型用 Master MySQL，读模型用 Slave Elasticsearch。

## 7. 总结
读写分离是数据库性能优化的 **"第一道门槛"**。
*   **适用**：所有读多写少的系统（90% 的互联网应用）。
*   **心法**：主库贵如油，从库多如狗。给写操作留好资源，让读操作尽情奔跑。

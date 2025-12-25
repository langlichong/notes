# Index Table Pattern (索引表模式)

通过创建专门的索引表来优化非主键字段的查询性能。它将查询条件字段作为主键，存储指向主表记录的引用，从而将全表扫描转化为高效的索引查找。

## 1. 痛点：非主键查询的"全表扫描噩梦"

在关系型数据库中，主键查询极快（O(log N)），但非主键查询可能极慢：

**灾难场景**：
```sql
-- 订单表：1 亿行记录
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,           -- 主键，有聚簇索引
    user_id BIGINT,
    phone VARCHAR(20),               -- 用户手机号
    order_time DATETIME,
    ...
);

-- 业务查询：根据手机号查订单
SELECT * FROM orders WHERE phone = '13800138000';
```

**性能分析**：
*   **执行计划**：Full Table Scan (全表扫描)。
*   **耗时**：需要扫描 1 亿行数据，耗时 **5-10 秒**。
*   **CPU 爆表**：如果 100 个用户同时查询，数据库直接卡死。

**简单方案**：给 `phone` 字段建普通索引。
```sql
CREATE INDEX idx_phone ON orders(phone);
```
*   **问题 A**：如果查询条件是 `phone + status + create_time` 的组合，单个索引不够用，需要建立多个组合索引，索引膨胀。
*   **问题 B**：写入性能下降（每次 INSERT 要更新多个索引）。

---

## 2. 解决方案：独立的索引表

创建一个专门的表，以查询字段为主键：

### 设计方案
```sql
-- 主表（不变）
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    phone VARCHAR(20),
    ...
);

-- 索引表（新建）
CREATE TABLE order_phone_index (
    phone VARCHAR(20) PRIMARY KEY,   -- 查询字段作为主键
    order_ids JSON                   -- 存储该手机号对应的所有订单ID列表
    -- 或者设计为一对多：(phone, order_id) 复合主键
);
```

### 查询流程
```sql
-- 1. 先查索引表（主键查询，极快）
SELECT order_ids FROM order_phone_index WHERE phone = '13800138000';
-- 结果：[12345, 67890, 99999]

-- 2. 再批量查主表（主键 IN 查询，也很快）
SELECT * FROM orders WHERE id IN (12345, 67890, 99999);
```

*   **总耗时**：从 5 秒降到 **50ms**。
*   **原理**：两次主键查询，都走索引，避免了全表扫描。

---

## 3. 实现策略

### 策略 A: 应用层维护（推荐）
在订单创建/更新时，应用代码同步维护索引表：
```java
@Transactional
public void createOrder(Order order) {
    // 1. 插入主表
    orderRepository.save(order);
    
    // 2. 更新索引表
    PhoneIndex index = phoneIndexRepository.findById(order.getPhone())
        .orElse(new PhoneIndex(order.getPhone()));
    index.addOrderId(order.getId());
    phoneIndexRepository.save(index);
}
```

### 策略 B: 数据库触发器（自动化）
```sql
CREATE TRIGGER after_order_insert
AFTER INSERT ON orders
FOR EACH ROW
BEGIN
    INSERT INTO order_phone_index (phone, order_ids)
    VALUES (NEW.phone, JSON_ARRAY(NEW.id))
    ON DUPLICATE KEY UPDATE 
        order_ids = JSON_ARRAY_APPEND(order_ids, '$', NEW.id);
END;
```

### 策略 C: 异步同步（最终一致性）
*   订单写入主表后，发送事件到 Kafka。
*   后台 Worker 消费事件，更新索引表。
*   **优点**：写入性能最高（无同步等待）。
*   **缺点**：查询可能有延迟（几秒内）。

---

## 4. 扩展：支持复杂查询

**需求**：查询"某个地区 + 某个时间段 + 某个商品类目"的订单。

**方案**：构建复合索引表。
```sql
CREATE TABLE order_composite_index (
    region VARCHAR(50),
    date DATE,
    category VARCHAR(50),
    order_ids JSON,
    PRIMARY KEY (region, date, category)
);
```

**查询**：
```sql
SELECT order_ids FROM order_composite_index 
WHERE region = 'Beijing' AND date = '2023-12-25' AND category = 'Electronics';
```

---

## 5. 注意事项与挑战

### 挑战 A: 一致性维护
如果订单删除了，索引表也必须同步删除对应的 ID。否则会查到不存在的记录。
*   **解决方案**：使用数据库事务确保主表和索引表的操作原子性。

### 挑战 B: 存储空间翻倍
索引表会占用额外的磁盘空间。
*   **评估**：如果索引表只存 ID（BIGINT 8字节），空间增长可控。

### 挑战 C: 热点数据
如果某个手机号有 10 万个订单，`order_ids` 这个 JSON 会非常大。
*   **解决方案**：改用一对多表结构（phone, order_id 两个字段），而不是 JSON 数组。

---

## 6. 与数据库索引的对比

| 对比维度 | 数据库普通索引       | Index Table 模式               |
| :------- | :------------------- | :----------------------------- |
| 创建方式 | `CREATE INDEX`       | 手动建表 + 应用维护            |
| 查询性能 | 好                   | 优秀（双主键查询）             |
| 写入性能 | 每个索引都有写入开销 | 可选异步，灵活                 |
| 灵活性   | 受数据库限制         | 完全自定义（如存 JSON / 分片） |
| 跨库支持 | 仅支持单表           | 可跨数据库（索引表在 Redis）   |

---

## 7. 适用场景

*   **电商订单**：按手机号、身份证号、物流单号查询。
*   **社交媒体**：按话题标签、@用户、地理位置查询帖子。
*   **日志系统**：按 TraceID、UserID、IP 查询日志。
*   **IoT 设备**：按设备 MAC 地址、SN 序列号查询历史数据。

## 8. 总结
Index Table 是数据库性能优化的 **"空间换时间"** 经典案例。
*   **信条**：与其让数据库疯狂扫描，不如提前算好答案。
*   **心法**：查询字段变主键，全表扫描变索引查找。

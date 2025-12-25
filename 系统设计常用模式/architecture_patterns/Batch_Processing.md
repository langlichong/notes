# Batch Processing Pattern (批量处理模式)

通过将多个单独的操作聚合成一个批次进行处理，从而大幅减少网络往返次数、事务提交次数和系统调用开销，实现性能的数量级提升。它是处理大规模数据操作的基本优化手段。

## 1. 痛点：单条操作的"千刀万剐"

**典型场景**：导入 10 万条订单数据到数据库。

**错误做法（逐条插入）**：
```java
public void importOrders(List<Order> orders) {
    for (Order order : orders) {
        jdbcTemplate.update(
            "INSERT INTO orders (id, user_id, amount) VALUES (?, ?, ?)",
            order.getId(), order.getUserId(), order.getAmount()
        );
    }
}
```

**性能分析**：
*   **网络往返**：10 万次 JDBC 调用 = 10 万次网络往返（假设每次 1ms，总计 100 秒）。
*   **事务提交**：如果开启自动提交（AutoCommit），每条 INSERT 都是一个独立事务，10 万次 `fsync` 刷盘。
*   **索引维护**：每次插入都要更新索引 B+Tree，10 万次随机写入。
*   **锁竞争**：高频的行锁申请与释放。

**总耗时**：**30-60 分钟** ⏰

---

## 2. 解决方案：批量处理

将 10 万条数据分成 N 个批次（如每批 1000 条），每批一次性提交。

### 实现方式 A: JDBC Batch Update
```java
public void importOrdersBatch(List<Order> orders) {
    String sql = "INSERT INTO orders (id, user_id, amount) VALUES (?, ?, ?)";
    
    jdbcTemplate.batchUpdate(sql, orders, 1000, (ps, order) -> {
        ps.setLong(1, order.getId());
        ps.setLong(2, order、getUserId());
        ps.setBigDecimal(3, order.getAmount());
    });
}
```

**优化效果**：
*   **网络往返**：从 10 万次降到 **100 次**（10万 / 1000）。
*   **事务提交**：100 次事务提交（而非 10 万次）。
*   **总耗时**：**2-5 分钟** ⏱️（提速 **10-30 倍**）。

### 实现方式 B: MySQL 多值插入
```sql
INSERT INTO orders (id, user_id, amount) VALUES
    (1, 100, 99.99),
    (2, 101, 199.99),
    ...
    (1000, 1099, 299.99);  -- 一次插入 1000 行
```

```java
public void importWithMultiValue(List<Order> orders) {
    List<List<Order>> batches = partition(orders, 1000);
    
    for (List<Order> batch : batches) {
        StringBuilder sql = new StringBuilder(
            "INSERT INTO orders (id, user_id, amount) VALUES ");
        
        for (int i = 0; i < batch.size(); i++) {
            if (i > 0) sql.append(", ");
            Order o = batch.get(i);
            sql.append(String.format("(%d, %d, %s)", o.getId(), o.getUserId(), o.getAmount()));
        }
        
        jdbcTemplate.update(sql.toString());
    }
}
```

---

## 3. 批次大小选择

**不是越大越好！**

| 批次大小   | 优点                 | 缺点                                 |
| :--------- | :------------------- | :----------------------------------- |
| **100**    | 事务小，锁持有时间短 | 网络往返次数多，总耗时较长           |
| **1000** ✅ | 平衡性能与风险       | **推荐值**（适合大多数场景）         |
| **10000**  | 网络往返最少         | 单个事务太大，可能锁表，内存压力大   |
| **100000** | 理论上最快           | ❌ 几乎必然超时或 OOM，数据库可能挂掉 |

**选择建议**：
*   **小数据量 (< 1万)**：500-1000。
*   **大数据量 (> 10万)**：1000-2000。
*   **数据库性能差**：300-500（降低单次压力）。

---

## 4. 关键挑战与解决方案

### 挑战 A: 部分失败处理
*   **问题**：1000 条数据中，第 500 条违反了唯一约束。整个批次回滚，前 499 条也白插了。
*   **解决方案**：
    ```java
    try {
        jdbcTemplate.batchUpdate(...第 1 批次...);
    } catch (DataIntegrityViolationException e) {
        // 回退到单条插入模式，找出具体是哪条有问题
        for (Order order : batch1) {
            try {
                jdbcTemplate.update("INSERT ...", order);
            } catch (Exception ex) {
                log.error("订单 {} 插入失败: {}", order.getId(), ex.getMessage());
            }
        }
    }
    ```

### 挑战 B: 内存溢出
*   **问题**：一次性加载 10 万条订单到内存，每条 1KB，占用 100MB。如果订单对象复杂，可能导致 OOM。
*   **解决方案**：**流式读取 + 批量写入**。
    ```java
    try (Stream<Order> stream = orderRepository.streamAll()) {
        AtomicInteger count = new AtomicInteger();
        List<Order> batch = new ArrayList<>(1000);
        
        stream.forEach(order -> {
            batch.add(order);
            if (batch.size() == 1000) {
                jdbcTemplate.batchUpdate(..., batch);
                batch.clear();
            }
        });
        
        // 处理最后不满 1000 条的批次
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(..., batch);
        }
    }
    ```

### 挑战 C: 死锁风险
*   **问题**：多个批次并发插入时，可能因为锁的申请顺序不同导致死锁。
*   **解决方案**：
    *   **单线程批量插入**（牺牲并发，换取稳定）。
    *   **预排序**：按主键排序后再批量插入，确保锁的申请顺序一致。

---

## 5. 实现策略

### 策略 A: 数据库批量操作
*   **INSERT**：上面已演示。
*   **UPDATE**：
    ```java
    jdbcTemplate.batchUpdate(
        "UPDATE orders SET status = ? WHERE id = ?",
        updates, 1000, (ps, update) -> {
            ps.setString(1, update.getStatus());
            ps.setLong(2, update.getId());
        }
    );
    ```
*   **DELETE**：
    ```sql
    DELETE FROM orders WHERE id IN (1, 2, 3, ..., 1000);
    ```

### 策略 B: 第三方工具批量导入
*   **MySQL LOAD DATA INFILE**：
    ```sql
    LOAD DATA LOCAL INFILE '/tmp/orders.csv'
    INTO TABLE orders
    FIELDS TERMINATED BY ','
    LINES TERMINATED BY '\n';
    ```
    *   **速度**：最快（绕过 SQL 解析，直接写存储引擎）。
    *   **限制**：需要文件系统访问权限。

---

## 6. 适用场景

*   **ETL 数据导入**：从数据仓库导入到业务系统。
*   **批量操作 API**：邮件群发、批量下单、批量修改库存。
*   **缓存预热**：批量加载数据到 Redis。
*   **日志归档**：批量删除 90 天前的日志数据。
*   **消息队列消费**：一次性消费 100 条消息后批量写库（提高吞吐）。

---

## 7. 与单条操作的对比

| 指标           | 单条操作 (10万条) | 批量操作 (1000条/批) |
| :------------- | :---------------- | :------------------- |
| 网络往返       | 100,000 次        | 100 次               |
| 事务提交       | 100,000 次        | 100 次               |
| 总耗时         | 30-60 分钟        | 2-5 分钟             |
| 性能提升       | 基准              | **10-30 倍** 🚀       |
| 实现复杂度     | 简单              | 中等                 |
| 错误处理复杂度 | 简单              | 较高                 |

## 8. 总结
Batch Processing 是性能优化的 **"聚沙成塔"** 法则。
*   **信条**：网络往返是昂贵的，事务提交是昂贵的，批量是廉价的。
*   **心法**：能批就批，但批需有度。

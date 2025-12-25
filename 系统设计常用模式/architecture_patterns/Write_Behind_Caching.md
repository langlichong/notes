# Write-Behind Caching (写后缓存模式)

一种高性能缓存写入策略。应用程序将数据先写入缓存（如 Redis），立即返回成功，然后由后台异步线程定期将缓存中的数据批量刷新到持久化存储（如数据库），从而大幅降低数据库写入压力。

## 1. 痛点：高频写入的"数据库瓶颈"

在某些业务场景中，写入频率极高：

**灾难场景**：
1.  **游戏排行榜**：100 万在线玩家，每秒产生 10 万次积分变化。
2.  **实时监控大屏**：千万级传感器，每秒上报数据。
3.  **电商秒杀**：瞬时处理 100 万个库存扣减请求。

**如果直接写数据库**：
```java
@Service
public class ScoreService {
    public void updateScore(Long userId, int delta) {
        // 每次调用都写数据库
        jdbcTemplate.update(
            "UPDATE user_scores SET score = score + ? WHERE user_id = ?",
            delta, userId
        );
    }
}
```

**后果**：
*   **DB 连接池爆满**：1000 个连接全部被占用。
*   **磁盘 IO 瓶颈**：每秒 10 万次随机写入，磁盘 IOPS 根本扛不住。
*   **锁竞争**：高频更新同一行数据，行锁冲突严重。
*   **响应延迟**：从 5ms 变成 500ms，用户体验崩溃。

---

## 2. 解决方案：写缓存，延迟刷库

将写操作分为两个阶段：

### 阶段 1: 快速写入缓存（同步）
```java
@Service
public class ScoreService {
    @Autowired RedisTemplate redisTemplate;
    
    public void updateScore(Long userId, int delta) {
        // 直接写 Redis（内存操作，极快）
        redisTemplate.opsForValue().increment("score:" + userId, delta);
        // 立即返回，耗时 < 1ms
    }
}
```

### 阶段 2: 批量刷新数据库（异步）
```java
@Scheduled(fixedRate = 10000)  // 每 10 秒执行一次
public void flushToDatabase() {
    // 1. 从 Redis 获取所有变化的数据
    Set<String> keys = redisTemplate.keys("score:*");
    
    // 2. 批量读取
    List<ScoreUpdate> updates = new ArrayList<>();
    for (String key : keys) {
        Long userId = extractUserId(key);
        Integer score = (Integer) redisTemplate.opsForValue().get(key);
        updates.add(new ScoreUpdate(userId, score));
    }
    
    // 3. 批量写入数据库（一次性提交 1000 条）
    jdbcTemplate.batchUpdate(
        "INSERT INTO user_scores (user_id, score) VALUES (?, ?) " +
        "ON DUPLICATE KEY UPDATE score = ?",
        updates
    );
    
    // 4. 清除已同步的缓存（可选）
    // redisTemplate.delete(keys);
}
```

---

## 3. 性能对比

| 指标         | 直接写 DB        | Write-Behind Caching   |
| :----------- | :--------------- | :--------------------- |
| 单次写入耗时 | 5-10 ms          | < 1 ms                 |
| 数据库 QPS   | 10,000           | 100 (批量写)           |
| DB 压力降低  | 基准             | **降低 100-1000 倍**   |
| 数据丢失风险 | 无（立即持久化） | 有（Redis 挂了丢数据） |

---

## 4. 关键挑战与解决方案

### 挑战 A: 数据丢失风险
*   **问题**：如果Redis在刷库前挂了，10秒内的数据全丢。
*   **解决方案**：
    1.  **Redis AOF 持久化**：开启 `appendfsync everysec`，最多丢 1 秒数据。
    2.  **主从复制**：Redis Sentinel 或 Cluster，故障自动切换。
    3.  **降级策略**：检测到 Redis 异常时，临时切换为直接写 DB。

### 挑战 B: 数据一致性
*   **问题**：用户在缓存里看到积分是 1000，但数据库只有 900（还没刷新）。
*   **解决方案**：
    *   **读写一致性承诺**：查询时也从 Redis 读。
    *   **最终一致性**：允许短暂不一致（适合排行榜、统计等场景）。

### 挑战 C: 缓存热点
*   **问题**：某个明星用户的积分被疯狂修改，这个 Key 的更新频率极高。
*   **解决方案**：使用 **本地内存 + Redis 两级缓存**。先累加本地内存，每秒才同步到 Redis。

---

## 5. 实现策略

### 策略 A: 定时刷新（固定间隔）
*   **适用**：更新频率稳定的场景。
*   **缺点**：如果某 10 秒内没有数据变化，也会空跑一次刷库逻辑。

### 策略 B: 脏标记触发（按需刷新）
```java
// 写入时标记为脏
public void updateScore(Long userId, int delta) {
    redisTemplate.opsForValue().increment("score:" + userId, delta);
    redisTemplate.opsForSet().add("dirty_keys", "score:" + userId);
}

// 定时刷新脏数据
@Scheduled(fixedRate = 5000)
public void flush() {
    Set<String> dirtyKeys = redisTemplate.opsForSet().members("dirty_keys");
    if (dirtyKeys.isEmpty()) return;  // 无数据变化，直接返回
    
    // 刷库...
    redisTemplate.delete("dirty_keys");
}
```

### 策略 C: Write-Behind + Write-Through 混合
*   **高优先级数据**：同步写 DB + 写 Cache (Write-Through)。
*   **低优先级数据**：仅写 Cache，异步刷库 (Write-Behind)。

---

## 6. 与 Write-Through 的对比

| 对比维度     | Write-Through (写穿) | Write-Behind (写后)   |
| :----------- | :------------------- | :-------------------- |
| 写入流程     | 同时写 Cache + DB    | 先写 Cache，异步写 DB |
| 写入延迟     | 高（等待 DB）        | 低（仅等待 Cache）    |
| 数据一致性   | 强一致               | 最终一致              |
| DB 压力      | 高                   | 极低（批量）          |
| 数据丢失风险 | 无                   | 有                    |
| 适用场景     | 金融交易、订单创建   | 统计、排行榜、日志    |

---

## 7. 适用场景

*   **游戏排行榜**：积分、等级、战绩统计。
*   **电商秒杀**：库存扣减（先扣 Redis，定期对账 DB）。
*   **计数器**：网站 PV/UV、点赞数、评论数。
*   **IoT 数据采集**：传感器上报数据聚合。
*   **用户行为日志**：点击流、浏览记录（允许丢失少量数据）。

## 8. 总结
Write-Behind Caching 是用 **"异步化 + 批量化"** 对抗高频写入的利器。
*   **信条**：用户要的是速度，数据库要的是喘息。
*   **心法**：缓存扛写入，数据库管持久化。用时间差换取性能差。

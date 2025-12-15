# TimescaleDB 连续聚合 (Continuous Aggregates)：深度解析

在 PostgreSQL 的世界里，**Continuous Aggregates (连续聚合)** 是 TimescaleDB 贡献的最具革命性的功能之一。它彻底解决了“实时报表查询慢”和“定时脚本维护难”的两难问题。

---

## 1. 核心概念：它是什么？

简单来说，它是 **自动化的、增量的、实时的物化视图**。

*   **普通 PG 物化视图**：是一个静态快照。你必须手动运行 `REFRESH`，此时数据库会把所有数据**重新算一遍**。如果不刷新，数据就是旧的。
*   **连续聚合**：TimescaleDB 会在后台自动跟踪新写入的数据。当新数据到来时，它**只计算新数据**对应的统计结果，并合并到视图中。

---

## 2. 它是如何工作的？(核心机制)

想象一下你需要绘制“每小时温度曲线”。

### A. 分桶 (Bucketing)
TimescaleDB 使用 `time_bucket()` 函数将无限的时间流切分成统一的格子（例如 1 小时一格）。

### B. 双层视图架构 (The Real-Time Layer)
这是它最聪明的设计。当你查询一个连续聚合视图时，你实际上在查询 **两个部分** 的 UNION：

1.  **物化层 (Materialized Layer)**：
    *   存储在磁盘上的、已经计算好的“历史结果”。
    *   例如：从 1 年前到 **1 小时前** 的每小时平均温。这部分数据量极小，查询飞快。
    
2.  **实时层 (Real-Time Layer)**：
    *   直接从原始表（Raw Table）中查询**最近未被物化**的数据。
    *   例如：**最近 1 小时** 刚刚写入、还没来得及运行后台任务的数据。

**查询结果** = `物化历史` (99%数据) + `实时聚合` (1%数据)。
用户对此**完全无感知**。你感觉就像在查询一张实时更新的表，但速度却像在查缓存。

---

## 3. 核心优势

### 1. 永远不需要“全量重算”
普通的视图 `REFRESH MATERIALIZED VIEW` 是 O(N) 操作，数据越多越慢。
连续聚合是 O(Delta) 操作。无论你有 100 亿条历史数据，刷新只处理最近新增的几千条，系统负载极低。

### 2. 自动降采样 (Downsampling)
随着时间推移，你可能不再需要“秒级”数据。
你可以设置保留策略：
*   原始表：保留 7 天（用于查细节）。
*   连续聚合（按小时统计）：保留 5 年（用于看趋势）。
*   **Result**：你在极小的存储成本下，保存了长期的统计特征。

### 3. 数据修正 (Backfilling)
如果在过去的某个时间点（比如昨天），突然插入了一条迟到的数据，或者修正了一个错误数据。
普通的定时脚本（每天凌晨跑昨天的）会漏掉这个修改。
TimescaleDB 会自动检测到“旧时间桶”发生了变动，并在下一次刷新时自动重新计算该桶。

---

## 4. 实战示例

### 步骤 1: 创建连续聚合视图
```sql
CREATE MATERIALIZED VIEW conditions_hourly
WITH (timescaledb.continuous) -- 开启魔法
AS
SELECT
    time_bucket('1 hour', time) as bucket, -- 按小时切分
    device_id,
    avg(temperature) as avg_temp,
    max(temperature) as max_temp
FROM conditions
GROUP BY bucket, device_id;
```

### 步骤 2: 添加自动刷新策略 (Refresh Policy)
你需要告诉数据库多久算一次。

```sql
SELECT add_continuous_aggregate_policy('conditions_hourly',
    start_offset => NULL,     -- 从最早的数据开始
    end_offset => INTERVAL '1 hour', -- 实时保留窗口（最近1小时的数据通过实时层查询，不物化）
    schedule_interval => INTERVAL '30 minutes'); -- 每30分钟运行一次后台任务
```

### 步骤 3: 快乐查询
```sql
-- 这个查询会飞快，且包含上一秒刚写入的数据
SELECT * FROM conditions_hourly
WHERE bucket > NOW() - INTERVAL '7 days';
```

---

## 5. 总结

| 特性 | 普通 PostgreSQL 物化视图 | TimescaleDB 连续聚合 |
| :--- | :--- | :--- |
| **刷新成本** | 极高 (全量重算) | 极低 (只算增量) |
| **实时性** | 差 (取决于刷新间隔) | **完美 (物化+实时 UNION)** |
| **历史修正** | 困难 | 自动处理 |
| **使用难度** | 需自己写 Cron Job | SQL 配置策略即可 |

这就是为什么在单体 PostgreSQL 架构下做时序分析，TimescaleDB 是无可替代的神器。

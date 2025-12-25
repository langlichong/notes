# Time-Series Data Pattern (时序数据模式)

针对具有时间戳特征的海量数据（如 IoT 传感器数据、系统监控指标、日志、金融行情）的专门优化模式。通过使用专用的时序数据库和特定的数据模型设计，实现高吞吐写入和高效的时间范围查询。

## 1. 痛点：MySQL 面对时序数据的"力不从心"

**典型场景**：智能家居平台，10 万个设备，每个设备每 10 秒上报一次温度、湿度、电量数据。

**计算写入量**：
*   10 万设备 × 6次/分钟 = **60 万条/分钟 = 1 万条/秒**。
*   数据持续积累，1 年 = **3000 亿行数据**。

**如果用 MySQL**：
```sql
CREATE TABLE sensor_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id INT,
    temperature FLOAT,
    humidity FLOAT,
    timestamp DATETIME,
    INDEX idx_device_time (device_id, timestamp)
);

INSERT INTO sensor_data (device_id, temperature, humidity, timestamp)
VALUES (12345, 25.5, 60.2, '2023-12-25 10:00:00');
```

**问题爆发**：
1.  **写入瓶颈**：MySQL 单机写入上限约 **5000-10000 TPS**。1 万 TPS 已经接近极限。
2.  **索引膨胀**：B+Tree 索引随着数据增长，深度增加，插入越来越慢。
3.  **查询缓慢**：
    ```sql
    -- 查询某设备最近 24 小时的平均温度
    SELECT AVG(temperature) FROM sensor_data
    WHERE device_id = 12345 
      AND timestamp > NOW() - INTERVAL 24 HOUR;
    ```
    即使有索引，也要扫描 8640 行（24小时 × 360条/小时）。如果查 1 年数据，百万行扫描。
4.  **存储空间爆炸**：3000 亿行数据，即使每行只有 50 字节，也是 **15 TB**。

---

## 2. 解决方案：时序数据库 (TSDB)

专门为时序数据设计的数据库，核心特性：

### 特性 A: LSM-Tree 存储引擎
*   **原理**：数据先写入内存 (MemTable)，再批量刷新到磁盘（顺序写）。
*   **优势**：写入速度极快（**10 万+ TPS**），不受索引深度影响。
*   **代表**：InfluxDB, TimescaleDB (基于 PostgreSQL), ClickHouse。

### 特性 B: 列式存储 + 压缩
*   **原理**：同一字段的数据连续存储，压缩比极高。
*   **示例**：1000 个温度值 `[25.1, 25.2, 25.1, ...]` 压缩后可能只占 100 字节。
*   **优势**：存储成本降低 **10-100 倍**。

### 特性 C: 自动分区 (Time Partitioning)
*   **原理**：数据按时间自动分片（如每天一个分区）。
*   **查询优化**：查最近 24 小时？只扫描 1 个分区。查 1 年前的数据？其他分区早已归档到冷存储。

### 特性 D: 内置聚合函数
```sql
-- InfluxDB 语法
SELECT MEAN(temperature), MAX(temperature)
FROM sensor_data
WHERE device_id = 12345 
  AND time > now() - 24h
GROUP BY time(1h);  -- 按小时聚合
```
*   **性能**：原地计算，不需要读取全部原始数据。

---

## 3. 技术选型

| 数据库          | 定位            | 适用场景              | 优势                                  |
| :-------------- | :-------------- | :-------------------- | :------------------------------------ |
| **InfluxDB**    | 纯时序数据库    | IoT、DevOps 监控      | 专为时序设计，开箱即用，InfluxQL 语法 |
| **TimescaleDB** | PostgreSQL 插件 | 需要关系型能力 + 时序 | SQL 兼容，事务支持，丰富生态          |
| **ClickHouse**  | 列式分析数据库  | 大数据 OLAP、BI 报表  | 查询速度极快，支持 JOIN               |
| **Prometheus**  | 监控专用时序库  | Kubernetes / 服务监控 | 内置告警，与 Grafana 深度整合         |
| **OpenTSDB**    | 基于 HBase      | 超大规模、PB 级数据   | 横向扩展能力强                        |

---

## 4. 数据模型设计

### InfluxDB 示例
```sql
-- Measurement (类似表名)
-- Tags (索引字段，低基数)
-- Fields (数据字段，不索引)
-- Timestamp (主时间字段)

INSERT sensor_data,device_id=12345,location=Beijing temperature=25.5,humidity=60.2 1672041600000000000
             ^          ^          ^          ^              ^              ^
          Measurement  Tag1      Tag2      Field1        Field2      Timestamp(纳秒)
```

**设计原则**：
*   **Tag**：用于过滤和分组的字段（设备ID、地区、类型）。
*   **Field**：实际测量值（温度、湿度、电量）。

---

## 5. 实现策略

### 策略 A: 冷热分离
*   **热数据 (最近 7 天)**：放在 SSD 存储的 InfluxDB，支持高频查询。
*   **温数据 (7-90 天)**：降采样（如每分钟聚合一次），存储空间降低 10 倍。
*   **冷数据 (90 天+)**：归档到对象存储 (S3/OSS)，按需查询。

### 策略 B: 降采样 (Downsampling)
```sql
-- 原始数据：每 10 秒一条
-- 降采样后：每 1 小时一条平均值

CREATE CONTINUOUS QUERY cq_hourly ON mydb
BEGIN
  SELECT MEAN(temperature) INTO hourly_avg_temp
  FROM sensor_data
  GROUP BY time(1h), device_id
END;
```
*   **收益**：存储空间降低 360 倍（3600 秒 / 10 秒）。

### 策略 C: 保留策略 (Retention Policy)
```sql
-- 自动删除 90 天前的原始数据
CREATE RETENTION POLICY rp_90days ON mydb
DURATION 90d REPLICATION 1 DEFAULT;
```

---

## 6. 关键挑战

### 挑战 A: 不支持复杂 JOIN
时序数据库通常不支持或性能很差地支持多表 JOIN。
*   **解决方案**：数据宽表化。将多个数据源的字段合并到一个 Measurement。

### 挑战 B: 更新和删除效率低
时序库优化了追加写入（Append-Only）。不适合频繁修改历史数据。
*   **建议**：时序数据应该是"不可变"的。如果发现错误，插入修正记录而非修改原记录。

### 挑战 C: 学习成本
每种时序库都有自己的查询语言 (InfluxQL, PromQL)。
*   **建议**：优先选择 TimescaleDB（SQL 兼容）降低学习成本。

---

## 7. 适用场景

*   **IoT 设备监控**：智能家居、车联网、工业设备。
*   **系统性能监控**：CPU、内存、网络流量、应用指标 (APM)。
*   **金融行情**：股票 Tick 数据、K 线数据。
*   **日志分析**：Nginx 访问日志、应用错误日志。
*   **用户行为追踪**：点击流、页面访问路径。

## 8. 总结
Time-Series Pattern 是用 **"专库专用"** 对抗海量时序数据的黄金法则。
*   **信条**：别让 MySQL 干不擅长的活。
*   **心法**：写入追加化、存储列式化、查询分区化、数据分层化。

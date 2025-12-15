# 什么是流式数据库 (Streaming Database)?

流式数据库（Streaming Database）是一种**专门设计用于处理动态、无界、实时数据流**的数据库系统。

它的核心理念与传统数据库完全相反：
*   **传统数据库**：数据是静止的，查询是移动的。（你发起一个查询，去扫描静止的数据）
*   **流式数据库**：查询是静止的（预定义好的），数据是移动的。（数据流过预定义的查询逻辑，实时触发结果更新）

---

## 1. 核心区别：流式数据库 vs. 传统数据库

| 特性 | 传统数据库 (PostgreSQL, MySQL) | 流式数据库 (RisingWave, ksqlDB) |
| :--- | :--- | :--- |
| **处理模式** | **Store-then-Process** (先存后算)<br>数据必须先写入磁盘，提交事务后，才能被查询到。 | **Process-then-Store** (先算后存)<br>数据在流入的瞬间就被计算、聚合，结果被实时更新存储。 |
| **查询触发** | **人主动** (Human Active)<br>用户提交 SQL -> 数据库执行 -> 返回快照结果。 | **数据主动** (Data Active)<br>新数据到达 -> 自动触发增量计算 -> 刷新物化视图。 |
| **结果时效性** | **当前时刻的快照**<br>查的是“此时此刻”的状态。 | **永远即时更新的视图**<br>查的是“截至目前”的累积结果。 |
| **擅长场景** | 事务处理 (OLTP), 复杂即席查询 (Ad-hoc OLAP) | 实时监控, 实时报表, 实时告警, 实时 ETL |
| **底层实现** | B+树, 页存储, ACID 事务 | LSM-Tree,流式算子, 增量计算 (Incremental View Maintenance) |

---

## 2. 核心能力：实时物化视图 (Real-time Materialized View)

这是流式数据库的“灵魂”。

在传统数据库中，`Materialized View`（物化视图）通常需要手动刷新 (`REFRESH MATERIALIZED VIEW`)，这是一个昂贵的全量重算过程。

在 **RisingWave** 这样的流式数据库中，**物化视图是自动、增量维护的**。
即：
1.  你定义一个 SQL：`SELECT region, avg(temp) FROM metadata JOIN stream ... GROUP BY region`。
2.  当**一条**新的传感器数据进来：
    *   流式数据库**不会**重新扫描整个表。
    *   它只会取出这条新数据的温度，修改对应 region 的 `sum` 和 `count`，算出新的 `avg`。
    *   这个过程是毫秒级的。
3.  应用层随时 `SELECT * FROM that_view`，拿到的永远是毫秒级新鲜的统计结果。

---

## 3. 为什么 RisingWave 被称为“数据库”而不是“引擎”？

像 Flink、Spark Streaming 被称为**流计算引擎**，而 RisingWave 被称为**流式数据库**，区别在于：

1.  **自带存储 (Serving)**：
    *   **Flink**：算完通常要写到外部系统（如 Redis, MySQL, Kafka）才能被查到。
    *   **RisingWave**：算完的结果直接存在自己内部（分层存储 S3 + 本地缓存），你可以直接用 SQL 连上去查，**不需要**再引入一个 Redis 或 MySQL 来存结果。

2.  **标准 SQL 交互**：
    *   它看起来、用起来就像一个 PostgreSQL。你可以用 JDBC/ODBC 驱动，用 Navicat/DBeaver 连接它。这大大降低了学习门槛。

3.  **流表关联 (First-class Table)**：
    *   它内部既能存“流”（Topic），也能存“表”（Table）。因此它极其擅长做 `Data Stream` JOIN `Static Table` 的操作（正是你需要的元数据关联场景）。

## 4. 总结：你的场景为什么适合？

*   **你需要“状态”**：比如窗口计算（过去5分钟）、状态机（连续3次）。流式数据库自动帮你管理这些状态，不用你自己写代码存 Redis。
*   **你需要“关联”**：你需要把“传感器流”和“设备配置表”合在一起看。传统做法是代码里查库，流式数据库里就是一个 `JOIN`。
*   **你需要“单体/轻量”**：既然它能存结果，你可能连“告警结果入库”这步都省了，业务系统直接读 RisingWave 的 View 就能展示实时大屏。

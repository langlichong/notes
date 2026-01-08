# 双 SAN 存储“无共享”高可用架构设计方案
## (Shared-Nothing Storage High Availability Architecture)

## 1. 架构概述
本方案描述了一种基于双物理存储节点（双 SAN）构建的高可用集群架构。与传统共享存储（Shared Storage）不同，该架构通过**存储级复制**实现数据的双份实时拷贝，旨在消除单占存储带来的单点故障（SPOF）。

## 2. 核心专业术语
在技术交流与文档编写中，建议使用以下标准术语：
*   **架构模式**：无共享架构 (Shared-Nothing Architecture)
*   **同步机制**：存储级同步镜像 (Storage-level Synchronous Mirroring)
*   **复制标准**：阵列级同步复制 (Array-based Synchronous Replication)
*   **切换策略**：自动故障转移 (Automatic Failover) 与 节点级倒换 (Node-level Switchover)

## 3. 存储方案对比

| 特性 | 共享存储架构 (Shared Storage) | 双 SAN 无共享架构 (Shared-Nothing) |
| :--- | :--- | :--- |
| **存储份数** | 1 份数据 (LUN 级别共享) | **2 份数据 (LUN 级别实时镜像)** |
| **单点故障** | 存储阵列损坏将导致全集群宕机 | 允许单个存储阵列完全物理损毁 |
| **扩容能力** | 受限于单一阵列的核心插槽/控制器 | 节点独立，支持横向扩展灾备能力 |
| **典型应用** | 传统内网 ERP、非核心 OA | **金融核心、工业控制、实时监控系统** |

## 4. 实施策略选择

### A. 阵列级复制 (Array-based - 最推荐)
*   **实现**：利用 SAN 存储原生协议（如 SRDF, SnapMirror）完成数据同步。
*   **优势**：由硬件控制器处理，0 CPU 损耗，数据一致性由硬件保证。
*   **适用**：企业级高端 SAN 环境。

### B. 软件定义存储 (DRBD / WinDRBD)
*   **实现**：在主机内核层拦截块设备 IO 并在网络间镜像。
*   **优势**：成本低，无需特定品牌存储，灵活性极强。
*   **适用**：基于 Linux/Windows Server 的标准化服务器集群。

### C. 镜像卷管理 (Host-based Mirroring)
*   **实现**：利用 LVM (Linux) 或 Storage Spaces (Windows) 构建软件 RAID 1。

## 5. 业务场景集成 (PostgreSQL, Redis, EMQX)
为了实现“整机一键式切换”，所有服务数据必须遵循以下部署原则：
1.  **数据归拢**：所有服务的数据目录（Data Directory）必须强行挂载于同步的逻辑卷（如 `/dev/drbd0`）之上。
2.  **原子切换**：通过 Keepalived 或 Pacemaker 联动，确保在一个节点失效时，执行以下原子操作：
    *   旧主节点服务强制关停 (Fencing)
    *   新主节点接管虚拟 IP (VIP)
    *   新主节点夺取存储写权限 (Primary)
    *   新主节点挂载文件系统并拉起应用服务

## 6. 核心优势总结
1.  **RPO = 0**：同步模式下，主备数据字节级对等，切换不丢数据。
2.  **业务连续性**：通过 VIP 漂移，客户端（MQTT 设备/前端）无感知切换。
3.  **灾备级别高**：支持跨机架、跨机房部署。

---
*Created on 2026-01-08 by Antigravity AI Assistant.*

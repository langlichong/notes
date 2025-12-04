# Java 应用 NUMA 亲和性优化指南

## 核心策略

对于 Java 应用，实现 NUMA 亲和性主要分两步：
1.  **操作系统/容器层**：将 Java 进程绑定到特定的 NUMA 节点（CPU + 内存）。
2.  **JVM 层**：开启 JVM 的 NUMA 感知特性，优化内存分配。

---

## 1. 操作系统层：绑定 NUMA 节点

使用 `numactl` 工具将 Java 进程绑定到特定的 NUMA 节点。

### 检查 NUMA 拓扑
```bash
numactl --hardware
# 输出示例：
# available: 2 nodes (0-1)
# node 0 cpus: 0 1 2 3 4 5 ...
# node 0 size: 32768 MB
# node 1 cpus: 16 17 18 19 ...
# node 1 size: 32768 MB
```

### 启动 Java 应用（绑定到节点 0）
```bash
# --cpunodebind=0 : 仅使用节点 0 的 CPU
# --membind=0     : 仅从节点 0 分配内存
numactl --cpunodebind=0 --membind=0 java -jar my-app.jar
```

> [!TIP]
> **`--membind` vs `--localalloc`**
> *   `--membind=0`：强制只用节点 0 的内存。如果节点 0 满了，即使节点 1 有空闲，也会抛出 OOM（推荐用于严格隔离）。
> *   `--localalloc`：优先在当前节点分配，满了可以溢出到其他节点（推荐用于软隔离）。

---

## 2. JVM 层：开启 NUMA 优化

即使 OS 层做了绑定，JVM 默认可能并不感知 NUMA 架构。需要显式开启优化。

### 关键参数：`-XX:+UseNUMA`

```bash
java -XX:+UseNUMA -XX:+UseParallelGC -jar my-app.jar
# 或
java -XX:+UseNUMA -XX:+UseG1GC -jar my-app.jar
```

### 作用原理
*   **ParallelGC**：默认支持最好。Eden 区会被分割成多个部分，每个部分对应一个 NUMA 节点。线程在哪个节点上运行，就在对应的 Eden 区分配对象。
*   **G1GC (JDK 14+)**：JDK 14 开始 G1 也支持了 NUMA 感知。它会将 Region 均匀分布在各个 NUMA 节点，并优先在本地节点分配。
*   **ZGC**：天生支持 NUMA 感知。

> [!IMPORTANT]
> **JDK 版本建议**
> *   JDK 8：推荐使用 `ParallelGC` 配合 `-XX:+UseNUMA`。
> *   JDK 11/17+：G1GC 对 NUMA 支持已经很好了，推荐使用。

---

## 3. 容器环境 (Kubernetes / Docker)

在容器环境中，直接使用 `numactl` 可能不方便，通常依赖调度器。

### Kubernetes (K8s)

K8s 通过 **Topology Manager** 实现 NUMA 感知调度。

1.  **Kubelet 配置**：
    启用 `TopologyManager` 特性门控（K8s 1.18+ 默认开启）。
    配置策略：`--topology-manager-policy=single-numa-node`（最严格，强制单 NUMA 节点）。

2.  **Pod 资源申请**：
    必须设置 `limits` 等于 `requests`（Guaranteed QoS），且 CPU 必须是整数。

    ```yaml
    resources:
      limits:
        cpu: "4"
        memory: "8Gi"
      requests:
        cpu: "4"
        memory: "8Gi"
    ```

### Docker

直接在启动时绑定 CPU 和内存节点：

```bash
# 绑定到 NUMA 节点 0 的 CPU 和内存
docker run --cpuset-cpus="0-15" --cpuset-mems="0" my-java-app
```

---

## 4. 验证效果

### 验证 JVM 是否开启 NUMA
```bash
java -XX:+UseNUMA -XX:+PrintFlagsFinal -version | grep UseNUMA
# bool UseNUMA = true {product}
```

### 验证内存分布
使用 `numastat` 查看进程的内存分配情况：
```bash
# 获取 Java 进程 PID
pid=$(pgrep -f my-app)
numastat -p $pid

# 输出示例（理想情况）：
#                           Node 0   Node 1
# Total                    8192.00     0.00  <-- Node 1 几乎为 0
```

## 总结最佳实践

1.  **物理机/虚机部署**：`numactl --cpunodebind=0 --membind=0 java -XX:+UseNUMA ...`
2.  **K8s 部署**：配置 Topology Manager 为 `single-numa-node`，并使用 Guaranteed QoS。
3.  **GC 选择**：优先 G1GC (JDK 14+) 或 ParallelGC。

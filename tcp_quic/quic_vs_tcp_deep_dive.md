# QUIC vs TCP：深度解析

本文档深入比较了 QUIC 和 TCP，重点关注 QUIC 在连接管理、数据结构和状态流转方面的内部机制。

## 1. 核心差异：TCP vs QUIC

| 特性 | TCP (传输控制协议) | QUIC (快速 UDP 互联网连接) |
| :--- | :--- | :--- |
| **传输层** | 直接运行于 IP 之上。 | 运行于 UDP 之上。 |
| **空间** | 在 OS 内核中实现（更新缓慢）。 | 在用户空间实现（迭代迅速）。 |
| **握手** | TCP 握手 (1-RTT) + TLS 握手 (1-2 RTT)。 | 集成握手 (TCP+TLS 结合)。1-RTT 或 0-RTT。 |
| **队头阻塞 (HoL)** | **连接级**：丢包会阻塞其后的所有数据。 | **流级**：丢包仅影响特定流；其他流继续传输。 |
| **连接 ID** | 由 **五元组** 标识 (源IP, 源端口, 目IP, 目端口, 协议)。网络切换会中断连接。 | 由 **连接 ID (CID)** 标识。在 IP/端口变化（NAT 重绑定、网络切换）下保持存活。 |
| **加密** | 可选（通常在顶层使用 TLS）。头部是明文。 | 强制（TLS 1.3）。大多数头部和负载都已加密。 |
| **流量控制** | 连接级滑动窗口。 | 每流 (Per-stream) 和 连接级 (Connection-level) 流量控制。 |

---

## 2. QUIC 连接建立

QUIC 结合了传输层和加密层的握手以最小化延迟。

### 握手过程 (1-RTT)

1.  **客户端初始 (Client Initial)**：
    *   客户端发送包含 TLS ClientHello 开始部分的 **Initial Packet**。
    *   生成一个随机的目标连接 ID (DCID)。
2.  **服务端初始与握手 (Server Initial & Handshake)**：
    *   服务端回复 **Initial Packet** (ServerHello, Key Share) 和 **Handshake Packet** (Encrypted Extensions, Certificate, Verify)。
    *   服务端建立自己的源连接 ID (SCID)。
3.  **客户端完成 (Client Completion)**：
    *   客户端发送 **Handshake Packet** (Finished)。
    *   连接完全建立。应用数据可以通过 **Short Header** 数据包发送。

### 0-RTT (恢复)
如果客户端之前连接过并缓存了会话票据 (session ticket)：
1.  **客户端初始**：发送 ClientHello + 使用缓存密钥加密的 **Early Data** (在 0-RTT 包中)。
2.  **服务端响应**：如果接受，服务端立即处理数据。如果拒绝（例如密钥轮换），服务端强制回退到 1-RTT。

---

## 3. QUIC 连接断开

与 TCP 的四次挥手 (FIN/ACK) 不同，QUIC 的断开更为干脆且安全。

### 立即关闭 (Immediate Close)
*   **发起方**：发送 `CONNECTION_CLOSE` 帧。
*   **原因**：可以是应用错误 (Application Close) 或 协议错误 (Transport Close)。
*   **状态**：端点进入 "draining" (排空) 状态。它停止发送数据包并丢弃接收到的数据包（可选发送无状态重置，但通常关闭后保持静默）。
*   **接收方**：收到 `CONNECTION_CLOSE` 后立即终止。没有像 TCP 那样的 "半关闭" (half-closed) 状态。

### 无状态重置 (Stateless Reset)
如果端点收到一个它不识别的连接的数据包（例如崩溃重启后），它会发送一个 **Stateless Reset Token**。这允许对端立即检测到连接断开，而无需等待超时。

---

## 4. 重要的内部数据结构

### A. 数据包 (Packets)
QUIC 数据包是传输的顶层单元。
*   **长头部包 (Long Header Packets)**：用于握手期间 (Initial, 0-RTT, Handshake, Retry)。包含显式的版本号和完整的连接 ID。
*   **短头部包 (Short Header Packets)**：握手后用于应用数据。为最小开销而优化。Key phase bit 允许密钥轮换。

### B. 帧 (Frames)
QUIC 数据包的负载由一个或多个帧组成。
*   `STREAM`：携带应用数据。包含 `Stream ID`, `Offset`, `Length`, 和 `Fin` 位。
*   `ACK`：确认收到的包号。支持范围 (ACK blocks) 以高效处理乱序交付。
*   `CRYPTO`：携带 TLS 握手数据。像流一样有序，但与应用流分离。
*   `MAX_DATA` / `MAX_STREAM_DATA`：流量控制更新（基于信用）。
*   `NEW_CONNECTION_ID`：为对端提供用于迁移的备用 CID。

### C. 流 (Streams)
*   **抽象**：连接内的有序字节流。
*   **类型**：
    *   **单向 (Uni-directional)**：单向传输（例如服务端推送）。
    *   **双向 (Bi-directional)**：请求/响应。
*   **ID**：最后 2 位指示类型（客户端/服务端发起，单向/双向）。

---

## 5. 状态流转与包号空间

QUIC 将加密上下文隔离到不同的 "空间" 中，以防止死锁并确保安全。

### 包号空间 (Packet Number Spaces)
1.  **Initial Space**：使用由目标连接 ID 派生的密钥加密。包含用于 ClientHello/ServerHello 的 `CRYPTO` 帧。
2.  **Handshake Space**：使用由 Diffie-Hellman 交换派生的密钥加密。包含 TLS Certificate/Verify。
3.  **Application Data Space (1-RTT)**：使用最终会话密钥加密。包含 `STREAM` 帧。

### 状态机 (简化版)
1.  **Idle (空闲)**：无连接。
2.  **Initializing (初始化)**：发送/接收 Initial 包。
3.  **Handshaking (握手)**：验证证书，派生 1-RTT 密钥。
4.  **Active (活跃)**：1-RTT 密钥已安装。流已打开。
    *   *Key Update (密钥更新)*：可以在不中断连接的情况下过渡到新密钥。
    *   *Migration (迁移)*：可以在保持 Active 状态的同时更改 IP/端口。
5.  **Closing (关闭中)**：发送/接收 `CONNECTION_CLOSE`。
6.  **Draining (排空)**：等待一段时间以吸收网络中乱序的数据包（类似于 TIME_WAIT 但通常更短）。
7.  **Closed (已关闭)**。

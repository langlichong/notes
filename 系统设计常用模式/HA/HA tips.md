- Pacemaker + Corosync + DRBD

第一层：基础设施/硬件层 HA (Infrastructure HA)
解决的问题：硬件坏了怎么办？

网络 HA：使用 VRRP (虚拟路由冗余)、LACP (链路聚合/网卡绑定)。解决单根网线断了或单个路由器坏了的问题。
电源 HA：使用 双电源 (Dual Power Supplies) 接在不同的 PDU 上。解决停电或电源模块烧毁的问题。
计算 HA：使用 VMware HA 或 KVM 集群。如果一台物理服务器冒烟了，虚拟机自动在另一台物理机上重启。

第二层：存储层 HA (Storage HA)
解决的问题：磁盘坏了或数据丢了怎么办？

本地磁盘 HA：使用 RAID (磁盘阵列)。掉了一块盘，数据还在。
网络存储 HA：使用 双控 SAN / 分布式存储。存储服务器本身有两颗“大脑”（控制器），一颗坏了另一颗瞬时接管。
跨机房存储 HA：使用 DRBD (分布式复制块设备) 或 Ceph。实现两台独立服务器之间的磁盘块级实时同步。


第三层：操作系统/平台层 HA (Platform HA)
解决的问题：系统死机或服务进程挂了怎么办？

心跳检测与故障切换：使用 Keepalived 或 Pacemaker/Corosync。
浮动 IP (VIP)：一旦这台服务器响应慢了，立即把 IP 地址“飘”到另一台服务器。
容器 HA：使用 Kubernetes (K8s)。如果一个容器挂了，K8s 自动在别处拉起一个新的。



第四层：应用/业务层 HA (Application HA)
解决的问题：程序逻辑报错或单个节点处理不过来怎么办？

负载均衡 (Load Balancing)：使用 Nginx, HAProxy, F5。把请求分发给后端 10 台服务器。如果其中 3 台当机，负载均衡器会自动把它们踢出队列。
数据库 HA：
主从复制 (Master-Slave)：MySQL 的 Binlog 同步。
集群 (Cluster)：Redis Cluster 或 MongoDB Replica Set。
消息队列 HA：使用 Kafka/RabbitMQ 的多节点镜像。


当你面对一个架构，问自己三个问题???
坏了一根线/一个路由（网络层）：能自动切换吗？ $\rightarrow$ 找 VRRP/LACP。
坏了一块盘/一个存储（存储层）：数据会丢吗？ $\rightarrow$ 找 RAID/DRBD/SAN。
坏了一个程序/一台服务（应用层）：业务会停吗？ $\rightarrow$ 找 Load Balancer/K8s/Middleware Cluster

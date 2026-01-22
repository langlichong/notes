为什么需要 Netlink？（解决什么痛点）
在 Linux 里，用户空间（你写的 C 代码、Java 进程、ip 命令）是不能直接修改内核里的网卡状态的，因为那里是禁区。

1. 以前的对话方式很不方便：
方法一：ioctl 系统调用。这是一种很老的、像盲盒一样的调用，扩展性极差。
方法二：/proc 或 /sys 文件系统。虽然能看状态，但想通过写文件来改变复杂的网络拓扑，速度慢且笨拙。
Netlink 的出现：它借鉴了 Socket (套接字) 的思想。你只需要开一个 AF_NETLINK 类型的 Socket，就能像发短信一样，给内核发一串结构化的消息

2.Netlink 的核心工作方式
你可以把它想象成内核里的一个**“办事处”**：

用户发令：你运行 ip addr add 192.168.1.1 dev eth0。
通信过程：这个命令底层会打开一个 Netlink Socket，写一段类似的消息：{动作: 添加, 对象: eth0, 内容: 192.168.1.1}，然后发送出去。
内核处理：内核里的 Netlink 接收模块收到消息，校验权限（这就是为什么需要 sudo），然后去修改网卡的数据结构。
实时监听（Broadcast）：这非常酷！如果某个网卡掉线了，内核会自动向 Netlink 频道广播一个消息。
Keepalived 或 NetworkManager 这种软件就天天蹲在 Netlink 频道里听。一旦听到“网线拔掉”的消息，立刻做出反应。

3. Netlink 处理的不同领域（Family）
Netlink 不仅仅管网络，它有很多“频道”：
NETLINK_ROUTE：管理路由表、IP 地址、邻居表等（最常用）。
NETLINK_FIREWALL：管理防火墙规则。
NETLINK_NFLOG：把防火墙拦截的日志传给用户。
NETLINK_KOBJECT_UEVENT：管理硬件的热插拔（比如你插上一个 U 盘，内核就是通过 Netlink 告诉系统的）

```c
#include <sys/socket.h>
#include <linux/netlink.h>
#include <linux/rtnetlink.h>

int main() {
    // 1. 创建一个 Netlink 套接字
    int fd = socket(AF_NETLINK, SOCK_RAW, NETLINK_ROUTE);
    
    // 2. 绑定路由事件频道
    bind(fd, (struct sockaddr *)&sa, sizeof(sa));

    // 3. 开始接收内核发来的“短信”
    while (1) {
        recv(fd, buf, sizeof(buf), 0);
        printf("收到内核发来的网络变更消息！\n");
    }
}
```

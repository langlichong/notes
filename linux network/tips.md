- NUMA VS vNUMA
- multipath
- VRRP Protocol
- Pacemaker
- drbd
- crm_sh
- Device mapper
- SAN iSCSI
- volume management tech
  - Thin Provisioning：按需分配，先用先得 `man 7 lvmthin`
  - Thick Provisioning (厚置备 - "Fat"): 先占坑，再吃饭。 当你创建一个 100GB 的逻辑卷（LV）时，系统会立即从物理硬盘中划走 100GB 的连续或非连续块。无论你是否在里面写了数据，这 100GB 的物理空间都已经不可用于其他用途。
  - LVM2 EVMS dmraid
  - dmsetup losetup
- LVM DR模式
  - 在 LVS（负载均衡）的 DR 模式下，备份机通常会在 lo 接口上绑定一个虚拟 IP (VIP)，并配置 ARP 抑制。这样做的目的是让服务器能接收发往 VIP 的包，但不会去抢主服务器的流量
- Loop Device (losetup): file as disk
  - 虚拟磁盘测试：你想练习 LVM 或 Device Mapper，但又没有多余的物理硬盘。你可以创建一个 1G 的空文件，把它虚拟成 /dev/loop0，然后把它当成物理卷（PV）来练手
  - 挂载镜像：比如你下载了一个 ubuntu.iso，你想看里面的内容，系统会把它映射到一个 loop 接口
  ```text
    losetup -a：列出当前所有已映射的回环设备。
    losetup -f：查找第一个可用的回环设备名。
    losetup /dev/loop0 my_disk.img：将文件关联到设备。
    mount -o loop image.iso /mnt：这是最常用的快捷方式，一步到位完成关联和挂载
  ```

```shell
# 1. 创建两个 100M 的虚拟硬盘文件
dd if=/dev/zero of=disk1.img bs=1M count=100
dd if=/dev/zero of=disk2.img bs=1M count=100

# 2. 将它们关联到 loop 设备
losetup /dev/loop0 disk1.img
losetup /dev/loop1 disk2.img

# 3. 接下来你就把 /dev/loop0 当成真正的硬盘，练习 Device Mapper 命令
echo "0 204800 linear /dev/loop0 0" | dmsetup create my_test_dev

# 4. 甚至可以结合起来测试冗余
# (此处可以使用 dmsetup create --target mirror 来模拟镜像)
```

- sparse file 技术
   - filefrag : 查看文件的底层结构，它能直接告诉你“空洞”在哪里
     - Sparse File (稀疏文件/瘦):    `truncate -s 1G thin.img` 名义上 1G，但实际占用 0 空间的文件
    - ls -l，你看到的是逻辑大小（名义大小）, 使用 ls -s 或 du 揭穿
  - Fully Allocated File (全分配文件/胖): `dd if=/dev/zero of=thick.img bs=1M count=1024`: 把内容填满，确保 1G 物理空间被占据,  或  或者使用现代文件系统的快速占位命令： `fallocate -l 1G fat_disk.img`

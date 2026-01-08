1. 整机切换万能脚本:/etc/keepalived/notify.sh
```shell
#!/bin/bash

TYPE=$1    # GROUP 或 INSTANCE
NAME=$2    # 实例名
STATE=$3   # MASTER | BACKUP | FAULT

case $STATE in
    "MASTER")
        # 1. 夺取 DRBD 控制权
        drbdadm primary db_sync
        # 2. 挂载磁盘 (建议加 -o noatime 优化性能)
        mount /dev/drbd0 /data
        # 3. 依次启动服务
        systemctl start postgresql
        systemctl start redis
        systemctl start emqx
        exit 0
        ;;
    "BACKUP"|"FAULT")
        # 1. 先停服务（最重要，防止脏写）
        systemctl stop emqx
        systemctl stop redis
        systemctl stop postgresql
        
        # 2. 卸载磁盘
        # 使用 -l (lazy) 可以在即便有进程占用时也确保挂载点断开，但更建议用 fuser 确保干净
        fuser -mk /data  # 强制踢出还占着磁盘的残余进程
        umount /data
        
        # 3. 降级为 Secondary，准备接收同步
        drbdadm secondary db_sync
        exit 0
        ;;
    *)
        echo "Unknown state: $STATE"
        exit 1
        ;;
esac
```
2.  keepalived.conf 中如何调用 **Keepalived 调用 notify 时会自动在末尾加上三个参数：$1=TYPE, $2=NAME, $3=STATE。所以脚本里用 $3 获取 MASTER/BACKUP**
- 在两台机器的 vrrp_instance 配置中，加入下面的调用
  ```text
    vrrp_instance VI_1 {
      ...
      # 无论状态变为什么，都调用这个脚本，并传入对应的参数
      notify /etc/keepalived/notify.sh
   }
  ```

3. DRBD
   <img width="1204" height="561" alt="image" src="https://github.com/user-attachments/assets/ade847ab-dbf7-41cb-9b4b-9e7f159a90ce" />
   <img width="1026" height="612" alt="image" src="https://github.com/user-attachments/assets/242b7a21-a56f-4138-810f-a1cdd5149cd2" />
   <img width="1073" height="749" alt="image" src="https://github.com/user-attachments/assets/428c790b-725d-4c5c-80e6-1ea1d077f011" />
   <img width="1233" height="398" alt="image" src="https://github.com/user-attachments/assets/2db7cc3e-06f1-4a78-ad37-20e47535eedb" />
   <img width="1205" height="288" alt="image" src="https://github.com/user-attachments/assets/09c08da6-1ccf-4418-a83c-f1612e301a96" />

3. 全自动工具: crm_sh (Corosync / Pacemaker)
   <img width="970" height="469" alt="image" src="https://github.com/user-attachments/assets/8e4aaea4-ba95-4a1f-afe7-cb9f5ad2a269" />
   <img width="1235" height="307" alt="image" src="https://github.com/user-attachments/assets/e37cb4a4-1661-41df-b182-de74126dfef9" />





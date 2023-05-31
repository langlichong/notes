-- 查看某个jvm进程支持的jcmd 子命令命令
jcmd <pid> help
 
 -- jcmd 0 help : 进程号为0，则该命令会被发送到所有的jvm进程中，如果有很多进程，则返回每一个进程支持的子命令列表
 
 -- 查看某个进程支持的jmx开启命令的具体选项：
 jcmd 24276 help ManagementAgent.start

--查看开启的JMX（local ,remote）
jcmd <pid> ManagementAgent.status  

--开启本地JMX
jcmd <pid> ManagementAgent.start_local

-- 开启远程JMX
jcmd <pid> ManagementAgent.start jmxremote.host=10.8.103.15  jmxremote.port=15555 jmxremote.authenticate=false jmxremote.ssl=false
 
-- 停止JMX
 jcmd <pid> ManagementAgent.stop
  
 ## linux 下以服务方式运行 java  jar包方式，如何获取到真正的java pid
  -- systemd 下，设服务名字为 tg-base-api.service
  运行命令：systemctl status tg-base-api.service 
  ```
    tg-base-api.service - 3D System Backend API base Snowy
     Loaded: loaded (/etc/systemd/system/tg-base-api.service; disabled; vendor preset: enabled)
     Active: active (running) since Tue 2023-05-30 16:03:23 CST; 37min ago
   Main PID: 2341149 (service.sh)
      Tasks: 80 (limit: 19105)
     Memory: 1.3G
     CGroup: /system.slice/tg-base-api.service
             ├─2341149 /bin/bash /home/genew/tg-base-service/bin/latest/service.sh
             └─2341150 /usr/bin/java -jar /home/genew/tg-base-service/bin/latest/main.jar --spring.profiles.active=prod --server.port=8000
  ```
  - 其中2341150 就是java进程的pid
  
  -- 有时候 jps 之类的命令无法找到pid ，java服务启动时候会在 /tmp/ 目录下生成很多目录，其名字为/tmp/hsperfdata_实际用户名，如/tmp/hsperfdata_root
  该目录下就是对应的pid文件，找到pid就可以使用 jcmd动态开启一些功能，如jmx远程连接，或者 jfr等
  
  -- 有时候运行 jps、jcmd、jstat等提示 java.io.IOException: Operation not permitted , 这个可能是你启动java 程序时候，权限较高，看下是不是用root权限启动的，
  加个 sudo 试试看
 
 -- 开启JMX后连接远程可能会报错连接不上，JMX开启后，除了手工指定的端口如上面的 15555 外及服务自身的端口8000，程序还会监听两个随机端口,需要确认是否端口未开放到防火墙
 ```
   在Java启动时，JMX会绑定一个接口，RMI也会绑定一个接口，在复杂网络环境下，有可能你通过打开防火墙允许了JMX端口的通过，但是由于没有放行RMI，远程连接也是会失败的。
这是因为JMX在远程连接时，会随机开启一个RMI端口作为连接的数据端口，这个端口会被防火墙给阻止，以至于连接超时失败。
 在Java7u25版本后，可以使用 -Dcom.sun.management.jmxremote.rmi.port参数来指定这个端口；好消息是，你可以将这个端口和jmx.port的端口设置成一个端口，这样防火墙就只需要放行一个端口就可以了
 jmxremote.rmi.port=xxx
 ```
 ```
 sudo netstat -tupln | grep 2341150
 tcp6       0      0 :::41801                :::*                    LISTEN      2341150/java
 tcp6       0      0 10.8.103.15:10001       :::*                    LISTEN      2341150/java
 tcp6       0      0 10.8.103.15:37459       :::*                    LISTEN      2341150/java
 tcp6       0      0 :::8000                 :::*                    LISTEN      2341150/java
 ```

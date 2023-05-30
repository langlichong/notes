--查看开启的JMX（local ,remote）
jcmd <pid> ManagementAgent.status  

--开启本地JMX
jcmd <pid> ManagementAgent.start_local

-- 开启远程JMX
jcmd <pid> ManagementAgent.start jmxremote.port=15555 jmxremote.authenticate=false jmxremote.ssl=false
  
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

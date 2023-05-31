- 在java11中由于没有tools.jar，所以原有的配置jstatd.policy方式行不通, jstatd.policy改成如下即可
```
grant codebase "jrt:/jdk.jstatd" {    
   permission java.security.AllPermission;    
};

grant codebase "jrt:/jdk.internal.jvmstat" {    
   permission java.security.AllPermission;    
};
```
- jstatd -J-Djava.rmi.server.hostname=79.129.161.x -J-Djava.security.policy=./jstatd.policy -p 1100
- 使用jvisualvm 连接即可

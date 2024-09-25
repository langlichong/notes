```xml
<!-- 使用winsw注册服务 -->
<service> 
     <id>pls-api</id> 
     <name>pls-api</name>
     <description>3D PLS Service</description>
	 <env name="JAVA_HOME" value="%JAVA_HOME%"/>
     <executable>java</executable> 
     <arguments> --add-opens java.base/java.lang.reflect=ALL-UNNAMED -Dspring.config.location=D:\apps\pls_api\config\ -Dspring.profiles.active=dev -Dserver.port=10053  -Dpls.alarm.enableBroadcast=false -jar "D:\apps\pls_api\application-2.0.0.jar" </arguments>
     <!-- 开机启动 -->
     <startmode>Automatic</startmode>
     <!-- 日志配置 -->
     <logpath>%BASE%\log</logpath>
     <logmode>none</logmode>
 </service>
```

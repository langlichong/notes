--查看开启的JMX（local ,remote）
jcmd <pid> ManagementAgent.status  

--开启本地JMX
jcmd <pid> ManagementAgent.start_local

-- 开启远程JMX
jcmd <pid> ManagementAgent.start jmxremote.port=15555 jmxremote.authenticate=false jmxremote.ssl=false

1. 分离前后端：从thingsboard 源码中移除（注释）前端相关的模块依赖、前端插件 
- pom移除相关ui-ngx模块及依赖: 
    application/pom.xml移除ui-ngx依赖
    thingsboard/pom.xml 移除ui-ngx 模块包含语句 ，移除 web-ui 依赖
    msa/black-box-test/pom.xml移除web-ui依赖
    msa/js-executor/pom.xml 移除frontend-maven-plugin
    msa/web-ui/pom.xml 移除ui-ngx依赖，移除 frontend-maven-plugin插件，移除maven-dependency-plugin插件的extract-web-ui目标
    msa/pom.xml 移除web-ui子模块
- 通过maven直接编译纯java的后端代码，保证编译通过（移除ui-ngx 及 web-ui及 frontend-maven-plugin后很容易编译成功）

2. 数据库初始化
方法 1 ： 
- 拷贝 dao模块的resources目录下的cassandra、sql文件夹到 application模块的src/main/data目录下
- 运行 application模块下的 ThingsboardInstallApplication#main方法完成数据库初始化 
- 为了方便可以将安装程序打包为可执行jar 方便后续在其他地方安装（可以使用idea的artifact导出功能）

方法 2:
 - 将 dao模块的resources目录下的cassandra、sql文件夹拷贝到一个自定义的目录下，e.g. D:\tb\data 
 - 将application/src/main/data 目录下的所有内容也拷贝到 D:\tb\data  目录下
 - 在ThingsboardInstallApplication#main 方法首行使用 System.setProperty("install.data_dir","D:\\tb\\data")
 - 运行ThingsboardInstallApplication#main 
 
 方法 3：
 - 将代码编译打包通过后，在packaging 模块下 有很多安装脚本，可以运行安装脚本进行数据库初始化
 
 3. ui-ngx 部署到nginx server中
 - 部署前可以参考ui-ngx前端代码中的proxy.conf.js ，该文件中已经配置了各种代理，配置nginx时候只需翻译为nginx的配置即可
 - 注意：由于tb 有 websocket代理配置，在nginx中websocket配置比较特别，可以参考nginx官方websocket章节(注意map directive的使用)
 - 以下给出经实践的nginx配置（thingsboard v3.4.4）：
 ```
   map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
  }

  server {
          server_tokens off;
          listen       90;
          server_name  localhost;
          #charset koi8-r;
          #access_log  logs/host.access.log  main;
          #项目文件映射
          location / {
              root  /home/xxx/apps/ui-ngx/;
              try_files $uri $uri/ /index.html =404;
          }

          #转发api
          location ~ ^/(api|static/rulenode|static/widgets|oauth2|login/oauth2|api/ws)  {
              proxy_pass http://xxx:xxx;
              proxy_http_version 1.1;
              proxy_set_header Upgrade $http_upgrade;
              proxy_set_header Connection $connection_upgrade;
              proxy_set_header Host $host;
          }

          error_page   500 502 503 504  /50x.html;
          location = /50x.html {
              root   html;
          }
    }
 ```

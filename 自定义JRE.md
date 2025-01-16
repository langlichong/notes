### 收集springboot应用的jdk模块依赖清单
1. 使用spring boot 打包插件打包应用，得到jar(Uber fat jar)
2. 解压缩fat jar: `tar xf xxx.jar`
3. 对解压生成的目录 `\BOOT-INF\lib`下所有jar包使用如下命令分析依赖的module
```shell
jdeps --list-deps -R  xxx.jar
```
4. 收集步骤3中每个jar所依赖的jdk module名称

### 生成自定义JRE
1. 将收集到的模块名称使用逗号分隔组织成字符串
2. 使用jlink生成jre，命令如下
```shell
jlink --verbose --add-modules  [此处为应用依赖的所有jdk模块,使用逗号分隔]  --strip-debug --no-man-pages --no-header-files --compress=2 --output  [生成的jre文件存储目录]
```

### 编写Dockerfile
- Dockerfile中使用自定义的jre能最大化缩减生成的镜像的大小

```
在 Java 程序启动后，tmp目录下会生成一个名为 hsperfdata_用户名称  的文件夹，在这个文件夹中会有一些以 java 进程pid命名的文件。
在我们使用jps命令查询进程信息的时候，实际上就是将这个文件夹下的文件列出来，因此当这个文件夹为空或者这个文件夹的所有者和文件所属组权限与运行 Java 程序的用户权限不一致时，jps命令就查询不到该进程了
```
> solution
- 一般systemd管理的服务都是以root用户启动的,所以java程序启动时生成的tmp下的文件夹名称是 hsperfdata_root, 而运行 jps ， jcmd等命令时使用root账户即可查看到(如, ubuntu下 sudo jps -l )

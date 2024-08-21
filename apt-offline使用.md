如何准备离线安装包

离线安装包准备，需要在一个有网络的机器上进行(此处是 ubuntu20.04), 需要提前安装apt-offline工具：


  sudo apt install apt-offline



此处只示范需要安装 apt repository 及不需要安装apt repository 两种方式



OpenJDK：不需安装对应软件官方 apt repository

# 注意：一般在apt update后即可安装 ，无需添加额外的 apt repository
# 利用apt-offline生成签名文件
sudo apt-offline set jdk.sig --install-package  openjdk-11-jdk
# 下载安装包及依赖到 jdk目录
sudo apt-offline get jdk.sig -d ./jdk # 此步骤若执行有报错，继续重复执行。
# 下载无误后安装
sudo dpkg -i ./jdk/*.deb




PostgreSQL：需要安装官方 apt repository

   # 1. 配置 PostgreSQL APT repository
 sudo apt install curl ca-certificates
 sudo install -d /usr/share/postgresql-common/pgdg
 sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc
 
 sudo sh -c 'echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'     
 # 2. 生成签名文件
 # 为安装 postgresql-15 生成签名文件
 # 有的低版本的apt-offline 可能有add命令，确认自己版本: apt show apt-offline
 sudo apt-offline set pls.sig --install-packages postgresql-15，此步骤执行若报错:ERROR: FATAL: Something is wrong with the apt system.
 请执行：sudo apt update，然后继续执行上述命令。
 #3. 下载依赖包并安装
 sudo apt-offline get pls.sig -d pg15 
 sudo dpkg -i pg15/*.deb

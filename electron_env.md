## 使用Electron-forge 进行环境构建（Windows 11）
[electron-forge](https://github.com/electron/forge)

## 步骤
1. 安装Node JS ，本地已经安装了很多版本，本人使用 nvm 进行Node的多版本管理，验证版本：
```
   > nvm list
    19.8.1
    16.15.1
  * 16.14.2 (Currently using 64-bit executable)
    16.13.0
    14.19.0
    
    npm -v
    8.5.0
```
2. 按照electron-forge github readme 进行初始化 （注意不要使用 electron-forge init,这个有很多坑，会让你死去活来）
- npx create-electron-app my-new-app
- 执行良久后提示
```
 npx create-electron-app my-new-app
Need to install the following packages:
  create-electron-app
Ok to proceed? (y)
npm WARN deprecated @npmcli/move-file@2.0.1: This functionality has been moved to @npmcli/fs
✔ Locating custom template: "base"
✔ Initializing directory
✔ Preparing template
✔ Initializing template
⚠ Installing template dependencies
  ✔ Installing production dependencies
  ✔ Installing development dependencies
  ✖ Failed to install modules: ["electron"]

  With output: Command failed with a non-zero return code (1):
  yarn add electron --dev --exact
  yarn add v1…
  
  最后一步electron安装失败 ， 那就手工安装(当时也不知道安装哪个版本就直接不带版本号试试) ： 
    npm install electron --save-dev  
   结果失败
   网上浪。。啊。。浪 ，大多都是说要翻墙、要手工下载，要设置国内镜像
```
3. 安装 nrm (管理npm 仓库源的工具)
```
  查看仓库情况：
  >nrm ls

  npm ---------- https://registry.npmjs.org/
  yarn --------- https://registry.yarnpkg.com/
  tencent ------ https://mirrors.cloud.tencent.com/npm/
  cnpm --------- https://r.cnpmjs.org/
  taobao ------- https://registry.npmmirror.com/
  npmMirror ---- https://skimdb.npmjs.com/registry/
  
  由于网友说需要切换，那就切到 cnpm :
  nrm use cnpm 
  接着安装： npm install electron --save-dev  
  结果很顺利！  （可能当时网络运气比较好）
```
4. 进入my-new-app,启动应用：
- cd my-new-app &  npm start 
- 看到了一个 跟 Atom editor 一样的窗体

## 其他
- 如果用了 electron-forge init , 那可能你 启动应用时候会报错：提示你 xxxx  electron-prebuilt-compile  必须在devDependencies中（原提示语记不清了）
- 然后天真的去项目下执行 npm install electron-prebuilt-compile --save-dev，
- 结果报错，根据错误最后日志文件看，他去registry中下载 electron-v8.2.0-win32-x64,而手工访问很多仓库（如淘宝之类的）根本就没有这个版本，最小的也是v8.5.x
- 也许这就是electron-forge readme中不推荐electron-forge cli 方式取 init 的原因吧

## 使用Autodesk的APS云服务修改自定义的参数化模型的参数值的POC
### 方案A 完全基于APS 提供的各类RestAPI
### 方案B 基于Visual Studio Code 的APS 插件(Autodesk Platform Services)
  - 该APS插件已经对APS的服务进行了分组，通过鼠标操作即可完成修改参数
    - 可以使用官方提供的一个修改参数的万能 AppBundle 
  - APS 插件截图
    - <img width="354" height="330" alt="image" src="https://github.com/user-attachments/assets/45a4127a-a219-4f4f-a2dc-4245e30d5019" />
    - <img width="1786" height="829" alt="image" src="https://github.com/user-attachments/assets/b77c5540-7dde-4dcf-9e42-59e37b667c70" />
  - 基本操作步骤
    - 创建OSS bucket 并上传目标参数化3D模型(在APS中推荐使用 .ipt ): 在 buckets & Derivatives下创建bucket 并在bucket下上传参数化模型文件
      - <img width="391" height="190" alt="image" src="https://github.com/user-attachments/assets/5918e398-d95d-413f-be71-120f276177c9" />
    - 上传AppBundle: 在Automation -> Owned App Bundles 中上传万能AppBundle 并 在bundle创建成功后的子项 alias下为该bundle创建一个别名(后续需要用的)
    - 创建Activity: 在 Automation -> Owned Activities 上右键菜单中选择 create activity （一个成功创建的Activity 如下图），成功后为activity创建alias(后续创建workitem需要)
      - <img width="1424" height="723" alt="image" src="https://github.com/user-attachments/assets/e6337788-7d55-452c-a589-9a13abb93f94" />
    - 提交workitem: 在activity的alias上右键选择 Create WorkItem(workitem中自动会出现其关联的AppBundle、activity的相关参数)
      - <img width="1445" height="423" alt="image" src="https://github.com/user-attachments/assets/5d55ab79-5cf0-49f9-a7e3-59730371fb9b" />
      - inputParams示例 `data:application/json,{"width":"50 in", "height":"20 in"}`                                


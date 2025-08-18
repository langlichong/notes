单元测试：只会用几个合法 PNG 文件去测。

Parameterized Tests: 

PBT 测试：可能会写性质，比如“解析后再序列化再解析结果相同”。

Fuzzing 测试：会自动生成随机的二进制数据、乱七八糟的 PNG 文件，甚至带恶意 payload，看看函数是否会：

  抛出异常
  
  内存溢出
  
  无限循环

  触发安全漏洞
TDD
BDD

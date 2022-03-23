最近一个项目中很典型的查询优化问题，跟大家分享一下，SQL 如下，SQL 文本有1.8MB
```
 select
        count(1)
from
        V_XXXXXXXXXXXXXX t
where
                                        C1= '235432'
                                    and C2= '345436'
    and
        (
                C3  = 'SADFDSGADFDSAFDSAFSAD'
             or C3 is null
        )
    and
        (
          id in ('ERTRTEWEB4DF2BE413523615EFDBA', 
              'ERTETRET2A7C44AE83EFEC5DD4169FA2', 'FF053E459ERTRETRETR755D70B6C1712', 
              '057ERWTETETRETRETRRRD8738ED5D886', '0518C9DERWTRETRETREE63B5346B38B3', 
              '3E50D3EF6ERTRERTRTREE6920014CD55', '421FA8BERTERTEWTEWRTREA1181A059A', 
              '31E2F34EWRTREWRTE31F72CA0563E4C9', '356EWRTREWTREWGFD1BE5DB4A4A39BEE', 
                 ................此处省略数万行
              '8BEE2AERTEWTR70885B6421166C3A6C5', '296E705ERTRETWHG456196D973439599')
        )
 
```
一个多表连接的比较复杂的视图，SQL 的过滤条件里面 ID 列 IN 了几万个常量，执行计划还好，但是 SQL 第一次执行需要 7s，第二次执行时间为毫秒级。

说明该语句执行时间主要消耗在 SQL 硬解析上，由于该功能并发量较大，引发了严重的性能问题。

这种问题比较普遍，开发人员图简单，对 IN 列表里面值的个数没有评估，动辙数万，甚至数十万，这种 SQL 并发多了就是灾难。
优化思路如下：
```
1、创建一个事务级的临时表
CREATE GLOBAL TEMPORARY TABLE TMP_INLIST
(
 ID VARCHAR(100)

 ) ON COMMIT DELETE ROWS;
 ```

2、将需要参与过滤的常量值插入临时表
```
--addBatch()批量绑定参数
INSERT INTO TMP_INLIST VALUES(?);
```
3、修改SQL语句
```
select
        count(1)
from
        V_XXXXXXXXXXXXXX t
where
                                        C1= '235432'
                                    and C2= '345436'
    and
        (
                C3  = 'SADFDSGADFDSAFDSAFSAD'
             or C3 is null
        )
    and
        (
          id in (select id from TMP_INLIST)
        );
```

这样不管 IN 列表里面有多少个常量，SQL 解析的代价都是一样的，性能问题得到解决

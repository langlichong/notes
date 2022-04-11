Schema和DataBase是否等同？
 涉及到数据库的模式有很多疑惑，问题经常出现在模式和数据库之间是否有区别，如果有，区别在哪里。取决于数据库供应商对schema（模式）
 产生疑惑的一部分原因是数据库系统倾向于以自己的方式处理模式
（1）MySQL的文档中指出，在物理上，模式与数据库是同义的，所以，模式和数据库是一回  事。
（2）但是，Oracle的文档却指出，某些对象可以存储在数据库中，但不能存储在schema  中。 因此，模式和数据库不是一回事。
（3）而根据这篇SQL Server技术文章SQLServer technical article，schema是数据库SQL Server内部的一个独立的实体。 所以，他们也不是一回事.

创建Schema
    尽管上述三个DBMS在定义schema方面有所不同，还是有一个共同点，就是每一个都支持CREATE SCHEMA语句。
    MySQL在MySQL中，CREATE SCHEMA创建了一个数据库，这是因为CREATE SCHEMA是CREATE DATABASE的同义词。 换句话说，你可以使用CREATE SCHEMA或者CREATE DATABASE来创建一个数据库。

Oracle Database
     在Oracle中，CREATE SCHEMA语句实际上并不创建一个模式，这是因为已经为在创建用户时，数据库用户就已经创建了一个模式，
也就是说在ORACLE中CREATE USER就创建了一个schema，CREATE SCHEMA语句允许你将schema同表和视图关联起来，并在这些对象上授权，从而不必在多个事务中发出多个SQL语句。

SQL Server
    在SQL Server中，CREATE SCHEMA将按照名称创建一个模式，与MySQL不同，CREATE SCHEMA语句创建了一个单独定义到数据库的模式。
    和ORACLE也不同，CREATE SCHEMA语句实际创建了一个模式(前面说到这个语句在ORACLE中不创建一个模式)，在SQL Server中，一旦创建了模式，就可以往模式中添加用户和对象.
    
    
 ------------------------------------------------Schema Database概念通俗比喻----------------------------------------
“我们可以把Database看作是一个大仓库。仓库分了很多很多的房间，Schema就是其中的房间。一个Schema代表一个房间。
Table可以看作是每个Schema中的床，Table（床）被放入每个房间中，不能放置在房间之外，那岂不是晚上睡觉无家可归了。
然后床上可以放置很多物品，就好比 Table上可以放置很多列和行一样，数据库中存储数据的基本单元是Table。现实中每个仓库放置物品的基本单位就是床，
User就是每个Schema的主人（所以Schema包含的是Object，而不是User），user和schema是一一对应的，
每个user在没有特别指定下只能使用自己schema（房间）的东西。如果一个user想使用其他schema（房间）的东西，
那就要看那个schema（房间）的user（主人）有没有给你这个权限了，或者看这个仓库的老大（DBA）有没有给你这个权限了。
换句话说，如果你是某个仓库的主人，那么这个仓库的使用权和仓库中的所有东西都是你的（包括房间），你有完全的操作权，
可以扔掉不用的东西从每个房间，也可以放置一些有用的东西到某一个房间，你还可以给每个User分配具体的权限，
也就是他到某一个房间能做些什么，是只能看（Read-Only），还是可以像主人一样有所有的控制权（R/W），这个就要看这个User所对应的角色Role了。”


MySql:
Conceptually, a schema is a set of interrelated database objects, such as tables, table columns, data types of the columns,
indexes, foreign keys, and so on. These objects are connected through SQL syntax, because the columns make up the tables, 
the foreign keys refer to tables and columns, and so on. Ideally, they are also connected logically, 
working together as part of a unified application or flexible framework. 
For example, the INFORMATION_SCHEMA and performance_schema databases use “schema” in their names 
to emphasize the close relationships between the tables and columns they contain.
In MySQL, physically, a schema is synonymous with adatabase. You can substitute the keyword SCHEMA instead of DATABASE in MySQL SQL syntax, 
for example using CREATE SCHEMA instead of CREATE DATABASE.  Some other database products draw a distinction. 
For example, in the Oracle Database product,  a schema represents only a part of a database: the tables and other objects owned by a single user.

--------------------------------------------------------------------

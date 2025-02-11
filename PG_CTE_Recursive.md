### 数据准备及执行过程分析
1. create table employees
   ```sql
   CREATE TABLE employees (
      employee_id SERIAL PRIMARY KEY,
      full_name VARCHAR NOT NULL,
      manager_id INT
    );
   ```
2. insert data
   ```sql
   INSERT INTO employees (employee_id, full_name, manager_id)
    VALUES
      (1, 'Michael North', NULL),
      (2, 'Megan Berry', 1),
      (3, 'Sarah Berry', 1),
      (4, 'Zoe Black', 1),
      (5, 'Tim James', 1),
      (6, 'Bella Tucker', 2),
      (7, 'Ryan Metcalfe', 2),
      (8, 'Max Mills', 2),
      (9, 'Benjamin Glover', 2),
      (10, 'Carolyn Henderson', 3),
      (11, 'Nicola Kelly', 3),
      (12, 'Alexandra Climo', 3),
      (13, 'Dominic King', 3),
      (14, 'Leonard Gray', 4),
      (15, 'Eric Rampling', 4),
      (16, 'Piers Paige', 7),
      (17, 'Ryan Henderson', 7),
      (18, 'Frank Tucker', 8),
      (19, 'Nathan Ferguson', 8),
      (20, 'Kevin Rampling', 8);
   ```
3. Cte recursive sql
   ```sql
       WITH RECURSIVE subordinates AS (
        SELECT
          employee_id, manager_id, full_name
        FROM employees   WHERE employee_id = 2
        UNION
        SELECT
          e.employee_id, e.manager_id,e.full_name
        FROM employees e
        INNER JOIN subordinates s ON s.employee_id = e.manager_id
      )
      SELECT * FROM subordinates;
   ```
4. 查询结果截图
   
![image](https://github.com/user-attachments/assets/b554037a-c0fa-441a-a26e-6a3f5f8452ef)

5. 执行过程
   
a. 执行cte anchor 查询作为初始化查询，通常都是单条记录,:

![image](https://github.com/user-attachments/assets/20ebd12f-f58d-4cec-b189-2a3bb4665434)

b. 以a中查询结果作为临时表 执行 cet查询语句的递归SQL部分, 即从源数据表 employee中过滤出 manager_id 等于初始化查询语句的结果中的 employee_id的所有记录，即类似执行下述语句

![image](https://github.com/user-attachments/assets/63bedc76-18de-4df3-8e26-f11294d647d5)

c. 在 b 步骤执行完毕后得到一个结果集，如果不为空（为空则整个递归结束了），则将 b 中结果集与 源数据表做关联查询，类似执行如下语句：

![image](https://github.com/user-attachments/assets/25f3a1ae-b255-4eb5-8cac-dc970712a94f)

d. 步骤 c 执行完毕后又得到一个结果集，继续将该结果集（看作临时表）与 递归语句关联查询，类似如下：

![image](https://github.com/user-attachments/assets/ffb2228b-c3bf-495f-8401-4ba2eb078fdd)

> 步骤 d 执行结果为空，则整个递归过程结束


e. 将 步骤 a, b, c, d 中的所有结果集使用 union 进行合并 得到最终结果集 即 subordinates

f. `SELECT * FROM subordinates;` 查询到最终数据

> 递归过程其实是使用不同临时结果集去 关联 源数据表(每次临时查询出的结果集会作为 cte 基表，然后关联 源数据表)  employees 得到新的数据集，最后将每次关联结果集并起来


### 正向与逆向查询示例
```sql

      -- 正向查询：查询自某个节点后的所有子孙节点 （从根节点到叶子节点）
      with recursive cte(id, build_name, parent_id)
      as (
      
          select id, bus_building.build_name, parent_id from bus_building where parent_id = '0'
      
          union
      
          select b.id, b.build_name , b.parent_id
          from bus_building b
          join cte on cte.id = b.parent_id
      )
      select * from cte;
      
      -- 反向查询: 查询某建筑节点的所有父辈节点（叶子节点到根节点）
      with recursive cte(id, build_name, parent_id)
      as (
      
              select id, bus_building.build_name, parent_id from bus_building where id = '1846457310377971713'
      
              union
      
              select b.id, b.build_name , b.parent_id
              from bus_building b
              join cte on cte.parent_id = b.id
      )
      select * from cte;
```


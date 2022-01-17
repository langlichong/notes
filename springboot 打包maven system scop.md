

## spring-boot 项目打包直接引入的本地jar文件
### spring-boot maven引入了一个scope 为system的jar，直接在Idea 中运行正常，但是打包后可能会报找不到类的错误，修正如下：


```
  网上一大堆要配置 resource 的操作，其实那种更容易引发资源其他引入问题，spring boot 中正确做法是只需要加入<includeSystemScope>true</includeSystemScope>即可
   <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <excludes>
                <exclude>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                </exclude>
            </excludes>
            <includeSystemScope>true</includeSystemScope>
        </configuration>
    </plugin>
```

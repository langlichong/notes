### 工具  重试  错误原因
- JOL: Library , 让你看对象本身
- HSDB: JDK Internal Tools 让你看类元数据（Klass + vtable + 字段布局）

### Java 对象会这么臃肿？（三座大山）
> 在 Java 的内存模型里，一个对象包含的不仅仅是你定义的字段，还有：
- 对象头（Header）： 至少 12 字节。哪怕你是个空类。
- 继承开销（Inheritance Baggage）： 就像你发现的，哪怕是私有字字段，子类也必须物理持有且占用内存。如果继承树很深（比如 5 层继承，每层加点字段），一个末端子类对象就会变得非常巨大。
- 对齐填充（Padding）： 为了 CPU 读取效率，对象大小必须是 8 的倍数。如果你背着的“行李”刚好让你超过了 32 字节一点点，JVM 就会强行帮你填满到 40 字节。

`结论： 在 Java 里，想存几个 byte 数据，最终在堆里可能要消耗 24 或 32 字节。这种“空间膨胀率”非常惊人`

> JDK 官方的终极优化方案：Project Valhalla
> 
**_在 Project Valhalla 正式落地之前，Java 社区已经形成了一套共识来应对这种内存开销。这就是你经常听到的：多用组合，少用继承（Composition over Inheritance）_**

### Layout 
1. Mark Word 的最后三位是锁状态的“信号灯： mark word中identity_hashcode是懒加载，第一次调用 hashCode() 相关方法时，JVM 才会计算并将其写入 Mark Word
	- 正常/无锁 (Unlocked)
	- 偏向锁 (Biased)： 可偏向的对象实例？
	- 轻量级锁 (Lightweight Locked)
	- 重量级锁 (Heavyweight Locked)
	- GC 标记 (Marked for GC)

2. hashcode 
	- 每个对象有2个hashcode, 一个是逻辑的(hashcode()值)，另一个是物理的(jvm生成的，即使垃圾回收挪了位置，其值也不变, System.identityHashcode(xxx))
	
3. 内存对齐（不对齐会引发并行或并发时候的一致性,原子性、另一个是cpu效率问题）
	- L1, L2, L3 想象成大小不一的仓库，而“缓存行”是仓库里堆放货物的标准集装箱
	- 字长 (Word Size)
	- 缓存行 (Cache Line):  CPU 从内存向高速缓存（L1/L2/L3）搬运数据的最小单位。
	- 伪共享： Java 8 引入了 @Contended 注解，它的原理就是——主动增加填充 (Padding)，把变量强行挪到不同的缓存行去
	- 查看jvm对齐参数: 
		- java -XX:+PrintFlagsFinal -version | findstr ObjectAlignmentInBytes 
		- jinfo -flag ObjectAlignmentInBytes <PID>
	- 优化技巧：在分配内存时，如果 32GB 刚好不够用
		- 策略 A： 优化代码，缩减对象，死守在 31GB 以内，享受压缩红利
		- 策略 B： 如果一定要扩，就直接跳到 48GB 或更高。因为 32G 到 40G 这个区间基本上是在为“指针膨胀”买单，实际可用空间根本没增加多少，反而变慢了
	
4. Array 
	- 其内存结构比普通对象多了长度信息(Mark Word、Klass Pointer、Array length)
	
5. vtable
	- 多态基础
	-  vtable 的索引是固定的
	- vtable（虚方法表） 本质上是一个函数指针数组。它存储在 Klass 对象（属于元空间）的末尾
	- 子类的 vtable 会先完整拷贝父类的内容(如 0 号index是父类的方法地址，但子类中也有一个该方法，所以会覆盖0index处的地址为子类的重写过的方法地址)，运行期通过查看对象头的Klass Ponter会找到子类对象

6. 查看 Klass结构	
	- HSDB (HotSpot Debugger)： 这是 JDK 自带的图形化探测工具
	- 运行方式：java -cp %JAVA_HOME%/lib/sa-jdi.jar sun.jvm.hotspot.HSDB (旧版) 或 jhsdb hsdb (新版)
	- ./jhsdb.exe clhsdb --pid 3704

7. 继承与访问修饰符背后
   - B 继承 A,实际内存布局中B其实拥有所有A的东西(包含了私有及其他的): 若只继承public或protected，则对一个public方法x，若B的实例调用b.x(), x内部可能会调用A的私有方法，如果B内存结构中抛弃A的私有部分则肯定报错(反证法)
   - 可以使用 JOL lib 进行打印子类的实例 ClassLayout.parseInstance(子类实例).toPrintable()
   - 子类必须背着父类所有的行李（所有字段），哪怕有些行李你永远不准打开看：确实是Java 被诟病**“内存大户”**的核心原因之一

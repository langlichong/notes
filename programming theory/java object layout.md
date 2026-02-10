### 工具  重试  错误原因
- JOL: Library , 让你看对象本身
- HSDB: JDK Internal Tools 让你看类元数据（Klass + vtable + 字段布局）

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

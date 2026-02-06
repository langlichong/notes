- https://docs.oracle.com/javase/specs/jls/se17/html/jls-17.html

- bus lock

- cache line lock

  > The cache line is the smallest unit of data that can be transferred between the cache and the main memory
  >
  > **How does a cache line work?**
  >
  > Here's a step-by-step explanation:
  >
  > 1. **Cache line allocation**: When a CPU accesses a memory location, the cache controller checks if the requested data is already stored in the cache. If not, it allocates a cache line to store the data.
  > 2. **Cache line fill**: The cache controller reads the required data from the main memory and fills the allocated cache line with the data.
  > 3. **Cache line store**: When the CPU modifies the data in the cache line, the cache controller stores the updated data in the cache line.
  > 4. **Cache line eviction**: When the cache is full and a new cache line needs to be allocated, the cache controller evicts the least recently used (LRU) cache line to make room for the new one.

- CAS: (v,e,n)=>(当前值，期望值, 新值)

  > 只能针对一个共享变量，利用AtomicReference 可以将cas锁施加到对象上，但可能出现ABA问题
  >
  > AtomicStampedRdference 通过加时间戳(或叫加版本解决ABA)
  >
  > 本质是一个自旋锁(spin lock), 浪费CPU cycle

- Java's concurrency model interacts with the underlying hardware ??

- Unsafe

  > JDK9 之后，官方推荐使用 `java.lang.invoke.Varhandle`
  >
  > Generally, **a variable handle is just a typed reference to a variable**. The variable can be an array element, instance, or static field of the class
  >
  > VarHandle(变量句柄)支持不同访问模型下对于变量的访问，包括简单的read/write访问，volatile read/write访问，以及CAS访问,VarHandle通过**MethodHandles**类中的内部类**Lookup**来创建



### JVM  Concurrency  practice

- synchronized : object monitor (lock) , wait set 
- locks:  LockSupport , ReentrantLock
- atomic: AtomicInteger .....
- AQS

### JMM

> **Happens-before as a description of intent**
>
> Happens-before is a way to describe the relationships between events in a concurrent program, and it's a way to express the intent of the programmer. It's a way to say "I want these events to happen in this order" or "I want this event to happen before that event".
>
> **Happens-before is not a guarantee**
>
> However, happens-before is not a guarantee that the events will actually happen in the desired order. It's a description of the desired behavior, but it's up to the programming language, the runtime environment, and the underlying hardware to ensure that the events actually happen in the desired order.
>
> **Happens-before is a will**
>
> In a sense, happens-before is a will, a desire, a intention. It's a way to express what we want to happen, but it's not a guarantee that it will actually happen.
>
> **The relationship between happens-before and synchronization**
>
> Synchronization primitives, such as locks and semaphores, are used to ensure that the events happen in the desired order. They are used to enforce the happens-before relationships that we have described.
>
> **The relationship between happens-before and concurrent programming**
>
> Concurrent programming is all about describing the relationships between events, and happens-before is a fundamental concept in concurrent programming. It's a way to describe the desired behavior of a concurrent program, and it's a way to ensure that the program behaves correctly

- visible: 一个线程修改了共享变量，其他线程要能立即感应到该修改

  > 要保证**可见性**，思路也很简单，变量写入主存后，把其他线程缓存的该变量清空，这样其他线程缓存未命中，就会去主存加载.
  >
  > `Java`中提供了`volatile`修饰变量保证**可见性**

> jmm 屏蔽了不同硬件结构下差异，类似 jvm `J M M`抽象结构划分为线程本地缓存与主存，每个线程均有自己的本地缓存，本地缓存是线程**私有**的，主存则是计算机内存，它是**共享**的。
>
> 数据的操作遵循：本地缓存中无时，从主存拷贝到缓存，然后操作缓存

- atomic

  > 有了可见性，但可见性的操作涉及多条指令，在并发场景下，如果线程的执行出现了交替现象，则visible操作被打断，导致最后数据不一致性

- ordering: 有序性

  > volatile: 禁止编译器进行指令重排

- > object monitor & wait set : 
  > 	operations: wait , notify, notifyAll
  > 	limitations: 
  > 		粗粒度: 实施三类操作的前提是拥有monitor , 只有三种方案: 同步块, 同步实例方法, 同步静态方法
  > 		性能问题: 读-读可并发的
  > 	
  > 	
  > advanced : java.util.concurrent
  > 		ReentrantLock,ReadWriteLock
  > 		Atomic系列
  > 		LockSupport系列
  > 		concurrent framework: ExecutorService, ThreadpoolExecutor, Future,Fork/join
  > 		concurrent collections: ConcurentHashMap, ConcurrentLinkedQueue,CopyOnWriteList 
  > 		concurrent primitives: Semaphore , CycleBarrier, Phaser, Exchanger,CountDownLatch
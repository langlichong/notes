# Computer Resources

### Computation

- CPU

### Memory: 

- Persistent memory
  - Hard driver or SSD
- Volatile memory
  - RAM


##### RAM

- Stack
  - Function Arguments
  - Local Variables
  - Known size at compile time
  - size: Dynamic / Fixed upper limit
  - Cleanup: Automatic  / When function returns

#### Heap

- Values that live beyond a function's lifetime
- Values accessed by multiple threads
- Large values
- Unknown size at compile time
- Size: Dynamic
- Lifetime: Determined by programmer
- Cleanup: Manual

#### Static

- Program's binary
- Static variables
- String literals
- Size: fixed
- Lifetime: Lifetime of program
- Cleanup: automatic When program terminates



## Heap Memory Managing

![image-20251003173345823](./Computer Resources.assets/image-20251003173345823.png)



# RAII 

> 利用析构函数 + 对象（Wrapper了原始对象）分配到栈上(不使用new)

### What is a resource

![image-20251003174023515](./Computer Resources.assets/image-20251003174023515.png)

![image-20251003174256818](./Computer Resources.assets/image-20251003174256818.png)

![image-20251003174411932](./Computer Resources.assets/image-20251003174411932.png)

## Solution

![image-20251003174545295](./Computer Resources.assets/image-20251003174545295.png)

![image-20251003174626946](./Computer Resources.assets/image-20251003174626946.png)

![image-20251003174858968](./Computer Resources.assets/image-20251003174858968.png)

![image-20251003175039798](./Computer Resources.assets/image-20251003175039798.png)

- only one owner

![image-20251003180611714](./Computer Resources.assets/image-20251003180611714.png)

- shared 

  ![image-20251003180907875](./Computer Resources.assets/image-20251003180907875.png)

### Ownership Based Resource Management (OBRM)

![image-20251003181230824](./Computer Resources.assets/image-20251003181230824.png)

- Rust OBRM VS C++ RAII

  > Rust中ownership不仅用于内存管理，而且用于资源管理,如文件句柄、网络socket等

  ![image-20251003181612328](./Computer Resources.assets/image-20251003181612328.png)

- 所有权转移对比

  ![image-20251003181810053](./Computer Resources.assets/image-20251003181810053.png)

- 共享所有权对比

  ![image-20251003182102818](./Computer Resources.assets/image-20251003182102818.png)

### Cons

![image-20251003223550038](./Computer Resources.assets/image-20251003223550038.png)

### Tips

- primitives that are entirely stored on stack，即primitive 类型的变量赋值给其他变量 或传递到函数中不会发生move，而是clone
- 赋值发生move: 一个变量赋值给另一个会发生所有权转移
- 所有权转移到函数:  将变量传递到函数中同样会发生所有权转移
- 所有权转移出函数: 函数返回值情况， 如函数返回 String 



### Borrowing

<img src="./Computer Resources.assets/image-20251003225740279.png" alt="image-20251003225740279" style="zoom:60%;" />

![](./Computer Resources.assets/image-20251003230057117.png)
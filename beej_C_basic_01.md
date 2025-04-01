## Compilation Details
- Preprocessor
- Compiler

## variables
- variable is human-readable name refers to some data in memory
- variable holds values
> Think memory as a big array of bytes, array's index can be called address or location or a pointer
>
> memory's numberic address is hard to remember , so make a name for it instead , so called **variable**
>
> so **pointer** is also a variable that hold the address of other variables

## variable type
- C make effort to convert automatically between numeric types
- Any other types conversion  except numeric  are mannual

## boolean
- C has no builtin boolean type
- in C , `0` means false , and non-zero means true
- `<stdbool.h>`: define a type named `bool` and with two value `true` and `false`

## Operators
- Ternary Operator(or ask expression): ` y += x > 10 ? 17: 12 `
- Pre-and-Post increment or decrement: `++i` , `--i`,    `i++`,  `i--`
> pre-increment and pre-decrement, firstly the variable is incremented or decremented and then evaluate with the new value
>
> post-increment and post-decrement just opposite (firstly do evaluate then do increment or decrement)
>
> ` i = 10; j = 5 + i++;  // Compute 5 + i, _then_ increment i ` , `j = 5 + ++i;  // Increment i, _then_ compute 5 + i`
- Pre and Post are used frequently with array and pointer access and manipulation
> `for (int i=0; i < 10; i++)`
- Comma Operator: `x=5 ,y = 10;`
> comma expression's result is the mostright one's value:  `x = (1, 2, 3); // finaly x will be 3`
>
> comma operator mostly used in for expression like `for(int x=0,y=0; x<100; x++,y++){}`
- sizeof: tell the momory size(in bytes) of a type or an expression or a variable
> sizeof's return type is **size_t** , neither int  or unsigned int, and use `%zu` to printf
>
> sizeof is computed at compile-time
## FLow Control
- if: can be in oneline with only one statement without braces (while and for and other loops work the same )
- if-else, if-else-if
- while, do-while
- for, switch(just work for int or character  or enum types)
> switch Not having a break is called **fall through** (without re-checking the condition.)

## Functions
- Parameter is a copy of argument,not the argument itself.
- function uses a copy of the argument
- **Passing by value**: **the expression passed to function will be firstly evaluated , the copy the evaluated value to the argument**
- **Passing by reference**
```text
   EVERYTHING you pass to a function WITHOUT EXCEPTION is copied into its corresponding parameter,
  and the function operates on that local copy, NO MATTER WHAT. Remember that,
  even when we’re talking about this so-called passing by reference.
```
>
> If the argument is a variable, a copy of the value of that variable gets made and stored in the parameter.
>
> **parameter(placeholder)**: A ​variable in a function/method ​declaration that defines what input it accepts.
>
> **argument(Actual value)**: The actual value passed to a function when it is called.
- Function Prototypes
```text
 Generally , you had to define the function before you used it.
 above isn’t quite strictly true.
 You can notify the compiler in advance that you’ll be using a function of a certain type that has a certain parameter list.
 That way the function can be defined anywhere (even in a different file),
 as long as the function prototype has been declared before you call that function.
```
- Recommend to use `void` when the function paramter is empty

## Pointers
```text
Each byte of memory is identified by an integer. These integers increase sequentially as you move up through memory.
But, not all data types use just a byte, For instance, an int is often four bytes.
think of it as a bunch of numbered boxes, where each box holds a byte45 of data.
Or like a big array where each element holds a byte, if you come from a language with arrays. The number that represents each box is called its address.
```
- **pointer** is a variable that holds an address.
- a pointer variable holds that address number.
> the address of the house is a pointer to that house. It’s not the house itself, but it tells you where to find it.
- **address-of operator**: `&`
- `%p`: used to print a pointer for printf
```text
 pointer is going to look like a garbage number (and it might be printed in hexadecimal47 instead of decimal).
 it is merely the index into memory the data is stored in.
 Or the index into memory that the first byte of data is stored in, if the data is multi-byte.
```
- **Virtual Memory**
```text
 Makes processes think they have the entire memory space of your computer to themselves, regardless of how much physical RAM backs it up.
 So even though the address was that huge number(even more than physical memory size),
 it’s being mapped to some lower physical memory address by the virtual memory system of  CPU.
```
- Pointer Arithmetic: `++`, `--`
> array pointer: `a[b] == *(a+b)`

### Pointer types
- asterisk `*`: used to identify pointer type.
```c
    int *p; // p's type is "pointer to an int", or "int-pointer"
    // int* (read “int-pointer”).
```
- void pointer: pointer to a thing that you don’t know the type of.

### Dereferencing
```text
A pointer variable can be thought of as referring to another variable by pointing to it.
When you have a pointer to a variable (roughly “a reference to a variable”)
```
- get access to the original variable
```text
if you have a variable called i, and you have a pointer to i called p,
you can use the dereferenced pointer p exactly as if it were the original variable i!
```
- **indirection operator**: the dereference operator, also asterisk operator `*`
```text
 what is the dereference operator? It’s actually called the indirection operator, because you’re accessing values indirectly via the pointer.
 Indirection operator does is tells the computer to use the object the pointer points to instead of using the pointer itself.
```
- passing pointer as argument
```text
 function use copy for argument.
 This is analogous to writing a home address on a piece of paper, and then copying that onto another piece of paper.
 You now have two pointers to that house, and both are equally good at getting you to the house itself.
```
- **NULL Pointer**: `NULL`
- **pointer to pointer**

## Arrays
- syntactic sugar of ponter
- must given size when declare array `float f[4]`
- get array's size: use `sizeof` and the type size
- char array size is equal to `sizeof` result (sizeof(char) only just number 1)
- __“pass” arrays to functions, you’re only passing a pointer to the first element__
```c
    int x[12];
    int size = sizeof x / sizeof(int);// exclude char array
```
- initialize
```c
 // with constant
 int a[5] = {1,2,3,4,5};

 //init with less than array size'e elements ,lefting the rest be init to zero
 int a[5] = {1,2,3};

 // set entire array to zero
 int a[100] = {0};

 //use constant
 #define  COUNT 5
 int a[COUNT] = {[COUNT-3]=1,2,1};

 //let compiler to compute
 int a[] = {1,2,3,4}
```
- C doesn’t stop you from accessing arrays out of bounds. It might not even warn you. (thus causes undefined behavior.)
```c
  int a[3] = {1,2,3};
  // can access element over the size of array , dangerous !!!
  printf("fourth element is: %d", a[3]);
```
- pointer and array
```text
  when say pointer to an array means pointer to the array's first element
  and there's a shorthand for pointer to first element of an array
```
```c
    int a[3];
    //notice: p is int* , and *p is same as a[0]
    int *p = &a[0];
    // same  as or shorthand
    int *p = a;
```

## Strings
- strings aren’t actually strings in C.They’re pointers. similar exist of array
- string literals: sequence of characters in double quotes. `"text"`
- string variable: `char s* = "some text";  printf("%s\n", s);`
- string variable as arrays: `char s[] = "Hello,World!";`
- print char and string : `%s` , `%c`
- initialize style
```c
  // flavor 1: can not be change similar immutable
  // if you have a pointer to a string literal, don’t try to change it
  char *s = "some text";
  s[0] = 'z' // bad


  //flavor 2: s is an array copy of the string and can be mutate
  // use a string in double quotes to initialize an array, that’s not actually a string literal.
  char s[] = "some text";
  s[0] = 'z'; // no problem
```
- get lenght: use `strlen()` of  `<string.h>` and return type `size_t`
- string termination:
```text
  Store the bytes of the string, and mark the end of the string with a special byte called the terminator.

  In C, a “string” is defined by two basic characteristics:
  - A pointer to the first character in the string.
  - A zero-valued byte (or NUL character65) somewhere in memory after the pointer that indicates the end of the string.
    (A NUL character can be written in C code as \0)

  Note: When you include a string in double quotes in your code, the NUL character is automatically, implicitly included.
```
- copy string
> can not use `=` , which will be a pointer copy of first character, use `strcpy` of `string.h`

## structs
- pass to function
- arrow operator
- padding:the compiler is free to add padding to a struct as it sees fit. And different compilers might do this differently.

## File input/output
- `FILE*` data type
- stdin, stdout, stderr
- read text file: `fopen` , `fclose`, `fgetc`,`fgets`
- end of file: `EOF`,  is a special character defined as a macro: EOF
> This is what fgetc() will return when the end of the file has been reached and you’ve attempted to read another character.
- binary file IO

## Manual Memory Allocation
- **automatic variables**: variables are automatically allocated and deallocated when they come into scope and leave scope.
- manual memory management
```text
You can tell C explicitly to allocate for you a certain number of bytes that you can use as you please.
And these bytes will remain allocated until you explicitly free that memory

- memory leak
```
- Allocating and Deallocating: `stdlib.h`: `malloc` and `free`
- Changing Allocated Size with `realloc()`
- memory alignment: Aligned Allocations

// start with hash sign(#) called directives that will be processde by preprocessor before compile the code
/**
 * preprocessor: hash sign (#)
 * preprocessor definition: #define
 * namespace: using namespace xxx , similar to java's static import
 */


#include <iostream>
#include <string>
#include <sstream>
//namespace
using namespace std;
#define PI 3.1415
#define NEW_LINE '\n'
/**
 * variable and data type: character 、numberical and integer 、floating-number( with different levels of precision)、bool
 * besides the arithmatic types there are another two types : nullptr 、void
 * compound types: e.g. string , which contains in header <string>
 *  char: fixed length occupy 1 byte, but other types has a dynamic length decided by implementation of a particular comipler and platform
 *  numeric_limits: 对一个具体的编译器实现而言可以查阅到数据类型的大小限制信息
 *  sizeof: calculate type's size
 */

int main1() {
    int a,b,sum;
    std::cin >> a;
    std::cin >> b;
    sum = a + b;
    //std: standard character output
    // insertion operator (<<),
    std::cout << sum ;


    int c;
    std::cout << c;

    /**
     * variable initialization style:
     *  1. c-lang style : int x = 3;
     *  2. cpp style ,named constructor init: int y = (2);
     *  3. 2011 revision introduce : int z = {4};
     *
     *  auto type detect and refer: auto 、decltype
     *  auto keyword: int a = 3;  auto b = a;
     */

    // 3 中变量初始化
    int x = 3 ;// c-lang style initial
    int y = (2); // cpp style initial named constructor initialization
    int z = {4}; // revision of the C++ standard, in 2011  type identifier {initial_value};

    //自动类型推导 auto 与 decltype
    int foo = 0;
    auto bar = foo ;

    // compound types:
    //1. string , 必须导入 string header

    std::string
    str = "字符串";


    cout << str;


    /**
     * input/output
     *  stdandard : cin、cout、cerr、clog
     *  string: header <sstream> contains a class stringstream , its operations similar to standard io
     */

    // preprocessor ,headers ,hash sign , namespace, insertiong operator(<<)
    // variable and type


    //constants: literal constant typed contains : integer, floating-point, characters, strings, Boolean, pointers, and user-defined literals
    // literal constants have a type
    // R prefix can be combined with any other prefixes
    // preprocessor definition: #define   format like : #define identifier replacement
    //operators: assignment operator , 可以单独使用，也可以参与表达式运算 e.g. y =  2 + (x = 5) , equals to  x = 5 ; y = 2 + x
    // valid expression : x = y = z = 5; always from right-to-left.
    //Compound assignment: (+=, -=, *=, /=, %=, >>=, <<=, &=, ^=, |=)

    //comma operator: a = (b=3, b+2); => a=5, b=3
    // sizeof
    cout << sizeof str;


    clog << "clog output";

    // endl  and '\n': new-line, endl不仅换行且flush buffer if there any
    // flushing operation incurs a certain overhead, and on some devices it may produce a delay.



    // getline for a paragraph or sentences with tab or space among
    // header <sstream> stringstream: allowing extraction or insertion operations from/to strings in the same way as they are performed on cin and cout.

    // input/output : cin cout cerr clog  sstream's stringstream
    /**
     * flow control:
     *   selection:
     *      if (condition) single statement
     *      if (condition) { more than one statement }
     *      switch (expression): limited to constant expressions
     *
     *   loop:
     *      while (expression) statement
     *      do statement while (condition);
     *      for (initialization; condition; increase) statement;
     *      for ( declaration : range ) statement;  Range-based for loop
     *   Jump statement:
     *      break、continue、goto
     */

    /**
     * Function:
     *      type name ( parameter1, parameter2, ...) { statements }: has return value
     *      type name ( argument1, argument2 ...) { statements }: no return value , marked with void return type
     *      empty parameter list can be used instead of void: int  printmessage (void)
     *      a funciton call statement is exactly a expression
     *
     * Arguments passed by value and by reference:
     *      by value: copy and asign copy value to function parameters
     *      by reference: useful to access an external variable from within a function
     *                    references are indicated with an ampersand (&) following the parameter type: void duplicate (int& a, int& b, int& c)
     *                    When a variable is passed by reference, what is passed is no longer a copy, but the variable itself (similar to alias of the arguments)
     * cost of arguments passing:
     *      for a complex compound type , to do a by value passing may result certain overhead
     *      const references: string concatenate (const string& a, const string& b)
     *                     By qualifying them as const, the function is forbidden to modify the values of neither a nor b,
     *                     but can actually access their values as references (aliases of the arguments), without having to make actual copies of the strings.
     *  Inline functions:
     *      meaningful: Calling a function generally causes a certain overhead (stacking arguments, jumps, etc...),
     *                  and thus for very short functions, it may be more efficient to simply insert the code of the function where it is called,
     *                  instead of performing the process of formally calling a function.
     *      inline :  informs the compiler that inline expansion is preferred over the usual function call mechanism for a specific function
     *      Notes: Note that most compilers already optimize code to generate inline functions when they see an opportunity to improve efficiency,
     *              even if not explicitly marked with the inline specifier.
     *
     *  Default value for function parmaters: int divide (int a, int b=2)
     *
     *  Declaring function: prototype of a function can be declared without actually defining the function completely(先声明再使用，不做实现)
     *                      int protofunction (int first, int second);
     *                      int protofunction (int, int);
     *  Recursivity：Recursivity is the property that functions have to be called by themselves
     */

    /**
     * Overloads and Templates:
     *  overloads: two different functions can have the same name if their parameters are different;
     *             either because they have a different number of parameters, or because any of their parameters are of a different type
     *  function template: the ability to define functions with generic types, known as function templates
     *      template keyword and  template parameters: template <template-parameters> function-declaration
     *      These parameters can be generic template types by specifying either the class or typename keyword followed by an identifier
     *        <pre>
     *         // SomeType  be used anywhere in the function definition, just as any other type;
     *         // it can be used as the type for parameters, as return type, or to declare new variables of this type
     *           template <class SomeType>
     *           SomeType sum (SomeType a, SomeType b)
     *           {
     *              return a+b;
     *           }
     *        </pre>
     * Instantiating a template: applying the template to create a function using particular types or values for its template parameters
     *          instant templete: calling the function template as regular funciton: name <template-arguments> (function-arguments)
     *          e.g.   x = sum<int>(10,20);  sometimes the generic type could be absent  x = sum(10,20)
     *   multiple template parameters: template <class T, class U>
     *
     *   Non-type template arguments: The template parameters can not only include types introduced by class or typename, but can also include expressions of a particular type
     *                  e.g. template <class T, int N>
     *
     */

    /**
     * name visibility:
     *    scope: global scope(outside any block)  、   block scope,  local variables.
     *    Namespaces：  namespace identifier {named_entities}
     *    using: used in  function or global scope
     *    Namespace aliasing: namespace new_name = current_name;
     */

    /**
     *  Storage classes:
     *      static storage:  storage for variables with global or namespace scope is allocated for the entire duration of the program
     *      automatic storage: The storage for local variables is only available during the block in which they are declared
     *   Notes:
     *     - Variables with static storage (such as global variables) that are not explicitly initialized are automatically initialized to zeroes.
     *     - Variables with automatic storage (such as local variables) that are not explicitly initialized are left uninitialized, and thus have an undetermined
     */

    /**
     * main function's return value:
     *  1. without encountering a return statement the compiler assumes the function ends with an implicit return statement: return 0 ; for historical reasons
     */

    return  0;
}


void duplicate(int& a,int& b, int& c){

    a*=2;
    b*=2;
    c*=2;
}

template<class T>
T sum(T a, T b){
    T result;
    result = a + b;
    return result;
}



int globall;
int main(){

   cout << sum<int>(2,3) <<endl;
   int local;
   cout << globall << local << endl;

}

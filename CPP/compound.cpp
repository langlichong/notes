//
// Created by langlichong on 2024/3/12.
//
#include <iostream>

using namespace std;

/**
*  Compound Data Types
 *  Arrays:   type name [elements];
 *      notes: arrays are blocks of static memory whose size must be determined at compile time, before the program runs
 *      initial:
 *          int bar [5] = { 10, 20, 30 };  int baz [5] = { };
 *          int foo [] = { 16, 2, 77, 40, 12071 };
 *     Arrays as parameters: passed is its address.
 *     Library arrays:  C++ provides an alternative array type as a standard container ,defined in header <array>.
 *
 * Character sequences:
 *    - end of strings represented in character sequences is signaled by a special character: the null character, as '\0' (backslash, zero)
 *    initiated:
 *        - style1: char myword[] = { 'H', 'e', 'l', 'l', 'o', '\0' };
 *        - style2:  string literal  char myword[] = "Hello"; auto add '\0' to the end
 *    Notes: the '\0' is from C-lang , also named C-strings
 *    C++ also define a strings in header <string> so-called the two  C-strings and library strings
 *    c-strings and library strings co-convert:
 *       char myntcs[] = "some text";
 *       string mystring = myntcs;  // convert c-string to string
 *       cout << mystring;          // printed as a library string
 *       cout << mystring.c_str();  // printed as a c-string
*/

/**
 *  Pointers: The variable that stores the address of another variable
 *      - memory address: sigle byte as unit , consecutive addresses(地址连续)
 *      - The actual address of a variable in memory cannot be known before runtime
 *    Address-of operator (&)：abtain the memory address of a variable
 *    Dereference operator (*):  access the variable a pointer point to
 *    - & is the address-of operator, and can be read simply as "address of"
 *    - * is the dereference operator, and can be read as "value pointed to by"
 *    Declaring pointers: type * name;
 *    Pointers and arrays:
 *            - arrays work very much like pointers to their first elements
 *            - actually, an array can always be implicitly converted to the pointer of the proper type
 *            - pointers can be assigned new addresses, while arrays cannot
 *        e.g.  int myarray [20];  int * mypointer;  myarray = mypointer;
 *
 *    Notes:
 *        offset operator: [index] 、pointer's arithmatic
 *        a[5] = 0;       // a [offset of 5] = 0
 *        *(a+5) = 0;     // pointed to by (a+5) = 0
 *   Pointer arithmetics:
 *      - only addition and subtraction operations are allowed
 *      - when adding one to a pointer, the pointer is made to point to the following element of the same type
 *    notes:
 *     ++:
 *        - as a prefix, the increment happens before the expression is evaluated
 *        - as a suffix, the increment happens after the expression is evaluated
 *      *p++   // same as *(p++): increment pointer, and dereference unincremented address
 *      *++p   // same as *(++p): increment pointer, and dereference incremented address
 *      ++*p   // same as ++(*p): dereference pointer, and increment the value it points to
 *      (*p)++ // dereference pointer, and post-increment the value it points to
 *   Pointers and const:
 *   Pointers and string literals: const char * foo = "hello";
 *   Pointers to pointers:
 *   void pointers: void pointers are pointers that point to a value that has no type (and thus also an undetermined length and undetermined dereferencing properties).
 *   Invalid pointers and null pointers:
 *   Pointers to functions:
 *
 */

/**
 * Dynamic Memory:  new 、new[] and delete、delete[]. returns a pointer to the beginning of the new block of memory allocated.
 *    pointer = new type
 *    pointer = new type [number_of_elements]
 *    分配失败情况： nothrow(使用nullptr进行判断) 或 bad_alloc 发生异常
 *  dynamic memory requested by our program is allocated by the system from the memory heap
 *
 *
 *  Dynamic memory in C：
 *     new、delete can not be used in C-lang  ,its cpp only
 *     in C , use library named  <stdlib.h> , in cpp the header name is <cstdlib> , these is available in cpp also !
 *
 */

/**
 *  Data Structure:
 *      a group of data elements grouped together under one name can have different types and different lengths.
 *     general structure:
 *     array structure:
 *          struct movies_t {
 *             string title;
 *             int year;
 *          } films [3];
 *    Pointers to structures:
 *            ->:  The arrow operator (->) is a dereference operator that is used exclusively with pointers to objects that have members
 *            "pointer->" equals to "(*pointer)."
 *
 *    a.b : Member b of object a
 *    a->b: Member b of object pointed to by a , equals to "(*a).b"
 *    *a.b: Value pointed to by member b of object a ,equals to "*(a.b)"
 *  Nesting structures:
 */

/**
 * Other Data types:
 *  Type aliases (typedef / using): typedef from C-lang , using is CPP
 *  Unions:
 *  Enumerated types (enum):
 */

/**
 * Classes： Classes are defined using either keyword class or keyword struct
 *     access specifier：  private, public or protected. ， default is private access
 *      scope operator (::, two colons):
 */

int main(){


}
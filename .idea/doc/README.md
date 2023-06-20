# Documentation

To use the compiler simply run the make command in the root directory of the project and
then run ./jlc path/to/file where path/to/file is the Javalette file to be compiled.
This will compile the Javalette file and output LLVM code to stdout.

The compiler has 3 parts:
- Lexer/Parser. For this part we used BNF Converter.
- Type Checker. This part will check the validity of the code to compile. If an error is detected, the program will return an error message that contains the type of error and the line of code that is problematic. At the end of this step, the code is considered valid.
- LLvm Backend. The program will generate llvm assembly code from the provided code. The generated code is written in a file 'program.ll'.

The specification of the Javalette language can be found in the file src/Javalette.cf.

We have implemented the following extensions:

- Arrays1 which allows to create arrays of elements of type 'int', 'double' and 'boolean'. The syntax is as described in the project description.
Note that for the declaration of the type of an arrays as int[] the 2 brackets must not be separated by a space.
The elements of these arrays are stored on the heap.

- Arrays2 allows to create arrays of higher dimensions. These arrays are treated as arrays of arrays and can be iterated by nested for loops.

- Pointers allows to define new data types (like struct in C). You can manipulate pointers to elements stored in the heap.
It is possible to define arrays of pointers.
Be careful when using similar variable and structure names because it can cause problems at compile time.

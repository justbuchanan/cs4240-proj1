
# Tiger Language Compiler

There is makefile in this folder to build the project.

After it is built, run it on a `.tiger` file with `java Driver myfile.tiger`

When run, the Driver will also create the following files:

* parser_rules.txt
* parser_table.csv
* first_sets.txt
* follow_sets.txt

Additionally, it prints a formatted Parse Tree, Abstract Syntax Tree, and Symbol Table to stdout.


# Register Allocation

The abstract base class `RegisterAllocator` provides the interface for our two allocators:

* `NaiveRegisterAllocator`
* `IntraBbRegisterAllocator`

Unfortunately we didn't complete our implementation of the extended basic block allocator, although we do generate a control-flow graph with extended basic blocks.

Each time the compiler runs, it builds a control flow graph of the IR code and writes a `cfg.dot` and `cfg.png` file.  This lets us quickly see what the graph looks like in picture form.

The register allocator to use is set in Driver.java, so they can be swapped out easily to try different strategies.


# MIPS Generation

The `MIPSGenerator` class accepts register-allocated IR code form the allocator and translates the IR code statements into actual MIPS instructions.

This will then write a `$filename.s` with the final result, which can then be run in SPIM

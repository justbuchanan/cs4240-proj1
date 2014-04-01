
# Tiger Language Compiler

There is makefile in this folder to build the project.

After it is built, run it on a `.tiger` file with `java Driver myfile.tiger`

When run, the Driver will also create the following files:

* parser_rules.txt
* parser_table.csv
* first_sets.txt
* follow_sets.txt

Additionally, it prints a formatted Parse Tree, Abstract Syntax Tree, and Symbol Table to stdout.

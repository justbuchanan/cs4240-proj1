
# Tiger Scanner && Parser

There is makefile in this folder to build the project.

After it is built, run it on a `.tiger` file with `java Driver myfile.tiger`

This will print a debug output to the console showing what is going on.  If the input file is a valid tiger program (and our code works), the last line will say 'Successful parse'.

When run, the Driver will also create the following files:

* parser_rules.txt
* parser_table.csv
* first_sets.txt
* follow_sets.txt

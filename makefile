# Replace this with your main class name
DRIVER=Driver

# Replace this with the class that contains all of your tests
TEST_CLASS=DoublyLinkedListTests

# Use all .java files in the directory
SRC=$(wildcard *.java)
OBJ=$(patsubst %.java, %.class, $(SRC))

# make the graph pngs
SRC_DOT=tiger_dfa.dot
OBJ_DOT=$(patsubst %.dot, %.png, $(SRC_DOT))

%.png: %.dot
	dot -Tpng $^ -o $@


# This list of files is included in the pkg.zip output for uploading to T-Square
# If you want to upload more than just the .java files, add them here
PKG_FILES=$(wildcard *.java) README.md $(wildcard *.png) $(wildcard test-input-ver-2/*) makefile

# class path
# CLASSPATH="junit-4.11.jar:hamcrest-core-1.3.jar:."
CLASSPATH="."

all: $(OBJ) $(OBJ_DOT)

run: all
	java -cp $(CLASSPATH) $(DRIVER)

ex5: all
	java $(DRIVER) test-input-ver-2/ex5.tiger &> ex5.log

ex1: all
	java $(DRIVER) test-input-ver-2/ex1.tiger &> ex1.log

ex2: all
	java $(DRIVER) test-input-ver-2/ex2.tiger &> ex2.log

ex3: all
	java $(DRIVER) test-input-ver-2/ex3.tiger &> ex3.log

ex6: all
	java $(DRIVER) test-input-ver-2/ex6.tiger &> ex6.log

tictactoe: all
	java $(DRIVER) test-input-ver-2/tictactoe.tiger &> tictactoe.log

debug: all
	jdb -cp $(CLASSPATH) $(DRIVER)

test: all
	java -cp $(CLASSPATH) org.junit.runner.JUnitCore $(TEST_CLASS)

pkg: $(PKG_FILES)
	zip pkg.zip $(PKG_FILES)

%.class: %.java
	javac -g -cp $(CLASSPATH) $^

clean:
	rm *.class *.png



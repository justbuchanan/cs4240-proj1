# Replace this with your main class name
DRIVER=Driver

# Replace this with the class that contains all of your tests
TEST_CLASS=DoublyLinkedListTests

# Use all .java files in the directory
SRC=$(wildcard *.java)
OBJ=$(patsubst %.java, %.class, $(SRC))

# make the graph pngs
SRC_DOT=$(wildcard *.dot)
OBJ_DOT=$(patsubst %.dot, %.png, $(SRC_DOT))

%.png: %.dot
	dot -Tpng $^ -o $@


# This list of files is included in the pkg.zip output for uploading to T-Square
# If you want to upload more than just the .java files, add them here
PKG_FILES=DoublyLinkedList.java Node.java Driver.java

# class path
# CLASSPATH="junit-4.11.jar:hamcrest-core-1.3.jar:."
CLASSPATH="."

all: $(OBJ) $(OBJ_DOT)

run: all
	java -cp $(CLASSPATH) $(DRIVER)

debug: all
	jdb -cp $(CLASSPATH) $(DRIVER)

test: all
	java -cp $(CLASSPATH) org.junit.runner.JUnitCore $(TEST_CLASS)

%.class: %.java
	javac -cp $(CLASSPATH) $^

clean:
	rm $(OBJ) *.png



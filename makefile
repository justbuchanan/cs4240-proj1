
SRC=$(wildcard *.dot)
OBJ=$(patsubst %.dot, %.png, $(SRC))

all: $(OBJ)

%.png: %.dot
	dot -Tpng $^ -o $@

clean:
	rm *.png

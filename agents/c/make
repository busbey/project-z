CFLAGS = -Wall -pedantic -DDEBUG
all: random smart
random: Agent.o Random.o
	gcc -Wall -pedantic -o random Agent.o Random.o
smart: Agent.o Smart.o
	gcc -Wall -pedantic -o smart Agent.o Smart.o
clean:
	rm -rf *.o random smart

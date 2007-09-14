CFLAGS = -Wall -pedantic -DDEBUG
BUG_PATTERN = '[B-N]'
DEFAULT_HOST = 'localhost'
DEFAULT_PORT = 1337
all: random smart
random: Agent.o Random.o
	gcc -Wall -pedantic -o random Agent.o Random.o
smart: Agent.o Smart.o
	gcc -Wall -pedantic -o smart Agent.o Smart.o
clean:
	rm -rf *.o random smart
hunter1go: smart
	./smart -h $(DEFAULT_HOST) -p $(DEFAULT_PORT) $(BUG_PATTERN)
hunter2go: random
	./random -h $(DEFAULT_HOST) -p $(DEFAULT_PORT)
server: Server.java StateWorker.java ChatMessage.java World.java Worker.java
	javac Server.java

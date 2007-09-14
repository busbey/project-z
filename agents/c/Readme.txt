Problem:
	Create a pair of agents (labeled [1-9]) that work together to hunt down bugs (labeled [B-N]) within an m by n map.
	
	Rounds are delimited by the server sending you a state update.  During each round you may send moves and chat messages back to the server.  The last move and message you send will be used by the server for the next set of updates.

	This kit includes a reference client implementation for the C programming language.

More Info:

	* Detailed Description: http://code.google.com/p/project-z/wiki/ProblemDescription
	* Client-to-Server network protocol: http://code.google.com/p/project-z/wiki/ActorProtocol
	* Server-to-Client network protocol: http://code.google.com/p/project-z/wiki/WorldStateProtocol

In this kit:
	* Readme.txt	- this guide
	* Makefile		- a gnu make file for building and running this sample and your implementation
	* Agent.h		- header with all constants and function definitions you'll need for dealing with the reference abstraction
	* Agent.c		- reference implementation to handle networking.
	* Random.c		- an example Agent that moves randomly each turn.
	* Smart.c		- an example Agent that moves probablisticly towards the nearest bug.  bugs are specified by a regular expression.
	* Server.jar	- a server implementation you can use to test.

To build examples:
	make all

To run examples:
	in seperate terminals run each of these commands
	java -jar Server.jar
	make hunter1go
	make hunter2go

To make your own implementation:
	create a source file
	include Agent.h
	implement init, fini, and respondToChange
	update Makefile by copying one of the examples and replace the example source/object files with your own.

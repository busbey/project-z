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
	* Agent.pm		- reference package that abstracts networking and provides trivial implementation
	* Random.pm		- an example Agent that moves randomly each turn.
	* Smart.pm		- an example Agent that moves probablisticly towards the nearest bug.  bugs are specified by a regular expression.
	* Server.jar	- a server implementation you can use to test.

To run examples:
	in seperate terminals run each of these commands
	java -jar Server.jar
	make hunter1go
	make hunter2go

To make your own implementation:
	create a source file
	extend Agent.pm
	implement init, fini, and respondToChange
	update Makefile

License:
#  Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
#  
#  This program is free software: you can redistribute it and/or modify it
#  under the terms of the GNU General Public License as published by the Free
#  Software Foundation, either version 3 of the License, or (at your option)
#  any later version.
#  
#  This program is distributed in the hope that it will be useful, but WITHOUT
#  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
#  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
#  more details.
#  
#  You should have received a copy of the GNU General Public License along with
#  this program.  If not, see <http://www.gnu.org/licenses/>.
#  

These agents are made to test hostile attacks on the server.


More Info:

	* Detailed Description: http://code.google.com/p/project-z/wiki/ProblemDescription
	* Client-to-Server network protocol: http://code.google.com/p/project-z/wiki/ActorProtocol
	* Server-to-Client network protocol: http://code.google.com/p/project-z/wiki/WorldStateProtocol

In this kit:
	* Readme.txt	- this guide
	* Makefile		- a gnu make file for building and running this sample and your implementation
	* Agent.h		- header with all constants and function definitions you'll need for dealing with the reference abstraction
	* Agent.c		- reference implementation to handle networking.
	* NoRead.c		- an Agent that doesn't move and stops reading from the 
					state socket in an effort to fill the write buffer of 
					the server.

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

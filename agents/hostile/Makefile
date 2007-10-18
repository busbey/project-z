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
CFLAGS = -Wall -pedantic -DDEBUG
DEFAULT_HOST = 'localhost'
DEFAULT_PORT = 1337
BUG_PORT = 7331
all: noread
noread: Agent.o NoRead.o
	gcc -Wall -pedantic -o noread Agent.o NoRead.o
clean:
	rm -rf *.o noread
hunter1go: noread 
	./noread -h $(DEFAULT_HOST) -p $(DEFAULT_PORT)
hunter2go: noread
	./noread -h $(DEFAULT_HOST) -p $(DEFAULT_PORT)
buggo: noread
	./noread -h $(DEFAULT_HOST) -p $(BUG_PORT)

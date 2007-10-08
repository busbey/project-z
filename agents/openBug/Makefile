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
CFLAGS = -Wall -pedantic -DDEBUG -O3
BUG_PATTERN = '[B-N]'
HUNTER_PATTERN = '^[1-9]'
PASSABLE_PATTERN = '^[ P]'
POWERUP_PATTERN = '^[P]'
DEFAULT_HOST = 'localhost'
DEFAULT_PORT = 1337
BUG_PORT = 7331
all: random smart open
random: Agent.o Random.o
	gcc -Wall -pedantic -o random Agent.o Random.o
smart: Agent.o Smart.o
	gcc -Wall -pedantic -o smart Agent.o Smart.o
open: Agent.o Open.o
	gcc -Wall -pedantic -o open Agent.o Open.o
clean:
	rm -rf *.o random smart open
hunter1go: smart
	./smart -h $(DEFAULT_HOST) -p $(DEFAULT_PORT) $(BUG_PATTERN)
hunter2go: random
	./random -h $(DEFAULT_HOST) -p $(DEFAULT_PORT)
buggo: open 
	./open -h $(DEFAULT_HOST) -p $(BUG_PORT) $(PASSABLE_PATTERN) $(POWERUP_PATTERN) $(HUNTER_PATTERN)

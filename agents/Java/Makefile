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
BUG_PATTERN = '[B-N]'
DEFAULT_HOST = 'localhost'
DEFAULT_PORT = 1337

RANDOMCLASSES = \
	RandomAgent.java \
	Agent.java 
SMARTCLASSES = \
	SmarterAgent.java \
	Agent.java

all: random smart
random: $(RANDOMCLASSES:.java=.class)
smart: $(SMARTCLASSES:.java=.class)
clean:
	rm -rf *.class
hunter1go: smart
	java Agent $(DEFAULT_HOST) $(DEFAULT_PORT) SmarterAgent $(BUG_PATTERN)
hunter2go: random
	java Agent $(DEFAULT_HOST) $(DEFAULT_PORT) RandomAgent
	
.SUFFIXES: .java .class
.java.class:
	javac $*.java


#  Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs
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
MAP = ../worlds/tiles
ROUNDS = 500
SERVERCLASSES = \
	ChatMessage.java \
	WorldFilter.java \
	World.java \
	RotatingWorld.java \
	Worker.java \
	StateWorker.java \
	Server.java

all: jar
server: $(SERVERCLASSES:.java=.class)
clean:
	rm -rf *.jar
	rm -rf *.class
jar: server ServerManifest
	jar cmf ServerManifest Server.jar *.class
1.4: jar
	java -cp ../dependencies/bsd/retroweaver-2.0/release/retroweaver-all-2.0.jar net.sourceforge.retroweaver.Weaver -jar Server.jar Server.1.4.jar
servergo: jar
	java -jar Server.jar -b $(ROUNDS) $(MAP)
	
.SUFFIXES: .java .class
.java.class:
	javac $*.java


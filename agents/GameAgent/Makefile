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
DEFAULT_HOST = 'localhost'
DEFAULT_PORT = 1337
BUG_PORT = 7331
PASS_HOST = 'localhost'
PASS_PORT = 10001
POWERUP_PATTERN = '[P]'

classpath=.:../../dependencies/bsd/jutils/jutils.jar:../../dependencies/bsd/jinput/jinput.jar

CLASSES = \
    Agent.java \
	JoystickAgent.java \
	PassthroughJoystickAgent.java \
	Game.java

all: joystick
smart: $(SMARTCLASSES:.java=.class)
joystick: $(CLASSES:.java=.class)
	@echo "Don't forget that you need to put the jni files from ../../dependencies/bsd/jinput into your java.library path.  on Linux/Unix do this by setting LD_LIBRARY_PATH.  On OS X copy the files into ~/Library/Java/Extensions.  On Windows, add to PATH.  make "joystick-test" to see if it works"
joystick-test: 
	java -classpath $(classpath):../../dependencies/bsd/jinput/jinput-test.jar net.java.games.input.test.ControllerReadTest 
clean:
	rm -rf *.class
hunter1go: joystick
	java -classpath $(classpath) Agent $(DEFAULT_HOST) $(DEFAULT_PORT) JoystickAgent
hunter2go: joystick
	#java -classpath $(classpath) -Djava.library.path=$(librarypath) Agent $(DEFAULT_HOST) $(DEFAULT_PORT) JoystickAgent
	java -classpath $(classpath) Agent $(DEFAULT_HOST) $(DEFAULT_PORT) JoystickAgent
joystickgo: joystick
	java -classpath $(classpath) Agent $(DEFAULT_HOST) $(BUG_PORT) JoystickAgent
passthroughgo: joystick
	java -classpath $(classpath) Agent $(DEFAULT_HOST) $(BUG_PORT) PassthroughJoystickAgent
game: all
	java -classpath $(classpath) Game "Manual Override" ../../dependencies/media/cute/Enemy\ Bug\ Blue\ Cropped.png ${DEFAULT_HOST} ${DEFAULT_PORT}

.SUFFIXES: .java .class
.java.class:
	javac -classpath $(classpath) $*.java


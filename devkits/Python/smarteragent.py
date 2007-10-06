#!/usr/bin/env python
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


import agent
import random
import sys
import getopt
import re
import math

def main (argv):
    host = ''
    port = 0
    regex = ''
    
    try:
        opts, args = getopt.getopt(argv, 'h:p:r:', ['host=', 'port=', 'regex='])
    except getopt.GetoptError:
        print 'smarteragent.py [-h|--host=] host [-p|--port=] port [-r|--regex=] search-pattern'
        sys.exit(1)
        
    for opt, arg in opts:
        if opt in ('-h', '--host'):
            host = arg
        elif opt in ('-p', '--port'):
            port = arg
        elif opt in ('-r', '--regex'):
            regex = arg

    agent = SmarterAgent(host, port, regex)

class SmarterAgent(agent.Agent):
    moves = ['u', 'd', 'l', 'r', 'n']

    def __init__ (self, host, port, regex):
        self.regex = regex
        super(SmarterAgent, self).__init__(host, port)
    
    def respondToChange (self):
        searcher = re.compile(self.regex)
        places = []
        position = ()
        for i in range(0, self.state.rows):
            for j in range(0, self.state.columns):
                if (searcher.search(self.state.readBoard(i, j))):
                    places.append( (i, j) )
                if (self.state.readBoard(i, j) == self.state.player):
                    position = (i, j)

        minDistance = self.state.rows + self.state.columns
        closest = ()
        if (len(places) == 0):
            self.writeMove('n')
            return

        for pair in places:
            distance = math.fabs(pair[0] - position[0]) + math.fabs(pair[1] - position[1])
            if (distance < minDistance):
                minDistance = distance
                closest = pair

        print "Goal '" + self.regex + "' is at (%d, %d); I am at (%d, %d)" % (closest[0], closest[1], position[0], position[1])

        if (random.random() < (math.fabs(closest[0] - position[0]) / minDistance)):
            if ((closest[0] - position[0]) < 0):
                self.writeMove('u')
            else:
                self.writeMove('d')
        else:
            if ((closest[1] - position[1]) < 0):
                self.writeMove('l')
            else:
                self.writeMove('r')

if __name__ == '__main__':
    main(sys.argv[1:])

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


import sys
import getopt
import socket
import struct

def main (argv):
    host = ""
    port = 0
    
    try:
        opts, args = getopt.getopt(argv, "h:p:", ["host=", "port="])
    except getopt.GetoptError:
        print "agent.py [-h|--host=] host [-p|--port=] port"
        sys.exit(1)
        
    for opt, arg in opts:
        if opt in ("-h", "--host"):
            host = arg
        elif opt in ("-p", "--port"):
            port = arg

    agent = Agent(host, port)

class Agent(object):
    def __init__ (self, host, port):
        self.host = host
        self.port = port
        self.state = None
        self.openSocket()
        self.runAgent()
        sys.exit(0)

    def openSocket (self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect( (self.host, int(self.port)) )

    def runAgent (self):
        while True:
            flag = struct.unpack("B", self.sock.recv(1))[0]
            if (flag == State.GAME_ENDED):
                self.socket.shutdown()
                self.socket.close()
                print 'Game has ended...'
                return 
            
            player = self.sock.recv(1)
            columns = self.readInteger()
            rows = self.readInteger()

            if (self.state == None):
                self.state = State(player, rows, columns)

            self.state.setFlag(flag)

            for i in range(0, rows):
                for j in range(0, columns):
                    self.state.changeBoard(i, j, self.sock.recv(1))
                        
            messages = self.readInteger()
            
            print 'flag:' + self.state.flagString()
            print "player: '" + player + "'"
            print 'rows: %d columns: %d' % (rows, columns)
            print 'messages: %d' % messages 

            self.state.emptyMessages()
            for i in range(0, messages):
                speaker = self.sock.recv(1)
                subject = self.sock.recv(1)
                action = self.sock.recv(1)
                print "message: '" + speaker + "' says '" + subject + "' should move " + self.moveString(action)
                self.state.addMessage(speaker, subject, action)

            self.respondToChange()
            print

    def readInteger (self):
        read = []
        while (len(read) < 4):
            read.append(self.sock.recv(1))
        return socket.ntohl(struct.unpack("L", ''.join(read))[0])

    def writeMove (self, move):
        print 'moving ' + self.moveString(move)
        self.sock.send(move)

    def sendMessage (self, speaker, subject, action):
        print "sending: '" + speaker + "' says '" + subject + "' should move " + self.moveString(action)
        self.sock.send(speaker)
        self.sock.send(subject)
        self.sock.send(action)
    
    def respondToChange (self):
        self.writeMove('n')

    def moveString (self, move):
        return {'u': 'UP',
                'd': 'DOWN',
                'l': 'LEFT',
                'r': 'RIGHT',
                'n': 'NONE'}[move]

class State:
    KILLER_BUG = 0x01
    WAS_KILLED = 0x02
    WAS_STUNNED = 0x04
    KILLED_SOMEONE = 0x08
    GAME_ENDED = 0xff

    def __init__ (self, player, rows, columns):
        self.killerBug = False
        self.wasStunned = False
        self.wasKilled = False
        self.killedSomeone = False
        self.player = player
        self.rows = rows
        self.columns = columns
        self.board = []
        for i in range(0, self.rows * self.columns):
            self.board.append('')
        self.emptyMessages()
    
    def changeBoard (self, row, column, type):
        self.board[row * self.columns + column] = type

    def readBoard (self, row, column):
        return self.board[row * self.columns + column]

    def emptyMessages (self):
        self.messages = []

    def addMessage (self, speaker, subject, action):
        self.messages.append( (speaker, subject, action) )

    def setFlag (self, flag):
        self.killerBug = ((flag & State.KILLER_BUG) == State.KILLER_BUG)
        self.wasKilled = ((flag & State.WAS_KILLED) == State.WAS_KILLED)
        self.wasStunned = ((flag & State.WAS_STUNNED) == State.WAS_STUNNED)
        self.killedSomeone = ((flag & State.KILLED_SOMEONE) == State.KILLED_SOMEONE)

    def flagString (self):
        if (not (self.killerBug or self.wasKilled or self.wasStunned or self.killedSomeone)):
            return ' None.'
        value = ''
        if (self.killerBug):
            value = value + ' [Bugs kill hunters]'
        if (self.wasKilled):
            value = value + ' [Player died last round]'
        if (self.wasStunned):
            value = value + ' [Player stunned last round]'
        if (self.killedSomeone):
            value = value + ' [Player killed someone last round]'
        return value

if __name__ == '__main__':
    main(sys.argv[1:])

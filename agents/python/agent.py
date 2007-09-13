#!/usr/bin/env python
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
            if (flag == 0xff):
                self.socket.shutdown()
                self.socket.close()
                return 
            print 'flag: %d' % flag
            
            player = self.sock.recv(1)
            print 'player: ' + player

            columns = self.readInteger()
            rows = self.readInteger()
            print 'rows: %d columns: %d' % (rows, columns)
            
            if (self.state == None):
                self.state = State(player, rows, columns)

            self.state.killerBug = ((flag & 0x01) == 0x01)
            self.state.wasKilled = ((flag & 0x02) == 0x02)
            self.state.wasStunned = ((flag & 0x04) == 0x04)

            for i in range(0, rows):
                for j in range(0, columns):
                    self.state.changeBoard(i, j, self.sock.recv(1))
            print 'board: \n' + self.state.boardString()
                        
            messages = self.readInteger()
            print 'messages: %d' % messages 
            
            self.state.emptyMessages()
            for i in range(0, messages):
                speaker = self.sock.recv(1)
                subject = self.sock.recv(1)
                action = self.sock.recv(1)
                print 'message: ' + speaker + ' says ' + subject + ' should move ' + action
                self.state.addMessage(speaker, subject, action)

            self.respondToChange()

    def readInteger (self):
        read = []
        while (len(read) < 4):
            read.append(self.sock.recv(1))
        return socket.ntohl(struct.unpack("L", ''.join(read))[0])

    def writeMove (self, move):
        print 'moving: ' + move
        self.sock.send(move)

    def sendMessage (self, speaker, subject, action):
        print 'sending: ' + speaker + ' says ' + subject + ' should move ' + action
        self.sock.send(speaker)
        self.sock.send(subject)
        self.sock.send(action)
    
    def respondToChange (self):
        self.writeMove('n')

class State:
    def __init__ (self, player, rows, columns):
        self.killerBug = False
        self.wasStunned = False
        self.wasKilled = False
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

    def boardString (self):
        result = ''
        for i in range(0, self.rows):
            result = result + ''.join(self.board[(i * self.columns):((i + 1) * self.columns)])
            if (i < self.rows - 1):
                result = result + '\n'

        return result

if __name__ == '__main__':
    main(sys.argv[1:])

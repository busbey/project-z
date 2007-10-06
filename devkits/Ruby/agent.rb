#!/usr/bin/env ruby
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


require 'socket'
require 'optparse'

class State
  attr_accessor :messages, :game_over, :killer_bug, :was_stunned, :was_killed, :killed_someone, :player, :rows, :columns, :board
  def initialize(player, rows, columns)
    @killer_bug = false
    @was_stunned = false
    @was_killed = false
	@killed_someone = false
    @player = player
    @rows = rows
    @columns = columns
    @board = Array.new(rows) { Array.new(columns) { ' ' } }
    @messages = []
  end

  def board_string
    result = ""
    @board.each { |row| result << row.join << "\n" }
    result
  end
end

class Agent
  attr_reader :socket
  @@moves = {:up => 'u', :down => 'd', :left => 'l', :right => 'r', :none => 'n' }
  @@reverse_moves = {}
  @@moves.each { |k,v| @@reverse_moves[v] = k } 
  
  def add_options(opts)
  end

  def verify
  end
  
  def write_move(move)
    puts "moving #{move.to_s.upcase}"
    move_str = @@moves[move]
    raise "Invalid move: #{move}" unless move
    @socket << move_str
  end
  
  def write_message(speaker, subject, action)
    puts "sending '#{speaker}' says '#{subject}' should move #{action.to_s.upcase}"
    [speaker, subject, action].each {|m| @socket << m}
  end

  def respond_to_change(state)
    #EXTEND ME
    write_move(:none)
  end

  def read_int
    @socket.read(4).unpack('N')[0]
  end

  def read_char
    @socket.read(1)
  end

  def run(hostname, port)
    @socket = TCPSocket::new(hostname, port)
    loop do
      puts 
      flag = read_char
      if !flag || flag[0] == 0xff
        @socket.close
        puts "Game has ended..."
        return
      end
      flag = flag[0]

      player = read_char

      columns = read_int
      rows = read_int
      @state ||= State.new(player, rows, columns)
      @state.killer_bug = ((flag & 0x01) == 0x01)
      @state.was_killed = ((flag & 0x02) == 0x02)
      @state.was_stunned = ((flag & 0x04) == 0x04)
	  @state.killed_someone = ((flag & 0x08) == 0x08)
      (0...rows).each { |r|
        (0...columns).each { |c|
          @state.board[r][c] = read_char
        }
      }
      
      @state.messages.clear
      message_count = read_int
      (0...message_count).each { |i|
        speaker = read_char
        subject = read_char
        action = read_char
        @state.messages << [speaker, subject, action]
      }
      
      #output
      flag_text = []
      flag_text << "[Bugs kill hunters]" if @state.killer_bug
      flag_text << "[Player died last round]" if @state.was_killed
      flag_text << "[Player stunned last round]" if @state.was_stunned
	  flag_text << "[Player killed someone last round]" if @state.killed_someone
      puts "flags: " + (flag_text.length > 0 ? flag_text.join(' ') : 'None')
      puts "player: '#{@state.player}'"
      puts "rows: #{@state.rows} columns: #{@state.columns}"
      puts "messages: #{@state.messages.length}"
      @state.messages.each { |msg| puts "message: '#{msg[0]}' says '#{msg[1]}' should move #{@@reverse_moves[msg[2]].to_s.upcase}" }
      respond_to_change @state
      @socket.flush  
    end
  end
end

def start_agent(agent, &block)
  hostname = "localhost"
  port = nil
  opts = OptionParser.new { |opts|
    opts.on("-n", "--hostname HOSTNAME") { |hn| hostname = hn }
    opts.on("-p", "--port PREFIX") { |p| port = p }
    opts.on_tail("-h", "--help", "Show this usage statement")
    agent.add_options(opts)
  }

  port_num = nil
  begin
    opts.parse!(ARGV)
    raise "Please specify port" unless port
    raise "Invalid port" unless port =~ /\d+/
    port_num = port.to_i
    agent.verify
  rescue Exception => e
    puts e, "", opts
    exit
  end
  
  agent.run(hostname, port_num)
end

if __FILE__ == $0
  start_agent(Agent.new)
end

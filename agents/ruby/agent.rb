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


require 'socket'
require 'optparse'

class State
	attr_accessor :messages, :killer_bug, :was_stunned, :was_killed, :player, :rows, :columns, :board
	def initialize(player, rows, columns)
		@killer_bug = false
		@was_stunned = false
		@was_killed = false
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
  
  def add_options(opts)
  end

  def verify
  end
  
	def debug msg
		puts msg if @debug_flag
	end

	def write_move(move)
		move_str = @@moves[move]
		raise "Invalid move: #{move}" unless move
		@socket << move_str
	end
	
	def write_message(speaker, subject, action)
		debug "sending: #{speaker} says #{subject} should move #{action}"
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

	def run(hostname, port, debug_flag)
    @debug_flag = debug_flag
		@socket = TCPSocket::new(hostname, port)
		loop do
			flag = read_char
			if !flag || flag[0] == 0xff
				@socket.close
				return
			end
			flag = flag[0]
			debug "flag: #{flag}"

			player = read_char
			debug "player: #{player}"

			columns = read_int
			rows = read_int
			debug "rows: #{rows} columns: #{columns}"
			@state ||= State.new(player, rows, columns)
			@state.killer_bug = ((flag & 0x01) == 0x01)
			@state.was_killed = ((flag & 0x02) == 0x02)
			@state.was_stunned = ((flag & 0x04) == 0x04)
			(0...rows).each { |r|
				(0...columns).each { |c|
					@state.board[r][c] = read_char
				}
			}
			debug "board:"
			debug @state.board_string
			
			@state.messages.clear
			message_count = read_int
			(0...message_count).each { |i|
				speaker = read_char
				subject = read_char
				action = read_char
				debug 'message: ' + speaker + ' says ' + subject + ' should move ' + action
				@state.messages << [speaker, subject, action]
			}
			respond_to_change @state
		end
	end
end

def start_agent(agent, &block)
  hostname = "localhost"
  port = nil
  debug_flag = false
  opts = OptionParser.new { |opts|
    opts.on("-n", "--hostname HOSTNAME") { |hn| hostname = hn }
    opts.on("-v", "--verbose") { debug_flag = true }
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
	
	agent.run(hostname, port_num, debug_flag)
end

if __FILE__ == $0
	start_agent(Agent.new)
end

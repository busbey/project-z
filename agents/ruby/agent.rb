
require 'socket'
require 'getoptlong'
require 'rdoc/ri/ri_paths'
require 'rdoc/usage'

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
	def initialize(hostname, port)
		@socket = TCPSocket::new(hostname, port)
	end

	def debug msg
		puts msg
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

	def run
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
			debug "rows: #{rows} colunns: #{columns}"
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
			respond_to_change
		end
	end
end

def start_agent(agent_name)
	opts = GetoptLong.new(
		[ '--help', GetoptLong::NO_ARGUMENT],
      		[ '--host', '-h', GetoptLong::REQUIRED_ARGUMENT ],
      		[ '--port', '-p', GetoptLong::REQUIRED_ARGUMENT ]
	)

    	hostname = "localhost"
	port = nil
	opts.each do |opt, arg|
		case opt
        	when '--help'
			usage
        	when '--host'
          		hostname = arg
        	when '--port'
          		port = arg.to_i
      		end
    	end
	if port
		Kernel.const_get(agent_name).new(hostname, port).run
	else
		usage
	end
end

def usage
puts <<EOF

	Usage: #{$0} --port PORT [--hostname HOSTNAME]
		By default, localhost is used.

EOF
end
if __FILE__ == $0
	start_agent("Agent")
end

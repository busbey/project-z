require 'agent'

class SmarterAgent < Agent
	
	def initialize (hostname, port, params)
		super(hostname, port, params)
		@regex = Regexp.new(params['--regex'])
	end

	def respond_to_change state
		places = []
		position = nil
		(0...state.rows).each { |i|
			(0...state.columns).each { |j|
				places << [i, j] if @regex =~ state.board[i][j]
				position = [i, j] if state.player == state.board[i][j]
			}
		}
		min_distance = state.rows + state.columns
		closest = nil
		if (places == [])
			write_move(:none)
			return
		end

		places.each { |p| 
			distance = (p[0] - position[0]).abs + (p[1] - position[1]).abs
			if distance < min_distance
				min_distance = distance
				closest = p
			end
		}
		
		move = if rand(min_distance) < (closest[0] - position[0]).abs
			closest[0] - position[0] < 0 ? :up : :down
		else
			closest[1] - position[1] < 0 ? :left : :right
		end
		write_move move
	end
end

if __FILE__ == $0
	start_agent "SmarterAgent", ["--regex", GetoptLong::REQUIRED_ARGUMENT]
	
end

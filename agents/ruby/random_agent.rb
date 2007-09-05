require 'agent'

class RandomAgent < Agent
	def respond_to_change state
		moves = [:up, :down, :left, :right, :none]
		write_move moves[rand(moves.length)]
		write_message state.player, state.player, moves[rand(moves.length)]
	end
end

if __FILE__ == $0
	start_agent "RandomAgent"
end

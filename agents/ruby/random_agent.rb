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

require 'agent'

class RandomAgent < Agent
	def initialize
    @moves = [:up, :down, :left, :right, :none]
  end
  def respond_to_change state
		write_move random_move()
		write_message state.player, state.player, random_move()
	end

  def random_move
    @moves[rand(@moves.length)]
  end
end

if __FILE__ == $0
	start_agent(RandomAgent.new)
end

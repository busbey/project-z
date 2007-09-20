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
require 'agent'

class SmarterAgent < Agent
  #example of adding an option. see optparse documentation for more information
  def add_options(opts)
    opts.on("-r", "--regex REGEX", "Regular expression representing target") { |r| @regex_string = r }
  end

  #example of verifying an option's value.  any exception thrown in this method will trigger the usage statement being thrown
  def verify
    raise "Regular expression not specified" unless @regex_string
    @regex = Regexp.new(@regex_string)
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
		puts "Goal '#{@regex_string}' is at (#{closest.join(', ')}); I am at (#{position.join(', ')})"

    write_move move
	end
end

if __FILE__ == $0
	start_agent SmarterAgent.new
end

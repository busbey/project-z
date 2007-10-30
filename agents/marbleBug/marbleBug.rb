#!/usr/bin/env ruby
#
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

def distance(q,p,rows,cols)
   dist = []
   dist << ( p[0] - q[0]).modulo(rows) + ( p[1] - q[1]).modulo(cols)
   dist << ( p[0] - q[0]).modulo(rows) + ( p[1] + q[1]).modulo(cols)
   dist << ( p[0] + q[0]).modulo(rows) + ( p[1] - q[1]).modulo(cols)
   dist << ( p[0] + q[0]).modulo(rows) + ( p[1] + q[1]).modulo(cols)
   dist << ( p[0] - q[0]).modulo(rows) + (-p[1] - q[1]).modulo(cols)
   dist << ( p[0] - q[0]).modulo(rows) + (-p[1] + q[1]).modulo(cols)
   dist << ( p[0] + q[0]).modulo(rows) + (-p[1] - q[1]).modulo(cols)
   dist << ( p[0] + q[0]).modulo(rows) + (-p[1] + q[1]).modulo(cols)
   dist << (-p[0] - q[0]).modulo(rows) + ( p[1] - q[1]).modulo(cols)
   dist << (-p[0] - q[0]).modulo(rows) + ( p[1] + q[1]).modulo(cols)
   dist << (-p[0] + q[0]).modulo(rows) + ( p[1] - q[1]).modulo(cols)
   dist << (-p[0] + q[0]).modulo(rows) + ( p[1] + q[1]).modulo(cols)
   dist << (-p[0] - q[0]).modulo(rows) + (-p[1] - q[1]).modulo(cols)
   dist << (-p[0] - q[0]).modulo(rows) + (-p[1] + q[1]).modulo(cols)
   dist << (-p[0] + q[0]).modulo(rows) + (-p[1] - q[1]).modulo(cols)
   dist << (-p[0] + q[0]).modulo(rows) + (-p[1] + q[1]).modulo(cols)
   return dist.min
end

class MarbleBug < Agent
   attr_accessor :current, :previous
   @current  = [0,0]
   @previous = [0,0]
   @costs    = {
      :self    => 0.0,
      :bug     => 4.2,
      :hunter  => 10.7,
      :powerup => -100.1,
      :fog     => 1.2,
      :wall    => 1.0/0, # Infinity
   }

   def respond_to_change state
      tracked_items = []
      if state.killer_bug
         @costs[:hunter] = 0.0 - @costs[:hunter].abs
      else
         @costs[:hunter] = 0.0 + @costs[:hunter].abs
      end
      price_board = Array.new(3) { Array.new(3) { 0.0 } }

      (0...state.rows).each do |xx|
         (0...state.columns).each do |yy|
            case state.board[xx][yy]
            when state.player
               @current = [xx,yy]
               tracked_items << [xx,yy,:self]
               #if @current.eql?(@previous)
               #   @costs[:self] += rand
               #else
               #   @costs[:self] -= rand
               #   @costs[:self]  = @costs[:self].abs
               #end
               @previous = @current
            when "B".."N"
               tracked_items << [xx,yy,:bug]
            when "X"
               tracked_items << [xx,yy,:fog]
            when "1".."9"
               tracked_items << [xx,yy,:hunter]
            when "P"
               tracked_items << [xx,yy,:powerup]
            end
         end
      end

      ((@current[0]-1).modulo(state.rows)..(@current[0]+1).modulo(state.rows)).each do |xx|
         ((@current[1]-1).modulo(state.columns)..(@current[1]+1).modulo(state.columns)).each do |yy|
            case state.board[xx][yy]
            when state.player
               price_board[xx][yy] = @costs[:self]
            when "O"
               price_board[xx][yy] = @costs[:wall]
            when "X"
               price_board[xx][yy] = @costs[:fog]
            when "P"
               price_board[xx][yy] = @costs[:powerup]
            when "1".."9"
               price_board[xx][yy] = @costs[:hunter]
            when "B".."N"
               price_board[xx][yy] = @costs[:bug]
            else
               tracked_items.each do |tx,ty,type|
                  price_board[xx][yy] += @costs[type]/(3**(distance([xx,yy],[tx,ty],state.rows,state.columns)))
               end
            end
         end
      end

      # XXX Need to double-check orientation of the board
      moves = {}
      moves[:none]  = price_board[ @current[0],                          @current[1]]
      moves[:left]  = price_board[(@current[0] - 1).modulo(state.rows),  @current[1]]
      moves[:right] = price_board[(@current[0] + 1).modulo(state.rows),  @current[1]]
      moves[:up]    = price_board[ @current[0],                         (@current[1] - 1).modulo(state.columns)] 
      moves[:down]  = price_board[ @current[0],                         (@current[1] + 1).modulo(state.columns)] 

      # I feel like a dumbass, but isn't there a way to do this with inject ?
      min = :none
      moves.each do |mv,cost|
         min = mv if cost < moves[min]
      end

      write_move min
   end
end

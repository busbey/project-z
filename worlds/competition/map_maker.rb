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

class Map
   attr_accessor :world
   attr_reader :rows, :columns

   def initialize(rows,columns)
      @world   = Array.new(rows) { Array.new(columns) { ' ' } }
      @rows    = rows
      @columns = columns
   end

   def circle(center, radius)
      points = []

      ((center[0] - radius)..(center[0] + radius)).each do |x|
         ((center[1] - radius)..(center[1] + radius)).each do |y|
            points << [x,y] if (radius == distance([x,y],center,@rows,@columns))
         end
      end

      # XXX Hack hack hack
      (points.select   { |p| p[0]>=center[0] and p[1]>=center[1] } + 
       points.select { |p| p[0]>=center[0] and p[1]<=center[1] }.reverse +
       points.select { |p| p[0]<=center[0] and p[1]<=center[1] }.reverse +
       points.select { |p| p[0]<=center[0] and p[1]>=center[1] }).uniq
   end

   def punch_hole!(circle, width, where)
      where %= circle.size
      circle.slice!(where,width)
   end

   def output
      (0...@rows).each do |xx|
         (0...@columns).each do |yy|
            print @world[xx][yy]
         end
         puts
      end
   end
end

spinners = Map.new(84,104)
holes    = []
holes << spinners.circle([20,20], 5)
holes << spinners.circle([20,84], 5)
holes << spinners.circle([64,84], 5)
holes << spinners.circle([64,20], 5)
holes << spinners.circle([20,20],10)
holes << spinners.circle([20,84],10)
holes << spinners.circle([64,84],10)
holes << spinners.circle([64,20],10)

#holes << spinners.circle([42,52], 5)
holes << spinners.circle([42,52],15)
#holes << spinners.circle([42,52],25)
holes << spinners.circle([42,52],35)
rounds = holes.inject(1) { |multiple,obj| multiple.lcm(obj.size) }

(0..rounds).each do |round|
   # Clear map
   (1...spinners.rows).each { |x| (1...spinners.columns).each { |y| spinners.world[x][y] = " " } }
   holes.each do |hole|
      this_hole = hole.clone
      spinners.punch_hole!(this_hole,2,round)
      this_hole.each { |x,y| spinners.world[x][y] = "O" }
   end
   spinners.world[20][20] = "P"
   spinners.world[20][84] = "P"
   spinners.world[64][84] = "P"
   spinners.world[64][20] = "P"
   spinners.output
   puts "#{"="*104}"
end
puts ";rounds per frame:1"

# a.inject { |multiple,obj| multiple.lcm(obj) }
#mm = Map.new(30,30) 
#(0..45).each do |offset|
#
#   (1...mm.rows).each do |x|
#      (1...mm.columns).each do |y|
#         mm.world[x][y] = " "
#      end
#   end
#
#   cc = mm.circle([10,10],5)
#   mm.punch_hole!(cc,rand(3)+1,offset)
#   n=0
#   cc.each do |x,y|
#      mm.world[x][y] = n+=1
#   end
#   mm.output
#   gets
#end

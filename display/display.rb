#!/usr/bin/env jruby
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

require 'java'
require 'yaml'
require 'enumerator'
require 'socket'

$CLASSPATH << "/Applications/Processing 0125/lib/core.jar"
include_class "processing.core.PApplet"
include_class "processing.core.PConstants"
class ZViewer < PApplet
	def initialize(rows, columns, max_width = 1200, max_height = 750)
		super()
		@rows = rows
		@columns = columns
		@images = Hash.new
		@tile_width = 0
		@tile_height = 0
		@text = " " * (rows * columns) 
		File.open('images.yaml') { |f|
			YAML::load(f).each { |k,v|
				i = loadImage(v)
				@images[k.to_s] = i
				@tile_width = i.width
				@tile_height = i.height
			}
		}
		potential_width, potential_height = dimensions(@columns, @rows, @tile_width, @tile_height)
		if potential_height > max_height || potential_width > max_width
			shrinkage = [max_height * 1.0 / potential_height, max_width * 1.0 / potential_width].min
			puts "Shrinking #{shrinkage}"
			@small_tile_height = (@tile_height * shrinkage).floor
			@small_tile_width = (@tile_width * shrinkage).floor
			
			@display_width = @small_tile_width * columns
			@display_height = @small_tile_height * rows
		else
			@display_width = potential_width
			@display_height = potential_height
		end
	end

	def setup
		size @display_width, @display_height	
		noLoop()
	end

	def text=(text)
		@text = text
		redraw()
	end

	def dimensions(columns, rows, tile_width, tile_height) 
		vertical_offset = tile_height / 2
		[columns * tile_width, tile_height + (rows - 1) * vertical_offset]
	end

	def draw
		background 0
		vertical_offset = @small_tile_height / 2
	
		(0..@rows - 1).each { |r|
			(0..@columns - 1).each { |c|
				vertical_shift = r * vertical_offset
				image(@images[" "], c * @small_tile_width, vertical_shift, @small_tile_width, @small_tile_height) 
				pos =  r * @columns + c
				tile_type = @text[pos..pos]
				if tile_type != " "
					image(@images[tile_type], c * @small_tile_width, vertical_shift - vertical_offset/2, @small_tile_width, @small_tile_height)
				end
			}
		}
	end
end

def random_string(rows, columns) 
	possible = " OB1234"
	text = ""
	(rows * columns).times { 
		text << possible[rand(possible.length)]
	}
	text
end

def read_state(f)
	def pc(x) 
		puts sprintf("%x", x)
	end
	flag = f.getc
	puts "flags: " + flag.to_s(16)
	representation = f.getc
	puts "I'm display '" + representation.chr + "'"
	columns = f.read(4).unpack("N*")[0]
	rows = f.read(4).unpack("N*")[0]
	puts "board is col x row: " + columns.to_s + " x " + rows.to_s
	text = ""
	(rows * columns).times { text << f.getc }
	text.gsub!(/[B-N]/) {|agent| agent.downcase } if (0x01 == flag & 0x01)
	puts "board : " + text
	numChats = f.read(4).unpack("N*")[0]
	puts "chat messages this round: " + numChats.to_s
	chats = Array.new
	numChats.times { chats << [f.getc, f.getc, f.getc] }
	return rows, columns, text, chats 
end

class ZDisplayClient
	def connect(hostname, port=8668)
		begin
			t = TCPSocket.new(hostname, port)
		rescue
			puts "error: #{$!}"
		else
			while true
				rows, columns, text, chats = read_state(t)
				viewer ||= create_window(rows, columns)
				viewer.text = text
				unless chats.empty?
					chats.each do |speaker, subject, action| 
						puts "'" + speaker + "' says " + subject + " should move " + action + "\n"
					end
				end
			end
		end
	end
end

#rows, columns, text = read_state(File.open("test.dat", "rb"))

def create_window(rows, columns)
	frame = javax.swing.JFrame.new "Bug Hunter"
	applet = ZViewer.new(rows, columns)
	applet.text = " " * rows * columns
	frame.content_pane.add applet
	frame.default_close_operation = javax.swing.JFrame::EXIT_ON_CLOSE
	applet.init
	sleep 0.1
	frame.pack
	frame.visible = true
	applet
end

host = ARGV[0]
host ||= "localhost"
ZDisplayClient.new.connect(host)

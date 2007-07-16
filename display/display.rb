#!/usr/bin/env jruby
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
	pc flag
	representation = f.getc
	pc representation
	columns = f.read(4).unpack("N*")[0]
	rows = f.read(4).unpack("N*")[0]
	pc columns
	pc rows
	text = ""
	(rows * columns).times { text << f.getc }
	puts text
	text.gsub!(/[B-N]/) {|agent| agent.downcase } if (0x01 == flag & 0x01)
	return rows, columns, text 
end

class ZDisplayClient
	def connect(hostname, port=8668)
		begin
			t = TCPSocket.new(hostname, port)
		rescue
			puts "error: #{$!}"
		else
			while true
				rows, columns, text = read_state(t)
				viewer ||= create_window(rows, columns)
				viewer.text = text
			end
		end
	end
end

#rows, columns, text = read_state(File.open("test.dat", "rb"))

def create_window(rows, columns)
	frame = javax.swing.JFrame.new "RGB Cube"
	applet = ZViewer.new(rows, columns)
	applet.text = " " * rows * columns
	frame.content_pane.add applet
	frame.default_close_operation = javax.swing.JFrame::EXIT_ON_CLOSE
	applet.init
	frame.pack
	frame.visible = true
	applet
end

ZDisplayClient.new.connect("localhost")

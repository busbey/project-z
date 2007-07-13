#!/usr/bin/env ruby
require 'rubygems'
require 'yaml'
require 'RMagick'
include Magick
require 'enumerator'

class ZViewer
	def initialize(rows, columns, max_width = 1200, max_height = 750)
		@rows = rows
		@columns = columns
		@images = Hash.new
		@tile_width = 0
		@tile_height = 0
		File.open('images.yaml') { |f|
			YAML::load(f).each { |k,v|
				i = ImageList.new(v)
				@images[k.to_s] = i
				@tile_width = i.columns
				@tile_height = i.rows
			}
		}
		potential_width, potential_height = dimensions(@columns, @rows, @tile_width, @tile_height)
		if potential_height > max_height || potential_width > max_width
			shrinkage = [max_height * 1.0 / potential_height, max_width * 1.0 / potential_width].min
			puts "Shrinking #{shrinkage}"
			@images.each { |k, v| 
				v.resize!(shrinkage)
				@tile_width = v.columns
				@tile_height = v.rows
			}
		end
	end

	def dimensions(columns, rows, tile_width, tile_height) 
		vertical_offset = tile_height / 2
		[columns * tile_width, tile_height + (rows - 1) * vertical_offset]
	end

	def render(text)
		vertical_offset = @tile_height / 2
		image = Image.new(@columns * @tile_width, @tile_height + (@rows - 1) * vertical_offset) {self.background_color = "light blue" }
	
		(0..@rows - 1).each { |r|
			(0..@columns - 1).each { |c|
				vertical_shift = r * vertical_offset
				image.composite!(@images[" "], c * @tile_width, vertical_shift, OverCompositeOp) 
				pos =  r * @columns + c
				tile_type = text[pos..pos]
				if tile_type != " "
					image.composite!(@images[tile_type], c * @tile_width, vertical_shift - vertical_offset/2, OverCompositeOp)
				end
			}
		}
		image
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

def read_state(filename)
	File.open(filename, "rb") { |f|
		flag = f.getc
		representation = f.getc
		columns = f.read(4).unpack("N*")[0]
		rows = f.read(4).unpack("N*")[0]
		p columns, rows
		text = ""
		(rows * columns).times { text << f.getc }
		return rows, columns, text 
	}
end

rows, columns, text = read_state("test.dat")

#rows = 20
#columns = 20
#text = random_string(rows, columns)

viewer = ZViewer.new(rows, columns)
image = viewer.render text
image.display

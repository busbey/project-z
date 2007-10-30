#!/usr/bin/env jruby
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

require 'java'
require 'yaml'
require 'enumerator'
require 'socket'
require 'find'
require 'optparse'
def to_signed (n)
  length = 32
  mid = 2**(length-1)
  max_unsigned = 2**length
  (n>=mid) ? n - max_unsigned : n
end
$CLASSPATH << File.expand_path("../dependencies/lgpl/processing-0125/core.jar")
Find.find('../dependencies/lgpl/processing-0125/libraries/minim/library') { |path|
  if path =~ /\.jar$/
    $CLASSPATH << File.expand_path(path)
  end
}

include_class "java.util.concurrent.atomic.AtomicBoolean"
include_class "processing.core.PApplet"
include_class "processing.core.PImage"
include_class "processing.core.PConstants"
include_class "ddf.minim.Minim"
include_class "java.awt.GridLayout"
include_class "java.awt.BorderLayout"
include_class "javax.swing.ImageIcon"
include_class "java.awt.image.BufferedImage"

class ZViewer < PApplet
  def initialize()
    super()
    @draw_delta = AtomicBoolean.new();
    @images = Hash.new
    @real_images = Hash.new
    @tile_width = 0
    @tile_height = 0
    @text = " " 
    File.open('images.yaml') { |f|
      YAML::load(f).each { |k,v|
        i = loadImage(v)
        $score_labels.each { |s, l|
          if k.to_s =~ s  
            icon = ImageIcon.new(v)
            w = (icon.icon_width / 2).to_i
            h = (icon.icon_height / 2).to_i
            bi = BufferedImage.new(w,h,BufferedImage::TYPE_INT_ARGB)
            bi.graphics.drawImage(icon.image, 0, 0, w, h, nil)
            l.icon = ImageIcon.new(bi)
          end
        }
        @real_images[k.to_s] = i
        @tile_width = i.width
        @tile_height = i.height
      }
    }
    Minim.start(self)
    @sounds = Hash.new
    File.open('sounds.yaml') { |f|
      YAML::load(f).each { |k,v|
        @sounds[k.to_s] = Minim.loadFile(v)
      }
    }
    @rows = 0
    @columns = 0
    @loaded = false
    @refresh_thread = Thread.new do
      while true do 
        @draw_delta.set(false)
        sleep 5
      end
    end
  end
  
  def board_change(rows, columns, max_width = 1200, max_height = 680)
    return if rows == @rows and columns == @columns
    
    @rows = rows
    @columns = columns
    potential_width, potential_height = dimensions(@columns, @rows, @tile_width, @tile_height)
    if potential_height > max_height || potential_width > max_width
      shrinkage = [max_height * 1.0 / potential_height, max_width * 1.0 / potential_width].min
      puts "Shrinking #{shrinkage}"
      @small_tile_height = (@tile_height * shrinkage).floor
      @small_tile_width = (@tile_width * shrinkage).floor
     
      #resize images
      @real_images.each { |k,v| 
        img = PImage.new(@small_tile_width, @small_tile_height)
        img.copy(v, 0, 0, @tile_width, @tile_height, 0, 0, @small_tile_width, @small_tile_height) 
        @images[k] = img
      }

      @display_width = @small_tile_width * columns
      @display_height = @small_tile_height * rows
    else
      @real_images.each { |k,v|
        @images[k] = v
      }

      @small_tile_height = @tile_height
      @small_tile_width = @tile_width
      @display_width = potential_width
      @display_height = potential_height
    end
    @black_top_block = PImage.new(@small_tile_width, @small_tile_height)
    black = color(0, 0, 0)
    (0...@small_tile_width).each {|x| (0...@small_tile_height).each {|y| @black_top_block.set(x, y, black) } }
    
    @dirty = Hash.new { h[k] = true} #Array.new(@rows * @columns,true) #initially all tiles are dirty
    @draw_delta.set(false)
    @loaded = true
  end
  
  def setup
    size @display_width, @display_height  
    background 0
    noLoop()
  end

  def text=(text)
    @old_text = @text
    @text = text
    redraw()
  end

  def play_sound(key)
    s = @sounds[key]
    if s
      Thread.new { s.play; s.rewind }
    else
      STDERR.puts "Error playing '#{key}'"
    end
  end
  
  def dimensions(columns, rows, tile_width, tile_height) 
    vertical_offset = tile_height / 2
    [columns * tile_width, tile_height + (rows - 1) * vertical_offset]
  end

  def draw
    return unless @loaded
    vertical_offset = @small_tile_height / 2
    unless @draw_delta.getAndSet(true)
      #mark all tiles for redrawing
      (0...@text.length).each { |i| @dirty[i] = true }
      background 0
    else
      (0...@text.length).each { |i| @dirty[i] = false }
      
      #find tiles that need to be redrawn
      (0...@text.length).each { |i| 
        if @text[i] != @old_text[i]
          k = i
          #mark cell and cell above as dirty
          final = [0, i - @columns].max
          while k >= final
            @dirty[k] = true
            final = [0, k - 2 * @columns].max if @text[k..k] =~ /[a-zP]/ || @old_text[k..k] =~ /[a-zP]/   #powerups need to have the upper two rows cleared as well
            k -= @columns
          end
        
          #mark all cells below as dirty
          (i + @columns...@text.length).step(@columns) { |k| @dirty[k] = true }
        end
      }
    end
    #redraw necessary tiles
    (0...@text.length).each { |i|
      if @dirty[i]
        r = i / @columns
        c = i % @columns
        
        vertical_shift = r * vertical_offset
        image(@black_top_block, c * @small_tile_width, vertical_shift) if r == 0 #redraw black background on top row
        image(@images[" "], c * @small_tile_width, vertical_shift)  #redraw base tile 
        tile_type = @text[i..i]
        if tile_type != " "
          image(@images[tile_type], c * @small_tile_width, vertical_shift - vertical_offset/2) if @images[tile_type]
        end
      end
    }
  end
end

def read_state(f)
  flag, numStuns = f.read(5).unpack('CN')
  STDERR.puts "flags: #{flag.to_s(16)}"
  puts "there were '#{numStuns}' stuns"
  stuns = []
  f.read(numStuns * 2).unpack('a*')[0].split(//).each_cons(2) { |stun| stuns << stun }
  numKills = f.read(4).unpack('N')[0]
  STDERR.puts "there were '#{numKills}' kills"
  puts "there were '#{numKills}' kills"
  kills = []
  f.read(numKills * 2).unpack('a*')[0].split(//).each_cons(2) { |kill| kills << kill }
  columns, rows = f.read(8).unpack('NN')
  puts "board is col x row: #{columns} x #{rows}"
  
  text = f.read(rows * columns)
  new_board = ( 0x10 == flag &  0x10)
  if (0x01 == flag & 0x01)
  	text.gsub!(/[B-N]/) {|agent| agent.downcase }
	# there must be a simpler way to do this substitution...
    text.gsub!(/1/, '!')
	  text.gsub!(/2/, '@')
	  text.gsub!(/3/, '#')
	  text.gsub!(/4/, '$')
	  text.gsub!(/5/, '%')
	  text.gsub!(/6/, '^')
	  text.gsub!(/7/, '&')
	  text.gsub!(/8/, '*')
	  text.gsub!(/9/, '(')
	  text.gsub!(/0/, ')')
  end
  puts "canonical board : #{text}"
  numViews = f.read(4).unpack('N')[0]
  puts "there are #{numViews} agent-specific boards"
  # We're just discarding this for now.
  f.read(numViews * (1 + (rows * columns)))
  
  numChats = f.read(4).unpack('N')[0]
  chats = []
  puts "chat messages this round: " + numChats.to_s
  f.read(numChats * 4).unpack('a*')[0].split(//).each_cons(4) { |c| chats << c }
  numScores = f.read(4).unpack('N')[0]
  puts "there were '#{numScores}' scores"
  scores = []
  numScores.times do 
    agent,score = f.read(5).unpack('CN')
    scores << [agent.chr,to_signed(score)]
  end
  
  return rows, columns, text, stuns, kills, chats, scores, new_board 
end

class ZDisplayClient
  def connect(hostname, port=8668)
    viewer = create_window()
    t = TCPSocket.new(hostname, port)
    loop do
      rows, columns, text, stuns, kills, chats,scores,new_board = read_state(t)
      if new_board
        viewer.board_change(rows, columns)
      end
      viewer.text = text
      puts kills.inspect
    
      unless kills.empty?
        viewer.play_sound(kills[0][1] =~ /[0-9]/ ? 'player_death' : 'bug_death') 
        kills.each do |killer, killed|
          puts "'#{killer}' killed '#{killed}"
        end 
      end
      unless stuns.empty?
        viewer.play_sound('player_stun')
        stuns.each do |stunner, stunned|
          puts "'#{stunner}' stunned '#{stunned}'"
        end
      end
      chats.each do |sender, speaker, subject, action| 
        puts "'#{speaker}' says '#{subject}' should move '#{action}' #{sender.eql?(speaker) ? '' : ' [lie]'}"
      end
      unless scores.empty?
        grouped_scores = Hash.new { |h,k| h[k] = 0 }
        scores.each { |pair|
          subject, score = pair
          $groups.each { |g|  grouped_scores[g] += score if g =~ subject }
        }
        update_scores(grouped_scores)
        puts "Scores -"
        grouped_scores.each do |subject, score|
          puts "\t#{subject}: #{score} "
        end
      end
      if new_board
        viewer.play_sound('board_change')
      end
    end
  end
end

def add_score_list(panel)
  score_panel = javax.swing.JPanel.new
  score_panel.layout = GridLayout.new(1, $groups.length)
  
  $score_labels.each { |g, l|
    score_panel.add(l)
  }
  panel.add(score_panel, BorderLayout::SOUTH) 
end

def update_scores(scores)
  $score_labels.each { |g, l|
    l.text = scores[g].to_s
  }
end

def create_window()
  frame = javax.swing.JFrame.new "Bug Hunter"
  frame.content_pane.layout = BorderLayout.new
  applet = ZViewer.new()
  applet.board_change(1,1)
  applet.text = " "
   
  frame.content_pane.add applet
  add_score_list(frame.content_pane)
  frame.default_close_operation = javax.swing.JFrame::EXIT_ON_CLOSE
  
  applet.init
  frame.extended_state = (frame.extended_state | javax.swing.JFrame::MAXIMIZED_BOTH);
  
  frame.visible = true
  applet
end

host = "localhost"
$groups = []
$score_labels = {}
opts = OptionParser.new do |opts|
  opts.on("-h", "--host HOSTNAME") do |h|
    host = h
  end
  opts.on("--groups GROUPFILE") do |g|
    File.open(g) { |f|
      YAML::load(f).each { |k|
        g = Regexp.new(k.to_s)
        $groups << g
        l = javax.swing.JLabel.new('0')
        $score_labels[g] = l  
        l.font = l.font.deriveFont(36.0)
      }
    }
  end
end
opts.parse!(ARGV)
ZDisplayClient.new.connect(host)

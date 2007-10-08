#!/usr/bin/env ruby
require 'set'
require '../../devkits/Ruby/agent'
# @killer_bug = false
# @was_stunned = false
# @was_killed = false
# @killed_someone = false
# @player = player
# @rows = rows
# @columns = columns
# @board = Array.new(rows) { Array.new(columns) { ' ' } }
# @messages = []
#
class EulerAgent < Agent
  Infinity = 1.0/0
  def initialize
    @moves = [:up, :down, :left, :right, :none]
  end
  
  def distance i, j
    @dist ||= Array.new(@rows * @columns) { nil }
    unless @dist[i]
      d = Array.new(@rows * @columns) { Infinity }
      
      queue = [[i,0]]
      queued = Set.new([i])
      while !queue.empty?
        top, dd = queue.shift
        d[top] = dd
        @neighbors[top].each { |n| 
          unless queued.include? n
            queue << [n, dd + 1]
            queued << n
          end
        }
      end
      @dist[i] = d
    end
    return @dist[i][j]
  end

  def make_empty_board_and_neighbors state
    @rows = state.rows
    @columns = state.columns
    @empty_board = Array.new(@rows) { Array.new(@columns) { 0 } }
    (0...@rows).each { |r|
      (0...@columns).each { |c|
        char = state.board[r][c]
        if char == 'O' || char == /[B-N]/ || (!state.killer_bug && state.board[r][c] =~ /[1-4]/) #players are boulders if we can't kill em
          @empty_board[r][c] = 1 
        end
        
      }
    }
    @neighbors = Hash.new { |h, k| h[k] = [] }
    (0...@rows).each { |r|
      (0...@columns).each { |c|
        if @empty_board[r][c] == 0
          pos = r * @columns + c
          [[r, (c - 1) % @columns], [r, (c + 1) % @columns], [(r - 1) % @rows, c], [(r + 1) % @rows, c]].each { |p|
            rr, cc = p
            if @empty_board[rr][cc] == 0
              @neighbors[pos] << (rr * @columns + cc) 
            end
          }
        end
      }
    }
  end


  def move_position(s, mv)
    r, c = s / @columns, s % @columns
    case mv
      when :up
        r -= 1
      when :down
        r += 1
      when :left
        c -= 1
      when :right
        c += 1
    end
    (r % @rows) * @columns + (c % @columns) 
  end

  def closest_to pos, targets
    move_to_make = nil
    targets.sort_by { |s| distance(pos, s) }.each { |s|
      possible = [:up, :down, :left, :right].collect { |m|
        new_pos = move_position(pos, m)
        [m, distance(new_pos, s)]
      }.sort_by{ |sd| sd[1] }.first
      if possible[1] != Infinity
        move_to_make = possible[0]
        break
      end
      break if move_to_make
    }
    move_to_make
  end

  def respond_to_change state
    make_empty_board_and_neighbors state #unless @empty_board && @neighbors
    
    @other_bugs = []
    @players = []
    @stars = []
    #find interesting parts of the board
    (0...@rows*@columns).each { |pos| 
      y = state.board[pos / @columns][pos % @columns]
      case y
        when state.player
          @bug_pos = pos
        when /[1-4]/
          @players << pos
        when /[B-N]/
          @other_bugs << pos
        when /P/
          @stars << pos
      end
    }
    puts state.board.collect { |row| row.join }.join("\n")
    puts "I, #{state.player}, am at #{@bug_pos}"
    puts "Players: #{@players.join(', ')}"
    puts "Other bugs: #{@other_bugs.join(', ')}"
    puts "Stars: #{@stars.join(', ')}"

    #clear distances
    @dist = nil
    targets = state.killer_bug ? @players : @stars
    move_to_make = closest_to(@bug_pos, targets) || :none 
    
    write_move move_to_make
    write_message state.player, state.player, random_move()
  end

  def random_move
    @moves[rand(@moves.length)]
  end
end

if __FILE__ == $0
  start_agent(EulerAgent.new)
end


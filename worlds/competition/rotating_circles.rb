# automatically produce a rotating circle map using
# rotating_circles.template

big = ( (?a .. ?z).to_a + (?A .. ?R).to_a ).map { |i| i.chr }
small = ( (?S .. ?Z).to_a + (?0 .. ?7).to_a ).map { |i| i.chr }
all = big + small
gaps = %w{ n o E D Q P S T U}
template = File.read 'rotating_circles.template'

orig = big.join << small.join
shif = ( big[1 .. -1] << big[0] ).join << ( small[1 .. -1] << small[0] ).join

animation = [template]
175.times { |i| animation[i+1] = animation[i].tr( orig, shif ) }

File.open( 'rotating_circles', "w") do |f|
	f.puts ";rounds per frame:5"
	f.print animation.map { |frame| frame.tr( ( gaps + (all - gaps) ).join, (' ' * gaps.size) << 'O' ) }.join( ('=' * 44) << "\n" )
end

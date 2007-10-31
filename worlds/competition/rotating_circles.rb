# automatically produce a rotating circle map using
# rotating_circles.template

all = ( (?a .. ?z).to_a + (?A .. ?R).to_a ).map { |i| i.chr }
gaps = %w{ n o E D Q P }
template = File.read 'rotating_circles.template'

orig = all.join
shif = ( all[1 .. -1] << all[0] ).join

animation = [template]
43.times { |i| animation[i+1] = animation[i].tr( orig, shif ) }

File.open( 'rotating_circles', "w") do |f|
	f.puts ";rounds per frame:5"
	f.print animation.map { |frame| frame.tr( ( gaps + (all - gaps) ).join, (' ' * gaps.size) << 'O' ) }.join( ('=' * 44) << "\n" )
end

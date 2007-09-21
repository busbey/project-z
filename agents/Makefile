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
all: C_Development_Kit.md5 Java_Development_Kit.md5 Ruby_Development_Kit.md5 Perl_Development_Kit.md5 Python_Development_Kit.md5
.PHONY: all clean
.PRECIOUS: %.tar.gz %.tar.bz2 %.zip %_Development_Kit
clean:
	rm -rf *.zip *.bz2 *.gz *_Development_Kit *.md5
%.md5: %.tar.gz %.tar.bz2 %.zip
	md5 $^ > $@
%.tar.gz: %
	tar czf $@ $*
%.tar.bz2: %
	tar cjf $@ $*
%.zip: %
	rm -f $@
	zip -r $@ $*
%_Development_Kit :: % ../gpl.txt ../server/Server.jar
	cd $<
	$(MAKE) -f $(CURDIR)/$</Makefile clean
	cd ..
	cp -r $< $@
	rm -rf $@/.svn
	cp ../gpl.txt $@/
	cp ../server/Server.jar $@/
../server/Server.jar:
	cd ../server
	$(MAKE) -f $(CURDIR)/../server/Makefile jar
	cd ../agents

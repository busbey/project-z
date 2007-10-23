#!/usr/bin/awk -f
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
BEGIN{FS="";} 
{
  srand();
  rand();
  rand();
  rand();
  for(i = 1; i <= length($0); i++)
  {
  	for(j = 1; j < int(100 * rand());j++)
  	{
  		rand();
  	}
    offset=int(length($0) * rand()) + 1;
	while("" != arrange[offset]) 
	{
	  offset=int(length($0) * rand()) + 1;
	}
	arrange[offset]=substr($0,i,1); 
  }
  for(i = 1; i <= length($0);i++)
  {
  	chars = chars arrange[i];
  }
  printf("%s",chars);
} 
END{}

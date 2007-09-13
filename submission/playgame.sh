#!/bin/bash
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


PATH=$PATH:/bin

java server/Server ./map > results &
pid=$!

pushd bug
make buggo > /dev/null 2>&1 &
popd

pushd firsthunters
make hunter1go > /dev/null 2>&1 &
make hunter2go > /dev/null 2>&1 &
popd

pushd secondhunters
make hunter1go > /dev/null 2>&1 &
make hunter2go > /dev/null 2>&1 &
popd

while [ 2 -eq `ps -p $pid | wc -l | xargs expr` ]
do
done


#!/bin/bash
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

PATH=$PATH:/bin

mkdir results

pushd server
java Server ./map > ../results/server.out 2> ../results/server.err &
pid=$!
popd

sleep 20

pushd bug
make buggo > ../results/bug.out 2> ../results/bug.err &
popd

pushd firsthunters
make hunter1go > ../results/firsthunters1.out 2> ../results/firsthunters1.err &
make hunter2go > ../results/firsthunters2.out 2> ../results/firsthunters2.err &
popd

pushd secondhunters
make hunter1go > ../results/secondhunters1.out 2> ../results/secondhunters1.err &
make hunter2go > ../results/secondhunters2.out 2> ../results/secondhunters2.err &
popd

while [ 2 -eq `ps -p $pid | wc -l | xargs expr` ]
do
echo > /dev/null
done

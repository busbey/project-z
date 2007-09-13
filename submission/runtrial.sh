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


rm -rf chroot
mkdir chroot

rsync -av common/ chroot/

BUG=`ls bugs/ | randomline.awk`
rsync -av bugs/$BUG/ chroot/bug
pushd chroot/bug
make > /dev/null 2>&1
popd

HUNTER1=`ls hunters/ | randomline.awk`
rsync -av hunters/$HUNTER1/ chroot/firsthunters
pushd chroot/firsthunters
make > /dev/null 2>&1
popd

HUNTER2=`ls hunters/ | grep -v $HUNTER1 | randomline.awk`
rsync -av hunters/$HUNTER2/ chroot/secondhunters
pushd chroot/secondhunters
make > /dev/null 2>&1
popd

MAP=`ls maps/ | randomline.awk`
cp $MAP chroot/map

cp playgame.sh chroot/
chroot chroot/ chroot/playgame.sh
cp chroot/results results/results-$BUG-$HUNTER1-$HUNTER2-`date +'%F-%H-%M'`

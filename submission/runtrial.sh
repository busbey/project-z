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

sudo rm -rf chroot
mkdir chroot

sudo rsync -av common/ chroot/

BUG=`ls bugs/ | ./randomline.awk`
echo "bug is ${BUG}"
rsync -av bugs/$BUG/ chroot/bug
pushd chroot/bug
make > /dev/null 2>&1
popd

HUNTER1=`ls hunters/ | ./randomline.awk`
echo "hunter1 is ${HUNTER1}"
rsync -av hunters/$HUNTER1/ chroot/firsthunters
pushd chroot/firsthunters
make > /dev/null 2>&1
popd

HUNTER2=`ls hunters/ | grep -v $HUNTER1 | ./randomline.awk`
echo "hunter2 is ${HUNTER2}"
rsync -av hunters/$HUNTER2/ chroot/secondhunters
pushd chroot/secondhunters
make > /dev/null 2>&1
popd

MAP=`ls maps/ | ./randomline.awk`
cp maps/$MAP chroot/map

cp playgame.sh chroot/
sudo chroot chroot/ /playgame.sh

RESULTS_DIR=results-$BUG-$HUNTER1-$HUNTER2-`date +'%F-%H-FM'`
mkdir "results/${RESULTS_DIR}"
mkdir "results/${RESULTS_DIR}/${BUG}"
mkdir "results/${RESULTS_DIR}/${HUNTER1}"
mkdir "results/${RESULTS_DIR}/${HUNTER2}"

cp chroot/results/server.* "results/${RESULTS_DIR}/${BUG}"
cp chroot/results/server.* "results/${RESULTS_DIR}/${HUNTER1}"
cp chroot/results/server.* "results/${RESULTS_DIR}/${HUNTER2}"

cp chroot/results/bug.* "results/${RESULTS_DIR}/${BUG}"
cp chroot/results/bug.* "results/${RESULTS_DIR}/${HUNTER1}"
cp chroot/results/bug.* "results/${RESULTS_DIR}/${HUNTER2}"

pushd results
tar cvfj "${BUG}.tar.bz2" "${BUG}/"
tar cvfj "${HUNTER1}.tar.bz2" "${HUNTER1}/"
tar cvfj "${HUNTER2}.tar.bz2" "${HUNTER2}/"
popd

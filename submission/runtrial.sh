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

rm bugblacklist hunterblacklist
touch bugblacklist hunterblacklist

result=0
while [ $result -eq 0 ]
do
	BUG=`ls bugs/ | sort | comm -23 - bugblacklist | ./randomline.awk`
	if [ "${BUG}" = "" ]
	then
			exit
  fi
	rsync -av "bugs/${BUG}/" chroot/bug
	pushd chroot/bug
	if make all > ../../bug.buildlog 2>&1
	then
			result=1
			popd
	else
			popd
			rm -rf chroot/bug
			mkdir "results/${RESULTS_DIR}/${BUG}"
			cp bug.buildlog "results/${RESULTS_DIR}/${BUG}"
			pushd "${RESULTS_DIR}"
			tar cvfj "${BUG}.tar.bz2" "${BUG}/"
			popd
			echo $BUG >> bugblacklist
			sort -o bugblacklist bugblacklist
	fi
done

echo $BUG >> hunterblacklist

result=0
while [ $result -eq 0 ]
do
	HUNTER1=`ls hunters/ | sort | comm -23 - hunterblacklist | ./randomline.awk`
	if [ "${HUNTER1}" = "" ]
	then
			exit
  fi
	rsync -av "hunters/${HUNTER1}/" chroot/firsthunters
	pushd chroot/firsthunters
	if make all > ../../firsthunters.buildlog 2>&1
	then
			result=1
			popd
	else
			popd
			rm -rf chroot/firsthunters
			mkdir "results/${RESULTS_DIR}/${HUNTER1}"
			cp firsthunters.buildlog "results/${RESULTS_DIR}/${HUNTER1}"
			pushd "${RESULTS_DIR}"
			tar cvfj "${HUNTER1}.tar.bz2" "${HUNTER1}/"
			popd
			echo $HUNTER1 >> hunterblacklist
			sort -o hunterblacklist hunterblacklist
	fi
done

echo $HUNTER1 >> hunterblacklist

result=0
while [ $result -eq 0 ]
do
	HUNTER2=`ls hunters/ | sort | comm -23 - hunterblacklist | ./randomline.awk`
	if [ "${HUNTER2}" = "" ]
	then
			exit
  fi
	rsync -av "hunters/${HUNTER2}/" chroot/secondhunters
	pushd chroot/secondhunters
	if make all > ../../secondhunters.buildlog 2>&1
	then
			result=1
			popd
	else
			popd
			rm -rf chroot/secondhunters
			mkdir "results/${RESULTS_DIR}/${HUNTER2}"
			cp secondhunters.buildlog "results/${RESULTS_DIR}/${HUNTER2}"
			pushd "${RESULTS_DIR}"
			tar cvfj "${HUNTER2}.tar.bz2" "${HUNTER2}/"
			popd
			echo $HUNTER2 >> hunterblacklist
			sort -o hunterblacklist hunterblacklist
	fi
done

echo $HUNTER2 >> hunterblacklist

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
cp chroot/results/firsthunters* "results/${RESULTS_DIR}/${HUNTER1}"
cp chroot/results/secondhunters* "results/${RESULTS_DIR}/${HUNTER2}"

cp bug.buildlog "results/${RESULTS_DIR}/${BUG}"
cp firsthunters.buildlog "results/${RESULTS_DIR}/${HUNTER1}"
cp secondhunters.buildlog "results/${RESULTS_DIR}/${HUNTER2}"

pushd results
tar cvfj "${BUG}.tar.bz2" "${BUG}/"
tar cvfj "${HUNTER1}.tar.bz2" "${HUNTER1}/"
tar cvfj "${HUNTER2}.tar.bz2" "${HUNTER2}/"
popd

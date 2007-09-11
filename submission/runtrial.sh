#!/bin/bash

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
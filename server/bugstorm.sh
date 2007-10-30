pwd=`pwd`
cat /dev/null > "${pwd}/bugPids"
echo "${pwd}"
for bug in `find $1 -type f -iname "Makefile" -not -regex ".*/\.svn/.*" -exec grep -l "buggo:" {} \; | cut -d / -f 1,2,3`; do
	pushd "${bug}" &>/dev/null < /dev/null
	make buggo &>/dev/null < /dev/null &
	pid=$!
	echo "${pid}" >> "${pwd}/bugPids"
	popd &>/dev/null < /dev/null
done;
sleep 30
for bugpid in `cat ${pwd}/bugPids`; do
	kill -9 "${bugpid}" &> /dev/null
done;

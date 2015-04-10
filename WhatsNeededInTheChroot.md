what we need in the chroot:

/bin:

java
perl
python
ruby
make
wc
xargs
bash
env
i also have everything else that's standard in /bin on OS X in there?

/server:

everything from project-z/server and Server.java needs to be compiled

/System:

a copy of /System/Library/Frameworks on OS X
a copy of /System/Library/Perl on OS X

/usr/lib:

a complete copy of /usr/lib with symlinks intact

/usr/bin:

symlinks to /bin/python /bin/perl /bin/ruby /bin/env

/dev:

a copy of /dev/null


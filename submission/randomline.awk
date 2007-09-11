#!/usr/bin/awk -f

{
	lines[NR] = $0;
}

END {
	srand();
	for (i = 1; i < 200; i++) 
	{
		rand()
	}
	print lines[int(NR * rand()) + 1]
}

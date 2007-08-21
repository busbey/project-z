<?php
	/** @brief filesystem path to store uploads in */
	$uploadDir = '/Library/WebServer/z';

	/** @brief allowed compression formats */
	$uploadTypes = Array(
		'POSIX tar archive',
		'gzip compressed data, from Unix',
		'POSIX tar archive (gzip compressed data, from Unix)',
		'bzip2 compressed data, block size = 900k',
		'POSIX tar archive (bzip2 compressed data, block size = 900k)',
		'Zip archive data, at least v1.0 to extract',
	);
	/** @brief command to use to check types */
	$uploadCheck = '/usr/bin/file';
	
?>

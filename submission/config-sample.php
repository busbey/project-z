<?php
	/** @brief filesystem path to store uploads in */
	$uploadDir = '/Library/WebServer/z';

	/** @brief allowed compression formats */
	$uploadTypes = Array(
		'POSIX tar archive' => 'tar',
		'gzip compressed data, from Unix'	=> 'tar.gz',
		'POSIX tar archive (gzip compressed data, from Unix)'	=> 'tar.gz',
		'bzip2 compressed data, block size = 900k'	=> 'tar.bz2',
		'POSIX tar archive (bzip2 compressed data, block size = 900k)'	=> 'tar.bz2',
		'Zip archive data, at least v1.0 to extract' => 'zip',
	);
	
	/** @brief command to use to check types */
	$uploadCheck = '/usr/bin/file -b -z';
	
	/** @brief placeholder for identity.  need to map user to teams */
	$_SESSION['team'] = $_REQUEST['team'];
?>

<?php
/* Copyright (C) 2007  Sean Busbey, Roman Garnett, Brad Skaggs, Paul Ostazeski
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

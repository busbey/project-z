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

	require_once('config.php');
	if(isset($_REQUEST['submit']))
	{
		$check = $uploadCheck . ' ' . $_FILES['usersource']['tmp_name'];
		$rawType = exec($check);	
		if(array_key_exists($rawType, $uploadTypes))
		{
			$type = $uploadTypes[$rawType];
			$storedfile = $uploadDir . '/' . $_SESSION['team'].'.'.$type;
			if(true === move_uploaded_file($_FILES['usersource']['tmp_name'], $storedfile))
			{
				echo "Successfully stored submission for ".$_SESSION['team'];
			}
			else
			{
				echo "Error handling upload: Couldn't move to destination.";
				exit();
			}
		}
		else
		{
			echo "Error handling upload: Unkown file type. (".$check.' => '.$rawType.")";
			exit();
		}
	}
	else
	{
		?>
<form enctype="multipart/form-data" action="index.php" method="POST">
Choose your sumbission bundle: <input name="usersource" type="file" /><br />
<input type="submit" name="submit" value="Upload File" />
<input type="hidden" name="team" value="<?php echo $_SESSION['team']; ?>" />
</form>
		<?php
	}
?>

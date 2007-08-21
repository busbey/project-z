<?php
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

<?php
	require_once('config.php');
	if(isset($_REQUEST['submit']))
	{
		$rawType = `$uploadCheck $_FILES['uploadedfile']['tmp_name']`;	
		if(array_key_exists($rawType, $uploadTypes))
		{
			$type = $uploadTypes[$rawType];
			$storedfile = $uploadDir . '/' . $_SESSION['team']. $type;

		}
		else
		{
			echo "Error handling upload: Unkown file type.";
			exit();
		}
	}
	else
	{
		?>
<form enctype="multipart/form-data" action="index.php" method="POST">
Choose your sumbission bundle: <input name="uploadedfile" type="file" /><br />
<input type="submit" name="submit" value="Upload File" />
</form>
		<?php
	}
?>

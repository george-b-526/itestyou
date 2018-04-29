<?php

function actionHash($action, $user_id, $unit_id) {
	return md5($action.$user_id.$unit_id);
}

function xmlescape($string) {
    return strtr($string, array("<", ">", "\"", "'", "&"),
        array("&lt;", "&gt;", "&quot;", "&apos;", "&amp;"));
}

function csvToXml($fname, $name, &$xml, &$error, &$row){
	$xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	$xml .= "<vocb name=\"".htmlentities($name)."\">\n";
	if (($handle = fopen($fname, "r")) !== FALSE) {
		while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
			$num = count($data);
			if ($num != 3) {
				$error = "Expected 3 items on line ".$row.", got: ".htmlentities($data);
				return;
			}
			$xml .= "<w e=\"".$data[0]."\" i=\"".$row."\" m=\"".$data[1]."\" t=\"".$data[2]."\" />\n";
			
			$row++;
			if ($row > 500) {
				$error = "Can't exceed 500 rows.";
				return;
			}
		}
		fclose($handle);
	}
	$xml .= "</vocb>";
}

function ity_render_toolbox_upload_vocb(){
	global $ity_render_toolbox_upload_vocb_lambda;
	if ($ity_render_toolbox_upload_vocb_lambda){
		return $ity_render_toolbox_upload_vocb_lambda;
	}

	include_once "../lib/dao.php";


	// bind to user
	$reason = null;  
	$name = null;
	$is_pro = null;
	$app_session = $_GET["app_session"];
	$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
	if ($user_id == null){
		echo "<p align='center'>Please <a href='/cms/log-in'>login</a> to see this page.</p>";
		return;
	}

	$is_upload = $_POST['wpUpload'] != null;
    if ($is_upload) {
		if ($_FILES["wpUploadFile"]["error"] > 0) {
		  $body = "Error: " . htmlentities($_FILES["wpUploadFile"]["error"]) . "<br />";
		} else {
			$title = $_POST["wpName"];
			if (strlen($title) > 64 || strlen($title) < 4) {
				return "Please provide title between 4 and 64 character long.";
			}
		  
		  
		  $body = "";
		  $body .= "Upload: " . htmlentities($_FILES["wpUploadFile"]["name"]) . "<br />";
		  $body .= "Size: " . htmlentities((int) ($_FILES["wpUploadFile"]["size"] / 1024)) . " Kb<br />";
   

          if ($_FILES["wpUploadFile"]["size"] / 1024 > 50) {
			$body .= "<b>File is too large!</b>";
		  } else {
            $fname = $_FILES["wpUploadFile"]["tmp_name"];

			$xml = "";
			$error = null;
			$rows = 0;
			csvToXml($_FILES["wpUploadFile"]["tmp_name"], $title, $xml, $error, $rows);
			if ($error) {
				$body .= "<b>Error: ".htmlentities($error)."</b>";
			} else {
				$body .= "Content received: ".$rows." rows<br />";
				$body .= "XML created: ".strlen($xml)." bytes<br />";

				$dbh = ity_db();
				$qry = ity_qry($dbh, "SELECT COUNT(*) AS COUNT FROM ITY_ADMIN.UNI_UNIT WHERE UNI_TYPE = 2 AND UNI_STATE = 1 AND UNI_OWNER_USR_ID = %s", array($user_id));
				$row = mysql_fetch_assoc($qry);
				$count = $row["COUNT"];
				if ($count >= 10) {
					$body .= "<b>Error: You have already reached a maximum of ".$count." items. Please delete one of the existing items and try again.</b>";
				} else {
					$qry = ity_qry($dbh, 
						"INSERT INTO ITY_ADMIN.UNI_UNIT
						(UNI_OWNER_USR_ID, UNI_XML, UNI_TITLE, UNI_NOTES, UNI_GRADE, UNI_TYPE, UNI_STATE) VALUES 
						(%s, COMPRESS('%s'), '%s', '', 0, 2, 1)" , array($user_id, $xml, $title));
					$qry = ity_qry($dbh, "SELECT LAST_INSERT_ID() AS ID");
					$row = mysql_fetch_assoc($qry);
					$body .= "Success! Here is your <a href='/cms/unit-by-id?id=".$row["ID"]."&engine=vocb'>new vocabulary unit</a>.";
				}
			}
		  }
		}
		return $body;
	}

	$is_delete = $_POST['wpDelete'] != null;
    if ($is_delete) {
		$body = "";
		$unit_id = (int) $_POST['wpDeleteUnitId'];
		if (actionHash("delete", $user_id, $unit_id) != $_POST['wpActionHash']) {
			return "Bad action. Try <a href='/cms/my-vocabulary'>again</a>.";
		}
		$dbh = ity_db();
		ity_qry($dbh, "UPDATE ITY_ADMIN.UNI_UNIT SET UNI_STATE = 2 WHERE UNI_ID = %s AND UNI_OWNER_USR_ID = %s", array($unit_id, $user_id));
		return "Deleted! <a href='/cms/my-vocabulary'>Continue</a>.";
	}

	// default view
	$items = "<ol>";
	$dbh = ity_db();
	$qry = ity_qry($dbh, "SELECT UNI_ID, UNI_TITLE FROM ITY_ADMIN.UNI_UNIT WHERE UNI_TYPE = 2 AND UNI_STATE = 1 AND UNI_OWNER_USR_ID = %s", array($user_id));
	while($row = mysql_fetch_assoc($qry)){
		$items .= 
			"<li><a href='/cms/unit-by-id?id=".$row["UNI_ID"]."&engine=vocb'>".htmlentities($row["UNI_TITLE"])."</a>
			<form action='/cms/my-vocabulary' method='POST'>
			<input type='hidden' name='wpActionHash' value='".actionHash("delete", $user_id, $row["UNI_ID"])."' />
			<input type='hidden' name='wpDeleteUnitId' value='".$row["UNI_ID"]."' />
			<input type='submit' value='Delete' name='wpDelete' onClick='return confirm(\"Are you sure you want to delete this item? ALL RELATED DATA and your progress will be lost!\");'/>
			</form></li>\n";
	}
	$items .= "</ol>";


	if ($items == "<ol></ol>") {
		$items = "<p>no items yet</p>";
	}

	$body = '
	    <p>We provide many existing vocabularies. You can also create your own!
		Here is the page where you can create and manage your custom vocabularies.</p>
		<h2>View Existing</h2>
		'.$items.'
		<h2>Create New</h2>
		<p>You need to prepare a CSV file with the following three columns: the word, the word definition, the word part of speech or category.
		You can prepare the file manually or you can use Microsoft Excel and save the file as CSV.</p>
		<p>Here is an example of the <a href="/toolbox/vocb/sample-vocabluary.csv">CSV file</a> we prepared, and here is the <a href="/toolbox/vocb/sample-vocabluary.xls">Microsoft Excel document</a> we used to prepare it.
		The file size can\'t exceed 100K and you can have at most 500 word definitions in one file.</p>
		<p>Follow these steps:</p>
		<form enctype="multipart/form-data" action="/cms/my-vocabulary" method="POST">
			<input type="hidden" name="MAX_FILE_SIZE" value="50000" />
			<ul>
			<li><label for="file">Choose a CSV file to upload (50K max size):</label>
			<input name="wpUploadFile" type="file" id="file"/></li>
			<li><label for="wpName">Give it a friendly name:</label>
			<input name="wpName" size="50" value="Words for the Test"/></li>
			<li><input type="submit" value="Upload File & Create New Vocabulary" name="wpUpload"/></li>
			</ul>
		</form>';

	return $body;
}

?>
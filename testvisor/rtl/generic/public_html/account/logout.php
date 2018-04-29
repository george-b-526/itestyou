<?php
	$expires = time() - 60 * 60 * 24 * 30;	// now minus 30 days  
	setcookie("web_session", "", $expires, "/", ".itestyou.com");
	header("Location: http://www.itestyou.com/");
	exit;
?>
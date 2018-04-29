<?php

function execCMD($cmd){
	echo "<pre>";
	$last_line = system($cmd, $retval);
	echo "</pre>";

	echo "<br />Last line of the output: ".$last_line;
	echo "<br />Return value:".$retval;
	echo "<hr />";
}

$cmd = "mysqldump --complete-insert --single-transaction --add-drop-database --add-drop-table --host=localhost --user=admin --password=admin --databases ";

$ns = date('Y-m-d.h-i-s');

//
// Testvisor
//
$home = "/oy/backup/mysql";
execCMD($cmd."ITY_ADMIN > ".$home."/ity.admin.".$ns.".sql");
execCMD($cmd."ITY_IDENTITY > ".$home."/ity.identity.".$ns.".sql");
execCMD($cmd."ITY_RUNTIME > ".$home."/ity.runtime.".$ns.".sql");
execCMD($cmd."ITY_LOCAL > ".$home."/ity.local.".$ns.".sql");
execCMD($cmd."ITY_WORDPRESS> ".$home."/ity.wordpress.".$ns.".sql");

?>


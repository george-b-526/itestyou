<?php
	function execCMD($cmd){
		echo "<pre>";
		$last_line = system($cmd, $retval);
		echo "</pre>";

		echo "<br />Last line of the output: ".$last_line;
		echo "<br />Return value:".$retval;
		echo "<hr />";
	}

	$cmd = "mysqlimport -u admin -padmin ";
	$cmd = "mysql -u admin -padmin ";

	$ns = "2015-03-17.02-29-06";

	$home = "/oy/backup/mysql";
	execCMD($cmd." < ".$home."/ity.admin.".$ns.".sql");
	execCMD($cmd." <".$home."/ity.identity.".$ns.".sql");
	execCMD($cmd." < ".$home."/ity.runtime.".$ns.".sql");
	execCMD($cmd." < ".$home."/ity.local.".$ns.".sql");
	execCMD($cmd." < ".$home."/ity.wordpress.".$ns.".sql");
?>


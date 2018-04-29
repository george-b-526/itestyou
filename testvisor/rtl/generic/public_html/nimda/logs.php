<?php

	echo "<div style='font-size: 12px;'>";
	echo "<style>PRE {margin: 0px; padding: 0px;} HR {margin: 2px; padding: 0px;}</style>";

	echo "<pre>";
	$last_line = system("sar -u 1 3", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre>";
	$last_line = system("df", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre style='background-color: #FFD0D0;'>";
	$last_line = system("tail --lines=20 0 $(ls -t /oy/testvisor/rtl/generic/logs/error.log.* | head -1)", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre style='background-color: #FFD0D0;'>";
	$last_line = system("tail --lines=20 0 /usr/java/tomcat-5.5/logs/catalina.out", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre>";
	$last_line = system("tail --lines=20 0 $(ls -t /oy/testvisor/rtl/generic/logs/access.log.* | head -1)", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre>";
	$last_line = system("tail --lines=500 0 $(ls -t /oy/testvisor/rtl/generic/logs/error.log.* | head -1)", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre>";
	$last_line = system("tail --lines=500 0 /usr/java/tomcat-5.5/logs/catalina.out", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "<pre>";
	$last_line = system("tail --lines=500 0 $(ls -t /oy/testvisor/rtl/generic/logs/access.log.* | head -1)", $retval);
	echo "</pre>";
	echo "<hr />";

	echo "</div>";

?>
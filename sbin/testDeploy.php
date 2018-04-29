<?php

function httpGet($url) {
	$ch = curl_init();
	$timeout = 5;
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
	$data = curl_exec($ch);
	if ($data === false) {
		echo 'ERROR (error fetching '.$url.'): ' . curl_error($ch);
		return false;
	}
	curl_close($ch);
	return $data;
}

function check_http($url, $min_len, $text) {
	$data = httpGet($url);
	if (strlen($data) < $min_len) {
		exit ("ERROR in GET (file too small): ".$url."\n".$data."\n");
	}
	if ($text !== false && strpos($data, $text) === false) {
		exit ("ERROR in GET (failed to find text): ".$text."\n");
	}
}

function check($url, $min_len, $text) {
	check_http("http://localhost".$url, $min_len, $text);
	check_http("https://localhost".$url, $min_len, $text);
}

# home page
check("/", 25000, "SAT and TOEFL vocabulary, Math Worksheets, Foreign Languages");

# dynamic plot
check("/api/ml/qdrtcplot?inA=3&inB=6&inC=-9", 7000, "PNG");

# high scores
check("/cms/math-worksheets-leaderboard", 28000, "Math Worksheets Leaderboard");

# category view
check("/cms/category/math-grade-4", 37000, "Grade 4 Math Worksheets");

# test view
check("/test/wdgt?action_id=0&inUnitId=379&inLocale=en", 2000, "Which of the following is used to find out how many inches are in ");

# admin view
check_http("http://localhost:8080/admin/bin/view", 1300, "Please login");
check("/admin/bin/view", 1300, "Please login");

# REST API
check_http("http://localhost:8080/api/identity", 1, "<ity-result verb='unknown' ver='1.0'><code>400</code>");
check("/api/identity", 1, "<ity-result verb='unknown' ver='1.0'><code>400</code>");

# MathTex
check("/api/ml/math?2+2", 197, "GIF");

# todo
# exit ("ERROR (complete todo's)\n");

# check email send out
# recover-mail-url=http://www.itestyou.com/api/mail/recover.php
# authorize-name=admin
# authorize-pwd=yetrEvubat6m

?>
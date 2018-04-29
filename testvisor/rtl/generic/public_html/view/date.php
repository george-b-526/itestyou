<?php

	function ity_date_wy($now, &$now_year, &$now_week, &$prior_week, &$prior_year, &$next_year, &$next_week){
		$now_year = date("o", $now);
		$now_week = date("W", $now);

		$past = $now - 7 * 24 * 60 * 60;
		$future = $now + 7 * 24 * 60 * 60;

		$prior_year = date("o", $past);
		$prior_week = date("W", $past);

		$next_year = date("o", $future);
		$next_week = date("W", $future);
	}

	function ity_assert($from, $to){
		if ($from != $to){
			echo "Fail: ".$from." != ".$to."<br />";
		}
	}

	// week inside the middle of the year
	$nov_13_2011 = strtotime("2011-11-13");		// sun
	$nov_14_2011 = strtotime("2011-11-14");		// mon

	// last week of year with last day in a next year
	$dec_25_2011 = strtotime("2011-12-25");		// sun
	$dec_26_2011 = strtotime("2011-12-26");		// mon

	// first week of the next year
	$jan_1_2012 = strtotime("2012-01-01");		// sun
	$jan_2_2012 = strtotime("2012-01-02");		// mon

	// verify
	ity_date_wy($nov_13_2011, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	ity_assert($now_year, 2011); ity_assert($now_week, 45);
	ity_assert($prior_year, 2011); ity_assert($prior_week, 44);
	ity_assert($next_year, 2011); ity_assert($next_week, 46);
	
	ity_date_wy($nov_14_2011, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	ity_assert($now_year, 2011); ity_assert($now_week, 46);
	ity_assert($prior_year, 2011); ity_assert($prior_week, 45);
	ity_assert($next_year, 2011); ity_assert($next_week, 47);


	ity_date_wy($dec_25_2011, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	ity_assert($now_year, 2011); ity_assert($now_week, 51);
	ity_assert($prior_year, 2011); ity_assert($prior_week, 50);
	ity_assert($next_year, 2011); ity_assert($next_week, 52);

	ity_date_wy($dec_26_2011, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	ity_assert($now_year, 2011); ity_assert($now_week, 52);
	ity_assert($prior_year, 2011); ity_assert($prior_week, 51);
	ity_assert($next_year, 2012); ity_assert($next_week, 1);


	ity_date_wy($jan_1_2012, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	ity_assert($now_year, 2011); ity_assert($now_week, 52);
	ity_assert($prior_year, 2011); ity_assert($prior_week, 51);
	ity_assert($next_year, 2012); ity_assert($next_week, 1);


	ity_date_wy($jan_2_2012, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	ity_assert($now_year, 2012); ity_assert($now_week, 1);
	ity_assert($prior_year, 2011); ity_assert($prior_week, 52);
	ity_assert($next_year, 2012); ity_assert($next_week, 2);

	echo "OK";	

?>
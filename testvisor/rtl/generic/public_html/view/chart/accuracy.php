<?php
	function pickcolor($img, $data_array, $row, $col) {
	  $d = $data_array[$row][$col+1]; // col+1 skips over the row's label
	  if ($d > 70) return 0;
	  if ($d > 35) return 1;
	  return 2;
	}
	
	include_once "common.php";

	// user_id
	$user_id = ity_bind_to_user();
	if ($user_id == null){
		return;
	}

	// get data and massage it
	$datasetEx = ity_chart_data_correct_incorrect($user_id);
	$dataset = array();
	for ($i=0; $i<count($datasetEx); $i++){
		$date = $datasetEx[$i][0];
		$incorrect = $datasetEx[$i][1];
		$correct = $datasetEx[$i][2];

		$rate = 0;
		if (($correct + $incorrect) != 0){
			$rate = 100 * $correct /($correct + $incorrect);
		}

		array_push($dataset, array($date, $rate));
	}

	// render
	$plot = ity_new_chart("Answers Accuracy");
	$plot->SetShading(0);

	$plot->SetYTitle('Correct Answers (%)');
	$plot->SetXTitle('Practice Week');

	$plot->SetPlotAreaWorld(NULL, 0, NULL, 109);
	$plot->SetDataColors("#A0A0FF");

	$plot->SetPlotType('bars');

    $plot->SetCallback('data_color', 'pickcolor', $dataset);
    $plot->SetDataColors(array('green', 'yellow', 'red'));

	$plot->SetDataType('text-data');
	$plot->SetDataValues($dataset);

	$plot->SetXTickLabelPos('none');
	$plot->SetXTickPos('none');
 
	$plot->SetXDataLabelAngle(90);
	$plot->SetYDataLabelPos('plotin');
    $plot->SetYTickIncrement(10);
    $plot->SetPrecisionY(0);
	
	ity_chart_render($plot);
?>
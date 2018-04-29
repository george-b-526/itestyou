<?php
	include_once "common.php";

	// user_id
	$user_id = ity_bind_to_user();
	if ($user_id == null){
		return;
	}

	// get data
	$dataset = ity_chart_data_correct_incorrect($user_id);
	$max = 0;
	for ($i=0; $i<count($dataset); $i++){
		$sum = $dataset[$i][1] + $dataset[$i][2];
		if ($sum > $max){
			$max = $sum;
		}
	}

	// render
	$plot = ity_new_chart("Practice Effort");
	$plot->SetShading(0);

	$plot->SetDataColors(array('salmon', 'green'));

	$plot->SetYTitle('Problems Solved');
	$plot->SetXTitle('Practice Week');

	$plot->SetPlotType('stackedbars');
	$plot->SetDataType('text-data');
	$plot->SetDataValues($dataset);

	$plot->SetLegend(array('Fail', 'Pass'));
	$plot->SetLegendReverse(True);

	$plot->SetXTickLabelPos('none');
	$plot->SetXTickPos('none'); 

	$plot->SetXDataLabelAngle(90);
	$plot->SetYDataLabelPos('plotstack');
	$plot->SetYTickIncrement(round($max / 10));
	$plot->SetPrecisionY(0);	

	ity_chart_render($plot);
?>
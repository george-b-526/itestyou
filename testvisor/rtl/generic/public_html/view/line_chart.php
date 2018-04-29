<?php
	$title = $_REQUEST['title'];
	$data = base64_decode($_REQUEST['data']);
	
	$dataset = array();
	$points = explode(";", $data);

	$pointCount = count($points);
	$i = 0;
	foreach ($points as $point){
		$xy = explode("\n", $point);
		if (count($xy) != 3) continue;
		
		if ($pointCount < 10 || ($i % ($pointCount / 10) == 0)){
			array_push($dataset, array($xy[2], $xy[0], $xy[1]));
		} else {
			array_push($dataset, array("", $xy[0], $xy[1]));
		}
		$i++;
	}

	include('/oy/testvisor/rtl/generic/library/pplot/phplot.php');

	header("Content-Type: image/png");	

	$graph = new PHPlot(800, 300);
	$graph->SetTitle($title);
	$graph->SetDataType("data-data");
	$graph->SetDataValues($dataset);	
	$graph->SetXDataLabelPos("plotdown");
    $graph->SetPointShapes("dot");
	$graph->SetPointSizes(3);

	$graph->SetXLabelAngle(90);
	$graph->SetIsInline(true);
	$graph->DrawGraph();
?>
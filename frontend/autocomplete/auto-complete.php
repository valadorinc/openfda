<?php

require('../config/constant.php');

if (!isset($_GET['type'])) {
	die("");
}

if (!isset($_GET['keyword'])) {
	die("");
}

$type = $_GET['type'];
$keyword = $_GET['keyword'];

if($type == "reaction") {
    $url = LOOKUP_REACTION_URL . $keyword;
}
else {
	$url = LOOKUP_DRUG_URL . $keyword;
}


$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
$output = curl_exec($ch);
curl_close($ch);
//take the raw output and convert it into a json object
$data = json_decode($output);
//Now that it's a json object we check the status code. 0 means success.
$StatusCode = $data->StatusCode;
if ($StatusCode == "0"){
	// if the call was successful there will be a 'body' containing all the data. In this case i just created an array with the results which we pull from the body.
	$body = $data->Body;
	$results = $body->results;
	echo json_encode($results);
}

?>

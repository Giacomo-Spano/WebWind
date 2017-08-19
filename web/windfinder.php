
<!DOCTYPE html>
<html>
<body>

PROVA PHP

<?php
echo "My first PHP script!";

// options
$username = '';
$password = '';
$cookie_file_path = "/home/pi/cookies.txt";

if (isset($_GET['spot'])) {
	$LOGINURL = "https://it.windfinder.com/forecast/" . $_GET['spot'];
} else {
	echo errore;
}

//$LOGINURL = "https://it.windfinder.com/forecast/lecco_galbiate";

// begin script
$ch = curl_init();

// extra headers
$headers[] = "Accept: */*";
$headers[] = "Connection: Keep-Alive";

// basic curl options for all requests
curl_setopt($ch, CURLOPT_HTTPHEADER,  $headers);
curl_setopt($ch, CURLOPT_HEADER,  0);
curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
//       curl_setopt($ch, CURLOPT_USERAGENT, $agent);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
curl_setopt($ch, CURLOPT_COOKIEFILE, $cookie_file_path);
curl_setopt($ch, CURLOPT_COOKIEJAR, $cookie_file_path);

// set first URL
curl_setopt($ch, CURLOPT_URL, $LOGINURL);

// execute session to get cookies and required form inputs
$html = curl_exec($ch);
curl_close($ch);

var_dump($html);
die;

?>

</body>
</html>


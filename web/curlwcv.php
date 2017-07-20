<!DOCTYPE html>
<html>
<body>

PROVA PHP

<?php
echo "My first PHP script!";

// options
$username = 'giacomo70';
$password = 'giacomo';
$cookie_file_path = "/home/pi/cookies.txt";
$LOGINURL = "wcv.it/forum/ucp.php?mode=login"; 


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
$content = curl_exec($ch); 

// grab the hidden inputs from the form required to login
$fields['username'] = $username;
$fields['password'] = $password;
$fields['login'] = 'Login';
$fields['autologin'] = 'on';
$fields['redirect'] = 'http://wcv.it';

// set postfields using what we extracted from the form
$POSTFIELDS = http_build_query($fields); 
// change URL to login URL
curl_setopt($ch, CURLOPT_URL, $LOGINURL); 

// set post options
curl_setopt($ch, CURLOPT_POST, 1); 
curl_setopt($ch, CURLOPT_POSTFIELDS, $POSTFIELDS); 

// perform login
$result = curl_exec($ch);  

print $result; 


$grab_url='http://wcv.it';

//page with the content I want to grab
curl_setopt($ch, CURLOPT_URL, $grab_url);
//do stuff with the info with DomDocument() etc
$html = curl_exec($ch);
curl_close($ch);

var_dump($html); 
die;



?>

</body>
</html>

<?php
define('HOST','localhost');
define('USER','root');
define('PASS','');
define('DB','logindb');
$con = mysqli_connect(HOST,USER,PASS,DB);
$json = file_get_contents('php://input');
$obj = json_decode($json);

$username = $obj -> {'username'};
$password = $obj -> {'password'};
    $photo = $obj -> {'photo'};
$sql = "insert into users(username,password,photo) values ('$username','$password','$photo')";
$res = mysqli_query($con,$sql);
if(isset($res)){
echo 'success';
}else{
echo 'failure';
}
 
mysqli_close($con);
?>

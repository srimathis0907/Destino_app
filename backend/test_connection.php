<?php
header('Content-Type: application/json');
http_response_code(500); // We will still send an error code to trigger the error logic in your app
$response = ['status' => false, 'message' => 'SUCCESS: The test_connection.php file was reached!'];
echo json_encode($response);
exit;
?>
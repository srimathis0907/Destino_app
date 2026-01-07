<?php
header('Content-Type: application/json');

// This script will just receive all the POST data and print it back.
// This helps us confirm the server is getting everything from the app.

$response = [
    'status' => true,
    'message' => 'Data received successfully by test script.',
    'received_data' => $_POST // This line captures all data sent by the app
];

echo json_encode($response, JSON_PRETTY_PRINT);
?>
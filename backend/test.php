<?php
echo "Starting test...<br>";

// Use your existing config file for the connection
require_once 'config.php';

// Check if the connection variable $conn exists from config.php
if (!$conn) {
    die("ERROR: Could not connect to the database. Check config.php.");
}

echo "Database connection successful.<br>";

// Prepare a very simple insert query
$sql = "INSERT INTO test_table (test_name) VALUES (?)";
$stmt = $conn->prepare($sql);

if ($stmt === false) {
    die("ERROR: Could not prepare the SQL statement. Error: " . $conn->error);
}

$test_data = "Hello World";
$stmt->bind_param("s", $test_data);

// Execute the query
if ($stmt->execute()) {
    echo "SUCCESS: The test data was inserted into test_table.";
} else {
    // If it fails, it will print the exact MySQL error here
    echo "ERROR: The final query failed. MySQL Error: " . $stmt->error;
}

$stmt->close();
$conn->close();
?>
<?php
header("Content-Type: application/json");
include 'config.php';

// This query groups by place_name, counts each occurrence, and orders by the count descending.
// It returns the top 5 most popular places.
$sql = "SELECT place_name, COUNT(id) as trip_count 
        FROM trips 
        WHERE status != 'cancelled'
        GROUP BY place_name 
        ORDER BY trip_count DESC 
        LIMIT 5";

$result = $conn->query($sql);

if ($result) {
    $stats = array();
    while($row = $result->fetch_assoc()) {
        $stats[] = $row;
    }
    echo json_encode(array("error" => false, "stats" => $stats));
} else {
    echo json_encode(array("error" => true, "message" => "Could not fetch popular places stats."));
}

$conn->close();
?>
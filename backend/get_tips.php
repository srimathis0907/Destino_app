<?php
header('Content-Type: application/json');
require_once 'config.php';

$sql = "SELECT id, tip_title, tip_content FROM travel_tips ORDER BY id DESC";
$result = $conn->query($sql);

$tips = [];
if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $tips[] = $row;
    }
}

echo json_encode(['status' => true, 'data' => $tips]);

$conn->close();
?>
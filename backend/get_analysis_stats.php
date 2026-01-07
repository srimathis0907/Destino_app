<?php
header('Content-Type: application/json');
require_once 'config.php';

$response = [
    'status' => true,
    'data' => [
        'total_users' => 0,
        'ongoing_trips' => 0,
        'cancellations' => 0,
        'avg_completed_cost' => 0.0
    ]
];

try {
    // 1. Get total users (UPDATED: Excludes the admin email)
    $stmt = $conn->prepare("SELECT COUNT(id) as total FROM users WHERE email != ?");
    $admin_email = 'admin123@gmail.com';
    $stmt->bind_param("s", $admin_email);
    $stmt->execute();
    $result = $stmt->get_result();
    $response['data']['total_users'] = (int)$result->fetch_assoc()['total'];

    // 2. Get ongoing (active) trips
    $result = $conn->query("SELECT COUNT(id) as total FROM trips WHERE status = 'active'");
    $response['data']['ongoing_trips'] = (int)$result->fetch_assoc()['total'];
    
    // 3. Get total cancelled trips
    $result = $conn->query("SELECT COUNT(id) as total FROM trips WHERE status = 'cancelled'");
    $response['data']['cancellations'] = (int)$result->fetch_assoc()['total'];

    // 4. Calculate average budget of 'finished' trips
    $result = $conn->query("SELECT AVG(total_budget) as avg_cost FROM trips WHERE status = 'finished' AND total_budget > 0");
    $avg_cost = $result->fetch_assoc()['avg_cost'];
    $response['data']['avg_completed_cost'] = $avg_cost ? (float)$avg_cost : 0.0;

} catch (Exception $e) {
    $response['status'] = false;
    $response['message'] = 'Failed to fetch stats: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>
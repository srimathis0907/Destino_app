<?php
header('Content-Type: application/json');
require_once 'config.php';

$chartType = $_GET['chart_type'] ?? 'trip_status';
$startDate = $_GET['start_date'] ?? null;
$endDate = $_GET['end_date'] ?? null;

$response = ['status' => true, 'data' => []];
$whereClause = '';
$params = [];
$types = '';

// Build the date filter WHERE clause if dates are provided
if ($startDate && $endDate) {
    $whereClause = ' WHERE created_at BETWEEN ? AND ?';
    $params = [$startDate . ' 00:00:00', $endDate . ' 23:59:59'];
    $types = 'ss';
}

try {
    switch ($chartType) {
        case 'trip_status':
            $sql = "SELECT status, COUNT(id) as count FROM trips" . str_replace('created_at', 'start_date', $whereClause) . " GROUP BY status";
            $stmt = $conn->prepare($sql);
            if (!empty($params)) $stmt->bind_param($types, ...$params);
            break;

        case 'trips_by_month':
            $sql = "SELECT DATE_FORMAT(start_date, '%Y-%m') as month, COUNT(id) as count FROM trips" . str_replace('created_at', 'start_date', $whereClause) . " GROUP BY month ORDER BY month ASC";
            $stmt = $conn->prepare($sql);
            if (!empty($params)) $stmt->bind_param($types, ...$params);
            break;
            
        case 'top_cancellers':
            $localWhere = str_replace('WHERE', 'AND', $whereClause);
            $sql = "SELECT u.fullname, COUNT(t.id) as count 
                    FROM trips t 
                    JOIN users u ON t.user_id = u.id 
                    WHERE t.status = 'cancelled'" . str_replace('created_at', 't.created_at', $localWhere) . "
                    GROUP BY t.user_id 
                    ORDER BY count DESC 
                    LIMIT 5";
            $stmt = $conn->prepare($sql);
            if (!empty($params)) $stmt->bind_param($types, ...$params);
            break;

        default:
            throw new Exception('Invalid chart type specified.');
    }

    $stmt->execute();
    $result = $stmt->get_result();
    $response['data'] = $result->fetch_all(MYSQLI_ASSOC);
    $stmt->close();

} catch (Exception $e) {
    $response['status'] = false;
    $response['message'] = 'Failed to fetch graph data: ' . $e->getMessage();
}

echo json_encode($response);
$conn->close();
?>
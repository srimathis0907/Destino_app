<?php
header("Content-Type: application/json");
include 'config.php';
mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT); // Added for better error reporting

// --- [FIX 1] Read POST data (form-data or raw JSON) ---
$data = $_POST;
if (empty($data)) {
    $rawInput = file_get_contents('php://input');
    $jsonData = json_decode($rawInput, true);
    if (json_last_error() === JSON_ERROR_NONE && is_array($jsonData)) {
        $data = $jsonData;
    }
}
// --- [END FIX 1] ---


// --- [FIX 2] Get and Validate Admin ID ---
$admin_id = $data['admin_id'] ?? null;

if (empty($admin_id)) {
     // This is the error you were seeing in Postman
     echo json_encode(array("error" => true, "message" => "Admin ID is required."));
     exit;
}

try {
    // Verify the admin_id against the 'users' table
    // Based on your screenshot, we check if id=8 AND username='Admin8'
    // If you add an 'is_admin' column later, change this query
    $adminCheckStmt = $conn->prepare("SELECT id FROM users WHERE id = ? AND username = 'Admin8'");
    if (!$adminCheckStmt) {
         throw new Exception("Admin check query failed to prepare: " . $conn->error);
    }
    
    $adminCheckStmt->bind_param("i", $admin_id); // Assuming 'id' is an integer
    $adminCheckStmt->execute();
    $adminResult = $adminCheckStmt->get_result();

    if ($adminResult->num_rows == 0) {
         // The ID was wrong or wasn't an admin
         echo json_encode(array("error" => true, "message" => "Authentication failed: Not a valid admin."));
         $adminCheckStmt->close();
         $conn->close();
         exit;
    }
    $adminCheckStmt->close();
    // --- [END FIX 2] ---


    // --- If Admin is Valid, Proceed with Original Query ---
    $sql = "SELECT
                t.id AS trip_id,
                t.place_name,
                t.place_location,
                t.start_date,
                t.end_date,
                t.num_people,
                t.num_days,
                t.total_budget,
                LOWER(t.status) AS status,
                u.fullname AS user_name
            FROM trips t
            JOIN users u ON t.user_id = u.id
            ORDER BY
                CASE
                    WHEN LOWER(t.status) = 'future' THEN 1
                    WHEN LOWER(t.status) = 'active' THEN 2
                    WHEN LOWER(t.status) = 'finished' THEN 3
                    WHEN LOWER(t.status) = 'completed' THEN 3
                    WHEN LOWER(t.status) = 'cancelled' THEN 4
                    ELSE 5
                END, t.start_date DESC";

    $result = $conn->query($sql);

    // This part is your existing success logic
    if ($result) {
        $trips = array();
        while ($row = $result->fetch_assoc()) {
            $trips[] = $row;
        }
        // This response {"error": false} means SUCCESS
        echo json_encode(array("error" => false, "trips" => $trips));
    } else {
        // This only runs if the $sql query itself fails
        echo json_encode(array("error" => true, "message" => "Could not fetch trips."));
    }

} catch (Exception $e) { // Catch any database or other errors
    http_response_code(500);
    echo json_encode(array("error" => true, "message" => "Server Error: " . $e->getMessage()));
}

$conn->close();
?>
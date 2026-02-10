<?php
header('Content-Type: application/json');
require_once 'config.php';

$response = ['status' => false, 'message' => 'No places found.', 'data' => []];

try {
    // We use a subquery to get the URL of the *first* image for each place.
    // This is more efficient than fetching all images for all places.
    $sql = "SELECT 
                p.id, 
                p.name, 
                (SELECT image_url FROM place_images WHERE place_id = p.id ORDER BY id ASC LIMIT 1) as image_url
            FROM 
                places p
            ORDER BY 
                p.id DESC";

    $result = $conn->query($sql);

    if ($result && $result->num_rows > 0) {
        $places = [];
        while ($row = $result->fetch_assoc()) {
            // Ensure image_url is not null
            $row['image_url'] = $row['image_url'] ?? 'default_image_path.jpg'; // Provide a fallback image if none exists
            $places[] = $row;
        }
        $response['status'] = true;
        $response['message'] = 'Places fetched successfully.';
        $response['data'] = $places;
    }

} catch (Exception $e) {
    $response['message'] = 'Database error: ' . $e->getMessage();
}

$conn->close();
echo json_encode($response);
?>
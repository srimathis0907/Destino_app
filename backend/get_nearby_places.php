<?php
// Included for consistency with your project structure. 
// Note: This script does not interact with your database, so config.php is not strictly required for its function.
include 'config.php'; 

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

// Overpass API endpoint
$overpass_url = "https://overpass-api.de/api/interpreter";

// Check if it's a POST request and if required parameters are set
if ($_SERVER['REQUEST_METHOD'] !== 'POST' || !isset($_POST['latitude']) || !isset($_POST['longitude']) || !isset($_POST['type'])) {
    http_response_code(400);
    echo json_encode(['status' => false, 'message' => 'Invalid request.']);
    exit;
}

$lat = floatval($_POST['latitude']);
$lon = floatval($_POST['longitude']);
$type = $_POST['type'];
$radius = 5000; // Search radius in meters (5km)

// Build the Overpass QL query based on the requested type
$query_part = "";
switch ($type) {
    case 'hotel':
        $query_part = 'nwr["tourism"~"hotel|motel|guest_house"](around:'.$radius.','.$lat.','.$lon.');';
        break;
    case 'restaurant':
        $query_part = 'nwr["amenity"~"restaurant|food_court"](around:'.$radius.','.$lat.','.$lon.');';
        break;
    case 'attraction':
        $query_part = 'nwr["tourism"~"attraction|museum|viewpoint|artwork"](around:'.$radius.','.$lat.','.$lon.');';
        break;
    default:
        http_response_code(400);
        echo json_encode(['status' => false, 'message' => 'Invalid place type specified.']);
        exit;
}

$query = '[out:json];' . $query_part . 'out center;';

// Use cURL to make the request to the Overpass API
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $overpass_url);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, "data=" . urlencode($query));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_USERAGENT, 'WeekendApp/1.0 (YourAppContact@example.com)');

$response_str = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($http_code != 200) {
    http_response_code(502); // Bad Gateway
    echo json_encode(['status' => false, 'message' => 'Failed to fetch data from Overpass API. Code: ' . $http_code]);
    exit;
}

$response_json = json_decode($response_str, true);
$places = [];

if (isset($response_json['elements'])) {
    foreach ($response_json['elements'] as $element) {
        if (isset($element['tags']['name'])) {
            $place_lat = 0.0;
            $place_lon = 0.0;

            if ($element['type'] == 'node') {
                $place_lat = $element['lat'];
                $place_lon = $element['lon'];
            } elseif (isset($element['center'])) {
                $place_lat = $element['center']['lat'];
                $place_lon = $element['center']['lon'];
            }

            if ($place_lat != 0.0) {
                $places[] = [
                    'name' => $element['tags']['name'],
                    'latitude' => $place_lat,
                    'longitude' => $place_lon
                ];
            }
        }
    }
}

if (empty($places)) {
    echo json_encode(['status' => false, 'message' => 'No nearby places found.']);
} else {
    $limited_places = array_slice($places, 0, 20);
    echo json_encode(['status' => true, 'data' => $limited_places]);
}

// Close the database connection if it was opened in config.php
if (isset($conn) && $conn instanceof mysqli) {
    $conn->close();
}
?>
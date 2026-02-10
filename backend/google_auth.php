<?php
// We add these two lines to see all errors
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require_once 'vendor/autoload.php';
require_once 'config.php'; // Your database connection

header('Content-Type: application/json');
$response = [];

$CLIENT_ID = "991194764836-dccd8idoic6smo5pnkafco4ru35panav.apps.googleusercontent.com"; // Make sure this is correct

// Manual verification function (keep for now)
function manualVerifyTokenSignature($id_token) {
    $google_certs_url = 'https://www.googleapis.com/oauth2/v3/certs';
    $certs_json = @file_get_contents($google_certs_url);
    if ($certs_json === false) { return ['valid' => false, 'error' => 'Failed to fetch Google certs']; }
    $certs = json_decode($certs_json, true);
    if (!isset($certs['keys'])) { return ['valid' => false, 'error' => 'Invalid certs format from Google']; }
    $token_parts = explode('.', $id_token);
    if (count($token_parts) !== 3) { return ['valid' => false, 'error' => 'Invalid token structure']; }
    list($header_base64, $payload_base64, $signature_base64) = $token_parts;
    $header_json = base64_decode(strtr($header_base64, '-_', '+/'));
    $header = json_decode($header_json, true);
    if (!isset($header['kid'])) { return ['valid' => false, 'error' => 'Token header missing kid']; }
    $kid = $header['kid'];
    $public_key_pem = null;
    foreach ($certs['keys'] as $key) {
        if (isset($key['kid']) && $key['kid'] === $kid && isset($key['n']) && isset($key['e'])) {
            $n = base64_decode(strtr($key['n'], '-_', '+/'));
            $e = base64_decode(strtr($key['e'], '-_', '+/'));
            $components = [
                'n' => new \phpseclib3\Math\BigInteger($n, 256),
                'e' => new \phpseclib3\Math\BigInteger($e, 256)
            ];
            $rsa = \phpseclib3\Crypt\RSA::load($components);
            $public_key_pem = $rsa->toString('PKCS8');
            break;
        }
    }
    if ($public_key_pem === null) { return ['valid' => false, 'error' => 'Matching public key not found for kid: ' . $kid]; }
    $message = $header_base64 . '.' . $payload_base64;
    $signature = base64_decode(strtr($signature_base64, '-_', '+/'));
    $result = openssl_verify($message, $signature, $public_key_pem, OPENSSL_ALGO_SHA256);
    if ($result === 1) { return ['valid' => true, 'error' => null];
    } elseif ($result === 0) { return ['valid' => false, 'error' => 'Signature verification failed (openssl_verify returned 0)'];
    } else { return ['valid' => false, 'error' => 'OpenSSL error: ' . openssl_error_string()]; }
}


if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $id_token = $_POST['idToken'] ?? '';

    if (empty($id_token)) {
        http_response_code(400);
        echo json_encode(['status' => false, 'message' => 'ID token is required.']);
        exit;
    }

    // Network test (keep this)
    $google_certs_url = 'https://www.googleapis.com/oauth2/v3/certs';
    $certs_json = @file_get_contents($google_certs_url);
    if ($certs_json === false) {
        $error = error_get_last();
        $error_message = 'Unknown network error';
        if ($error !== null && isset($error['message'])) { $error_message = $error['message']; }
        http_response_code(500);
        echo json_encode(['status' => false, 'message' => 'PHP failed to connect to Google certificate server: ' . $error_message]);
        exit;
    }

    try {
        $manual_check = manualVerifyTokenSignature($id_token);
        if (!$manual_check['valid']) {
            http_response_code(401);
            echo json_encode(['status' => false, 'message' => 'MANUAL CHECK FAILED: ' . $manual_check['error']]);
            exit;
        }

        // --- START: ADDED TIME/AUDIENCE LOGGING ---
        $token_parts = explode('.', $id_token);
        $payload_verified_manually = null;
        if(count($token_parts) === 3) {
             $payload_json = base64_decode(strtr($token_parts[1], '-_', '+/'));
             $payload_verified_manually = json_decode($payload_json, true);
        }

        $current_time = time();
        $log_message = "Manual check PASSED. ";
        if ($payload_verified_manually) {
             $log_message .= "Token AUD: " . ($payload_verified_manually['aud'] ?? 'N/A') . ". ";
             $log_message .= "Token EXP: " . ($payload_verified_manually['exp'] ?? 'N/A') . " (" . date('Y-m-d H:i:s', $payload_verified_manually['exp'] ?? 0) . "). ";
             $log_message .= "Token IAT: " . ($payload_verified_manually['iat'] ?? 'N/A') . " (" . date('Y-m-d H:i:s', $payload_verified_manually['iat'] ?? 0) . "). ";
             $log_message .= "Server Time: " . $current_time . " (" . date('Y-m-d H:i:s', $current_time) . "). ";
             $log_message .= "Expected AUD: " . $CLIENT_ID . ". ";
        } else {
             $log_message .= "Could not decode payload manually. ";
        }
        // --- END: ADDED TIME/AUDIENCE LOGGING ---

        // ✅ ADDED: Give extra time leeway for token verification (clock skew)
        \Firebase\JWT\JWT::$leeway = 120; // 2 minutes leeway for clock differences

        $client = new Google_Client(['client_id' => $CLIENT_ID]);
        $payload = $client->verifyIdToken($id_token);

        // ✅ ADDED: manual fallback check for audience
        if ($payload && isset($payload['aud']) && $payload['aud'] !== $CLIENT_ID) {
            http_response_code(401);
            echo json_encode(['status' => false, 'message' => 'Invalid audience: ' . $payload['aud']]);
            exit;
        }

        if ($payload) {
            $email = $payload['email'];
            $fullname = $payload['name'];
            
            // Updated Query: Now selecting 'status' as well
            $stmt = $conn->prepare("SELECT id, fullname, email, status FROM users WHERE email = ?");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $result = $stmt->get_result();
            $user = $result->fetch_assoc();
            $stmt->close();
            
            if ($user) { 
                // --- NEW BLOCKING LOGIC START ---
                if ($user['status'] === 'blocked') {
                    http_response_code(403); // Forbidden
                    echo json_encode(['status' => false, 'message' => 'Your account is blocked. You cannot sign in with Google.']);
                    exit();
                }
                // --- NEW BLOCKING LOGIC END ---

                $user_id = $user['id']; 
                $fullname = $user['fullname']; 
                $email = $user['email'];
            } else {
                list($username_base) = explode('@', $email); $username = $username_base; $counter = 1;
                while (true) { $stmt_check_user = $conn->prepare("SELECT id FROM users WHERE username = ?"); $stmt_check_user->bind_param("s", $username); $stmt_check_user->execute(); $stmt_check_user->store_result(); if ($stmt_check_user->num_rows == 0) { $stmt_check_user->close(); break; } $stmt_check_user->close(); $username = $username_base . $counter; $counter++; }
                $random_password = password_hash(bin2hex(random_bytes(16)), PASSWORD_DEFAULT); 
                
                // Note: New users are active by default, so we don't need to insert status unless db default is not 'active'
                $insertStmt = $conn->prepare("INSERT INTO users (fullname, email, password, username) VALUES (?, ?, ?, ?)"); 
                $insertStmt->bind_param("ssss", $fullname, $email, $random_password, $username); 
                $insertStmt->execute(); 
                $user_id = $insertStmt->insert_id; 
                $insertStmt->close();
            }
            $session_token = bin2hex(random_bytes(32)); 
            $updateStmt = $conn->prepare("UPDATE users SET session_token = ? WHERE id = ?"); 
            $updateStmt->bind_param("si", $session_token, $user_id); 
            $updateStmt->execute(); 
            $updateStmt->close();
            $response['status'] = true; $response['message'] = 'Login successful!'; $response['token'] = $session_token; $response['user'] = ['id' => $user_id, 'fullname' => $fullname, 'email' => $email]; echo json_encode($response);
        } else {
            http_response_code(401);
            echo json_encode(['status' => false, 'message' => 'Token verification returned false. (' . $log_message . 'Official check FAILED)']);
        }
    } catch (Exception $e) {
        http_response_code(401);
        echo json_encode(['status' => false, 'message' => 'Google verification failed (' . $log_message . 'Exception): ' . $e->getMessage()]);
    }

} else {
    http_response_code(405);
    echo json_encode(['status' => false, 'message' => 'Invalid request method.']);
}

$conn->close();
?>
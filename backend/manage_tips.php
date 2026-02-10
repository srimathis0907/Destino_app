<?php
header('Content-Type: application/json');
require_once 'config.php';

$data = json_decode(file_get_contents('php://input'), true);
$action = $data['action'] ?? null;

if (!$action) {
    echo json_encode(['status' => false, 'message' => 'Action is required.']);
    exit;
}

try {
    switch ($action) {
        case 'add':
            $title = $data['title'] ?? '';
            $content = $data['content'] ?? '';
            if (empty($title) || empty($content)) throw new Exception('Title and content are required.');
            
            $stmt = $conn->prepare("INSERT INTO travel_tips (tip_title, tip_content) VALUES (?, ?)");
            $stmt->bind_param("ss", $title, $content);
            $stmt->execute();
            echo json_encode(['status' => true, 'message' => 'Tip added successfully.']);
            break;

        case 'update':
            $id = $data['id'] ?? null;
            $title = $data['title'] ?? '';
            $content = $data['content'] ?? '';
            if (!$id || empty($title) || empty($content)) throw new Exception('ID, title, and content are required.');

            $stmt = $conn->prepare("UPDATE travel_tips SET tip_title = ?, tip_content = ? WHERE id = ?");
            $stmt->bind_param("ssi", $title, $content, $id);
            $stmt->execute();
            echo json_encode(['status' => true, 'message' => 'Tip updated successfully.']);
            break;

        case 'delete':
            $id = $data['id'] ?? null;
            if (!$id) throw new Exception('Tip ID is required.');

            $stmt = $conn->prepare("DELETE FROM travel_tips WHERE id = ?");
            $stmt->bind_param("i", $id);
            $stmt->execute();
            echo json_encode(['status' => true, 'message' => 'Tip deleted successfully.']);
            break;

        default:
            throw new Exception('Invalid action specified.');
    }
} catch (Exception $e) {
    echo json_encode(['status' => false, 'message' => $e->getMessage()]);
}

$conn->close();
?>
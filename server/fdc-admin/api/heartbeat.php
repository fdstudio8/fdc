<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);

if (!$data) {
    echo json_encode(['status' => 'error', 'message' => 'Invalid JSON']);
    exit;
}

$devicesFile = __DIR__ . '/../devices.json';
$devices = [];
if (file_exists($devicesFile)) {
    $devices = json_decode(file_get_contents($devicesFile), true) ?: [];
}

$deviceKey = ($data['manufacturer'] ?? 'Generic') . '_' . ($data['model'] ?? 'Device');
$data['last_seen'] = date('Y-m-d H:i:s');
$data['ip_address'] = $_SERVER['REMOTE_ADDR'] ?? '127.0.0.1';

$devices[$deviceKey] = $data;

file_put_contents($devicesFile, json_encode($devices, JSON_PRETTY_PRINT));

echo json_encode([
    'status' => 'ok',
    'timestamp' => time(),
    'message' => 'Heartbeat received successfully'
]);

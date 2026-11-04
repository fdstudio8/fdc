<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

$raw = file_get_contents('php://input');
$data = json_decode($raw, true);

if (!$data) {
    echo json_encode(['status' => 'error', 'message' => 'Invalid JSON']);
    exit;
}

$configFile = __DIR__ . '/../config.json';
$current = [];
if (file_exists($configFile)) {
    $current = json_decode(file_get_contents($configFile), true) ?: [];
}

// Increment config version automatically
$data['config_version'] = ($current['config_version'] ?? 1) + 1;

file_put_contents($configFile, json_encode($data, JSON_PRETTY_PRINT));

echo json_encode([
    'status' => 'ok',
    'config_version' => $data['config_version'],
    'message' => 'Policy configuration updated successfully'
]);

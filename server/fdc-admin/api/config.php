<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

$configFile = __DIR__ . '/../config.json';
if (file_exists($configFile)) {
    echo file_get_contents($configFile);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Configuration file not found'
    ]);
}

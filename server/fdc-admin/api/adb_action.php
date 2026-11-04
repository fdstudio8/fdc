<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

$adb = '/Users/fahadahmad/Library/Android/sdk/platform-tools/adb';
$action = $_POST['action'] ?? $_GET['action'] ?? '';
$package = trim($_POST['package'] ?? $_GET['package'] ?? '');

if ($action === 'status') {
    $devices = shell_exec("$adb devices 2>&1");
    echo json_encode([
        'status' => 'ok',
        'output' => trim($devices)
    ]);
    exit;
}

if ($action === 'hide' && !empty($package)) {
    $cmd = "$adb shell pm disable-user --user 0 " . escapeshellarg($package) . " 2>&1";
    $output = shell_exec($cmd);
    echo json_encode([
        'status' => 'ok',
        'action' => 'hide',
        'package' => $package,
        'output' => trim($output)
    ]);
    exit;
}

if ($action === 'unhide' && !empty($package)) {
    $cmd = "$adb shell pm enable --user 0 " . escapeshellarg($package) . " 2>&1";
    $output = shell_exec($cmd);
    echo json_encode([
        'status' => 'ok',
        'action' => 'unhide',
        'package' => $package,
        'output' => trim($output)
    ]);
    exit;
}

if ($action === 'set_device_owner') {
    // 1. Remove secondary user
    shell_exec("$adb shell pm remove-user 10 2>&1");
    // 2. Set Device Owner
    $cmd = "$adb shell dpm set-device-owner com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver 2>&1";
    $output = shell_exec($cmd);
    echo json_encode([
        'status' => 'ok',
        'action' => 'set_device_owner',
        'output' => trim($output)
    ]);
    exit;
}

echo json_encode(['status' => 'error', 'message' => 'Invalid action or missing package']);

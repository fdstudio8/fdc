<?php
$configFile = __DIR__ . '/config.json';
$devicesFile = __DIR__ . '/devices.json';

$config = file_exists($configFile) ? json_decode(file_get_contents($configFile), true) : [];
$devices = file_exists($devicesFile) ? json_decode(file_get_contents($devicesFile), true) : [];

$macIp = gethostbyname(gethostname());
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FDC Admin Console — MAMP PRO</title>
    <style>
        :root {
            --bg: #F2F2F7;
            --card-bg: rgba(255, 255, 255, 0.85);
            --border: rgba(0, 0, 0, 0.08);
            --primary: #007AFF;
            --mint: #34C759;
            --purple: #AF52DE;
            --orange: #FF9500;
            --red: #FF3B30;
            --text-primary: #1C1C1E;
            --text-secondary: #8E8E93;
            --mono-bg: #E5E5EA;
        }
        @media (prefers-color-scheme: dark) {
            :root {
                --bg: #000000;
                --card-bg: rgba(28, 28, 30, 0.85);
                --border: rgba(255, 255, 255, 0.12);
                --text-primary: #FFFFFF;
                --text-secondary: #98989D;
                --mono-bg: #2C2C2E;
            }
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
            background: var(--bg);
            color: var(--text-primary);
            padding: 32px 20px;
            display: flex;
            justify-content: center;
        }
        .container {
            width: 100%;
            max-width: 980px;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 8px;
        }
        .header h1 {
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.5px;
        }
        .badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 999px;
            font-size: 13px;
            font-weight: 600;
            background: rgba(52, 199, 89, 0.15);
            color: var(--mint);
        }
        .badge-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--mint);
        }
        .card {
            background: var(--card-bg);
            backdrop-filter: blur(20px);
            border: 1px solid var(--border);
            border-radius: 20px;
            padding: 24px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
        }
        .card-title {
            font-size: 12px;
            text-transform: uppercase;
            font-weight: 700;
            letter-spacing: 0.5px;
            color: var(--text-secondary);
            margin-bottom: 14px;
        }
        .grid-2 {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }
        @media (max-width: 768px) {
            .grid-2 { grid-template-columns: 1fr; }
        }
        .endpoint-box {
            background: var(--mono-bg);
            padding: 12px 16px;
            border-radius: 12px;
            font-family: ui-monospace, Menlo, Monaco, monospace;
            font-size: 13px;
            word-break: break-all;
            margin-top: 6px;
        }
        .row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 0;
            border-bottom: 1px solid var(--border);
        }
        .row:last-child { border-bottom: none; }
        .row-info h4 { font-size: 16px; font-weight: 600; }
        .row-info p { font-size: 13px; color: var(--text-secondary); margin-top: 2px; }
        .switch {
            position: relative;
            display: inline-block;
            width: 50px;
            height: 30px;
        }
        .switch input { opacity: 0; width: 0; height: 0; }
        .slider {
            position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;
            background-color: var(--mono-bg);
            transition: .3s cubic-bezier(0.16, 1, 0.3, 1);
            border-radius: 30px;
        }
        .slider:before {
            position: absolute; content: ""; height: 26px; width: 26px; left: 2px; bottom: 2px;
            background-color: white;
            transition: .3s cubic-bezier(0.16, 1, 0.3, 1);
            border-radius: 50%;
            box-shadow: 0 2px 4px rgba(0,0,0,0.2);
        }
        input:checked + .slider { background-color: var(--mint); }
        input:checked + .slider:before { transform: translateX(20px); }
        .btn {
            background: var(--primary);
            color: white;
            border: none;
            padding: 12px 20px;
            border-radius: 12px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.1s ease;
        }
        .btn:active { transform: scale(0.97); }
        .btn-purple { background: var(--purple); }
        textarea {
            width: 100%;
            height: 110px;
            padding: 12px;
            border-radius: 12px;
            border: 1px solid var(--border);
            background: var(--mono-bg);
            color: var(--text-primary);
            font-family: ui-monospace, Menlo, monospace;
            font-size: 13px;
            margin-top: 8px;
            resize: vertical;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
            margin-top: 8px;
        }
        th, td {
            text-align: left;
            padding: 10px 8px;
            border-bottom: 1px solid var(--border);
        }
        th { color: var(--text-secondary); font-weight: 600; font-size: 12px; text-transform: uppercase; }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div>
                <h1>FDC Admin Console</h1>
                <p style="color: var(--text-secondary); font-size: 14px;">MAMP PRO Universal Android Device Controller</p>
            </div>
            <div class="badge">
                <div class="badge-dot"></div>
                MAMP PRO Server Online
            </div>
        </div>

        <!-- Server & GitHub Bridge Card -->
        <div class="card">
            <div class="card-title">SERVER & GITHUB DYNAMIC URL ARCHITECTURE</div>
            <div class="grid-2">
                <div>
                    <p style="font-weight: 600; font-size: 14px;">Local MAMP PRO Server Endpoint:</p>
                    <div class="endpoint-box">http://192.168.31.105/fdc-admin</div>
                    <p style="font-size: 12px; color: var(--text-secondary); margin-top: 6px;">
                        Accessible by any enrolled phone on your local Wi-Fi.
                    </p>
                </div>
                <div>
                    <p style="font-weight: 600; font-size: 14px;">GitHub JSON Dynamic Pointer:</p>
                    <div class="endpoint-box">https://raw.githubusercontent.com/.../fdc-config.json</div>
                    <p style="font-size: 12px; color: var(--text-secondary); margin-top: 6px;">
                        Phones fetch this GitHub URL to locate your MAMP server & sync permanent policies.
                    </p>
                </div>
            </div>
        </div>

        <!-- Policy Configuration Card -->
        <div class="card">
            <div class="card-title">PERMANENT POLICY CONTROLLER (ACTIVE FOREVER)</div>
            <form id="policyForm">
                <div class="row">
                    <div class="row-info">
                        <h4>Permanent Strict Lock (Always-On)</h4>
                        <p>Locks policies permanently 24/7 across device reboots and battery drain</p>
                    </div>
                    <label class="switch">
                        <input type="checkbox" id="permanent_lockdown" <?= ($config['permanent_lockdown'] ?? false) ? 'checked' : '' ?>>
                        <span class="slider"></span>
                    </label>
                </div>

                <div class="row">
                    <div class="row-info">
                        <h4>Anti-Tamper Shield (Settings App Info Lock)</h4>
                        <p>Blocks Force Stop, Clear Data & Storage, and uninstallation in Android Settings</p>
                    </div>
                    <label class="switch">
                        <input type="checkbox" id="anti_tamper_shield" <?= ($config['anti_tamper_shield'] ?? true) ? 'checked' : '' ?>>
                        <span class="slider"></span>
                    </label>
                </div>

                <div class="row">
                    <div class="row-info">
                        <h4>Block Factory Reset</h4>
                        <p>Disables settings-based factory reset permanently</p>
                    </div>
                    <label class="switch">
                        <input type="checkbox" id="disallow_factory_reset" <?= ($config['disallow_factory_reset'] ?? true) ? 'checked' : '' ?>>
                        <span class="slider"></span>
                    </label>
                </div>

                <div class="row">
                    <div class="row-info">
                        <h4>Block Safe Boot Mode</h4>
                        <p>Prevents rebooting into Safe Mode to bypass controls</p>
                    </div>
                    <label class="switch">
                        <input type="checkbox" id="disallow_safe_boot" <?= ($config['disallow_safe_boot'] ?? true) ? 'checked' : '' ?>>
                        <span class="slider"></span>
                    </label>
                </div>

                <div class="row">
                    <div class="row-info">
                        <h4>Permanent Kiosk Mode</h4>
                        <p>Locks device screen permanently to FDC</p>
                    </div>
                    <label class="switch">
                        <input type="checkbox" id="kiosk_mode" <?= ($config['kiosk_mode'] ?? false) ? 'checked' : '' ?>>
                        <span class="slider"></span>
                    </label>
                </div>

                <div style="margin-top: 18px;">
                    <label style="font-weight: 600; font-size: 14px;">Permanently Suspended Packages (one package name per line):</label>
                    <textarea id="suspended_packages"><?= htmlspecialchars(implode("\n", $config['suspended_packages'] ?? [])) ?></textarea>
                </div>

                <div style="margin-top: 18px; display: flex; gap: 12px; justify-content: flex-end;">
                    <button type="button" class="btn btn-purple" onclick="copyGitHubJson()">📋 Copy JSON for GitHub</button>
                    <button type="submit" class="btn">💾 Save & Deploy to Devices</button>
                </div>
            </form>
        </div>

        <!-- Connected Device Fleet Card -->
        <div class="card">
            <div class="card-title">MANAGED DEVICE FLEET & LIVE HEARTBEAT</div>
            <?php if (empty($devices)): ?>
                <p style="color: var(--text-secondary); font-size: 14px; padding: 12px 0;">
                    No devices have checked in yet. Open FDC on your Nothing Phone or run a sync to register live heartbeat.
                </p>
            <?php else: ?>
                <table>
                    <thead>
                        <tr>
                            <th>Device</th>
                            <th>OS Version</th>
                            <th>IP Address</th>
                            <th>Permanent Lock</th>
                            <th>Last Heartbeat</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($devices as $key => $dev): ?>
                            <tr>
                                <td><strong><?= htmlspecialchars(($dev['manufacturer'] ?? '') . ' ' . ($dev['model'] ?? '')) ?></strong></td>
                                <td>Android <?= htmlspecialchars($dev['android_version'] ?? '') ?> (API <?= htmlspecialchars($dev['api_level'] ?? '') ?>)</td>
                                <td><?= htmlspecialchars($dev['ip_address'] ?? '') ?></td>
                                <td>
                                    <span style="color: <?= ($dev['permanent_lockdown'] ?? false) ? 'var(--mint)' : 'var(--orange)' ?>; font-weight: 600;">
                                        <?= ($dev['permanent_lockdown'] ?? false) ? 'ACTIVE' : 'STANDBY' ?>
                                    </span>
                                </td>
                                <td><?= htmlspecialchars($dev['last_seen'] ?? '') ?></td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            <?php endif; ?>
        </div>
    </div>

    <script>
        document.getElementById('policyForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const suspendedText = document.getElementById('suspended_packages').value;
            const suspendedArr = suspendedText.split('\n').map(s => s.trim()).filter(s => s.length > 0);

            const payload = {
                admin_server_url: "http://192.168.31.105/fdc-admin",
                permanent_lockdown: document.getElementById('permanent_lockdown').checked,
                anti_tamper_shield: document.getElementById('anti_tamper_shield').checked,
                disallow_factory_reset: document.getElementById('disallow_factory_reset').checked,
                disallow_safe_boot: document.getElementById('disallow_safe_boot').checked,
                disallow_apps_control: true,
                disallow_config_date_time: true,
                kiosk_mode: document.getElementById('kiosk_mode').checked,
                suspended_packages: suspendedArr,
                hidden_packages: []
            };

            const res = await fetch('api/update_policy.php', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                alert('Policy updated successfully in MAMP PRO! Config Version bumped.');
                location.reload();
            } else {
                alert('Error updating policy.');
            }
        });

        function copyGitHubJson() {
            fetch('api/config.php')
                .then(r => r.text())
                .then(text => {
                    navigator.clipboard.writeText(text);
                    alert('GitHub configuration JSON copied to clipboard! Paste it into your GitHub repository or Gist.');
                });
        }
    </script>
</body>
</html>

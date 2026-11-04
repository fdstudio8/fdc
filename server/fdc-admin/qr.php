<?php
$qrPayloadFile = __DIR__ . '/qr-payload.json';
$defaultPayload = file_exists($qrPayloadFile) ? file_get_contents($qrPayloadFile) : '{}';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Android Enterprise Full Auto-Setup QR Code</title>
    <!-- Lightweight QR Generator -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/qrcodejs/1.0.0/qrcode.min.js"></script>
    <style>
        :root {
            --bg: #F2F2F7;
            --card: rgba(255, 255, 255, 0.9);
            --text-primary: #1C1C1E;
            --text-secondary: #8E8E93;
            --primary: #007AFF;
            --mint: #34C759;
            --purple: #AF52DE;
            --border: rgba(0, 0, 0, 0.1);
            --mono: #E5E5EA;
        }
        @media (prefers-color-scheme: dark) {
            :root {
                --bg: #000000;
                --card: rgba(28, 28, 30, 0.9);
                --text-primary: #FFFFFF;
                --text-secondary: #98989D;
                --border: rgba(255, 255, 255, 0.15);
                --mono: #2C2C2E;
            }
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
            background: var(--bg);
            color: var(--text-primary);
            padding: 30px 16px;
            display: flex;
            justify-content: center;
        }
        .container {
            width: 100%;
            max-width: 860px;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        .header {
            text-align: center;
            margin-bottom: 8px;
        }
        .header h1 {
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.5px;
        }
        .header p {
            color: var(--text-secondary);
            font-size: 15px;
            margin-top: 4px;
        }
        .grid {
            display: grid;
            grid-template-columns: 360px 1fr;
            gap: 20px;
        }
        @media (max-width: 800px) {
            .grid { grid-template-columns: 1fr; }
        }
        .card {
            background: var(--card);
            backdrop-filter: blur(20px);
            border: 1px solid var(--border);
            border-radius: 24px;
            padding: 24px;
            box-shadow: 0 4px 24px rgba(0,0,0,0.06);
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        #qrcode {
            background: #FFFFFF;
            padding: 20px;
            border-radius: 18px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.1);
            margin: 16px 0;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .steps {
            background: var(--card);
            backdrop-filter: blur(20px);
            border: 1px solid var(--border);
            border-radius: 24px;
            padding: 24px;
        }
        .step-item {
            display: flex;
            gap: 14px;
            margin-bottom: 18px;
        }
        .step-num {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: var(--primary);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 14px;
            flex-shrink: 0;
        }
        .step-text h4 { font-size: 16px; font-weight: 600; }
        .step-text p { font-size: 13px; color: var(--text-secondary); margin-top: 2px; line-height: 1.4; }
        .badge-green {
            background: rgba(52, 199, 89, 0.15);
            color: var(--mint);
            padding: 6px 12px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 600;
            margin-top: 8px;
        }
        textarea {
            width: 100%;
            height: 180px;
            background: var(--mono);
            border: 1px solid var(--border);
            color: var(--text-primary);
            font-family: ui-monospace, Menlo, monospace;
            font-size: 12px;
            padding: 12px;
            border-radius: 12px;
            resize: vertical;
            margin-top: 10px;
        }
        .btn {
            background: var(--primary);
            color: white;
            border: none;
            padding: 10px 18px;
            border-radius: 12px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 10px;
            width: 100%;
        }
        input[type="text"] {
            width: 100%;
            padding: 10px 14px;
            border-radius: 10px;
            border: 1px solid var(--border);
            background: var(--mono);
            color: var(--text-primary);
            font-size: 13px;
            margin-top: 4px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Zero-Touch Auto-Provisioning QR Code</h1>
            <p>Scan once on any factory-reset Android phone — 100% automated setup with Zero manual steps</p>
        </div>

        <div class="grid">
            <!-- QR Code Card -->
            <div class="card">
                <span class="badge-green">✨ FULL AUTO-SETUP ENABLED</span>
                <div id="qrcode"></div>
                <p style="font-size: 12px; color: var(--text-secondary); text-align: center; margin-top: 4px;">
                    Scan this code after tapping the Welcome screen 6 times.
                </p>
                <button class="btn" onclick="updateQrFromText()">🔄 Regenerate QR</button>
            </div>

            <!-- Steps & Config Card -->
            <div class="steps">
                <h3 style="margin-bottom: 16px; font-size: 18px;">How to Enroll Any Android Phone:</h3>

                <div class="step-item">
                    <div class="step-num">1</div>
                    <div class="step-text">
                        <h4>Tap Welcome Screen 6 Times</h4>
                        <p>Power on a factory-reset phone (Samsung, Xiaomi, Pixel, Nothing, etc.). On the initial "Welcome" screen, tap 6 times rapidly in the exact same spot.</p>
                    </div>
                </div>

                <div class="step-item">
                    <div class="step-num">2</div>
                    <div class="step-text">
                        <h4>Scan the QR Code</h4>
                        <p>The camera will launch automatically. Point the phone camera at the QR code on your Mac screen.</p>
                    </div>
                </div>

                <div class="step-item">
                    <div class="step-num">3</div>
                    <div class="step-text">
                        <h4>Put the Phone Down — Everything is Automatic!</h4>
                        <p>The phone will download the APK from GitHub/Server, verify the SHA-256 signature, provision Device Owner, lock uninstallation, block factory reset, and launch FDC in permanent mode!</p>
                    </div>
                </div>

                <hr style="border: 0; border-top: 1px solid var(--border); margin: 16px 0;">

                <h4 style="font-size: 14px; font-weight: 600;">Active Provisioning Payload (JSON):</h4>
                <textarea id="payloadInput"><?= htmlspecialchars($defaultPayload) ?></textarea>
            </div>
        </div>
    </div>

    <script>
        let qrcode = null;

        function renderQr(text) {
            const container = document.getElementById('qrcode');
            container.innerHTML = '';
            qrcode = new QRCode(container, {
                text: text,
                width: 260,
                height: 260,
                colorDark: "#000000",
                colorLight: "#ffffff",
                correctLevel: QRCode.CorrectLevel.M
            });
        }

        function updateQrFromText() {
            const raw = document.getElementById('payloadInput').value;
            renderQr(raw);
        }

        // Initial render
        window.onload = function() {
            updateQrFromText();
        };
    </script>
</body>
</html>

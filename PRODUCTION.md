# Fahad Device Controller (FDC) — Production Deployment Manual
**End-to-End Enterprise Guide: From Local Build to Global Fleet Management**  
*Document Version: 2.0.0 | Target: Production Enterprise Fleet*

---

## 🏗️ 1. Production Architecture Overview

In a production environment, FDC operates via a decentralized 3-tier architecture inspired by **MDMesh / Headwind MDM**:

```
+─────────────────────────────────────────────────────────────────────────────+
|                         TIER 1: GITHUB CONFIG POINTER                       |
|           URL: https://raw.githubusercontent.com/.../config.json            |
|  - 99.99% Global Uptime (GitHub CDN)                                        |
|  - Decouples devices from static IP addresses                               |
|  - Controls the active Admin Server URL, permanent policies & blacklist     |
+─────────────────────────────────────────────────────────────────────────────+
                                       │
                                       ▼ (Periodic Fetch / On-Demand Sync)
+─────────────────────────────────────────────────────────────────────────────+
|                         TIER 2: MAMP PRO / CLOUD ADMIN                      |
|           URL: https://admin.yourdomain.com OR Local MAMP PRO               |
|  - Centralized Web Dashboard (Apple HIG Design)                             |
|  - Live Device Fleet Monitoring & Heartbeat Registry                        |
|  - 1-Click Policy Switchboard & APK Host                                    |
+─────────────────────────────────────────────────────────────────────────────+
                                       │
                                       ▼ (Provisioning via QR / Zero-Touch)
+─────────────────────────────────────────────────────────────────────────────+
|                         TIER 3: MANAGED ANDROID FLEET                       |
|           Nothing • Samsung • Xiaomi • OPPO • Vivo • Pixel • OnePlus        |
|  - Fully Managed Device Owner (AOSP DPC)                                    |
|  - Permanent 24/7 Enforcement (Survives Reboots & Battery Drain)            |
|  - Anti-Tamper Shield (Settings App Info & Clear Data Permanently Disabled)|
|  - Emergency Master Key Override Guarantee                                  |
+─────────────────────────────────────────────────────────────────────────────+
```

---

## 🔑 2. Phase 1: Build & Sign Production Release APK

Production Android Enterprise devices require an APK signed with an official cryptographic keystore and V2/V3 signatures.

### Automated Production Build:
Run the automated build script:
```bash
./build-production-release.sh
```

This script automatically:
1. Generates the 2048-bit RSA production keystore (`keystore/fdc-release.jks`).
2. Builds the optimized release APK using Gradle.
3. Signs the APK with Android V1, V2, and V3 signatures using `apksigner`.
4. Computes the SHA-256 fingerprint (Hex and URL-safe Base64).
5. Generates the exact Android Enterprise QR provisioning payload (`release/android-enterprise-qr.json`).
6. Copies the release APK to your MAMP PRO web root (`/Applications/MAMP/htdocs/fdc-admin/fdc-production.apk`).

---

## ☁️ 3. Phase 2: Host the GitHub Configuration JSON

Instead of hardcoding a server IP inside the APK, your fleet reads from a public **GitHub Raw JSON**.

### Steps to Host:
1. Create a public repository on GitHub (e.g. `github.com/your-username/fdc-fleet-config`).
2. Create a file named `config.json` with this content:
   ```json
   {
     "admin_server_url": "https://admin.yourdomain.com/fdc-admin",
     "config_version": 1,
     "permanent_lockdown": true,
     "anti_tamper_shield": true,
     "kiosk_mode": false,
     "disallow_factory_reset": true,
     "disallow_safe_boot": true,
     "disallow_apps_control": true,
     "disallow_config_date_time": true,
     "suspended_packages": [
       "com.instagram.android",
       "com.zhiliaoapp.musically",
       "com.snapchat.android"
     ],
     "hidden_packages": []
   }
   ```
3. Commit and push the file.
4. Click on **"Raw"** to copy the public Raw URL:
   `https://raw.githubusercontent.com/your-username/fdc-fleet-config/main/config.json`
5. Whenever you edit this JSON on GitHub, all enrolled phones will automatically sync the updated policies!

---

## 🌐 4. Phase 3: Production Server Hosting for MAMP PRO

To allow devices anywhere in the world to report heartbeats and receive updates:

### Option A: Cloudflare Tunnel (Recommended — Free & No Port Forwarding)
You can expose your local Mac MAMP PRO to a secure HTTPS domain using Cloudflare Tunnels:
```bash
# 1. Install cloudflared
brew install cloudflared

# 2. Run tunnel pointing to local MAMP PRO
cloudflared tunnel --url http://localhost:80
```
Cloudflare will provide a public HTTPS URL (e.g. `https://random-subdomain.trycloudflare.com`). Put that URL in your `config.json` under `"admin_server_url"`.

### Option B: Cloud VPS / Shared Hosting (DigitalOcean, AWS, cPanel)
Upload the contents of `/Users/fahadahmad/Desktop/Hidden/server/fdc-admin` to your web server:
- Supports standard PHP 8.1+ with Apache / Nginx.
- Set permissions so `config.json` and `devices.json` are writable (`chmod 775`).

---

## 📲 5. Phase 4: Production Enrollment on Real Devices

To enforce **Device Owner** status permanently without bypass, devices must be enrolled using official Android Enterprise provisioning:

### Method 1: The 6-Tap QR Code Method (Universal for all Android 8–15 phones)
*Use this for newly unboxed phones or after factory resetting a device.*

1. Power on the phone to the initial **"Hi there" / "Welcome"** screen.
2. **Tap the screen 6 times rapidly in the same spot.**
3. A QR code scanner will open.
4. Connect the phone to Wi-Fi.
5. Scan your Android Enterprise QR code (generated from `release/android-enterprise-qr.json`).
6. The phone will automatically:
   - Download the production APK from your server or GitHub release.
   - Verify the SHA-256 checksum.
   - Provision FDC as the single Device Owner **before any user account can be created**.
   - Lock uninstallation, block factory reset, and enforce your permanent restrictions immediately!

### Method 2: Staging Enrollment via ADB (For existing devices without wipe)
1. Go to **Settings -> Accounts** and remove all Google/OEM accounts.
2. Connect phone via USB with USB Debugging enabled.
3. Install the production APK:
   ```bash
   adb install -r release/fdc-production-v2.0.0.apk
   ```
4. Set FDC as Device Owner:
   ```bash
   adb shell dpm set-device-owner com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver
   ```
5. You can now re-add accounts; FDC remains permanently locked as Device Owner.

### Method 3: Samsung Knox Mobile Enrollment (KME)
For Samsung fleets:
1. Log in to [Samsung Knox Portal](https://samsungknox.com).
2. Register your devices via IMEI / Serial numbers.
3. Create a profile pointing to `com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver`.
4. As soon as a user powers on a new Samsung phone and connects to Wi-Fi, Knox silently downloads and locks FDC as the Device Owner automatically.

---

## 🛡️ 6. Production Security & Tamper Resistance Invariants

### 1. The Emergency Master Key
- In production, never share the Master Key with ordinary users.
- Default Key: `FDC-MASTER-9988-7766`
- Change this key in production via the Admin Auth dialog to a unique organization key.
- Keep the Master Key stored securely in an enterprise password manager (1Password, Bitwarden).

### 2. Reboot Resilience Guarantee
- FDC registers `FdcBootReceiver` with `RECEIVE_BOOT_COMPLETED`.
- Even if a user drains the battery, turns the phone off, or reboots, Android Enterprise re-applies all restrictions before the user can even unlock the lockscreen.

### 3. Anti-Bypass Lockdown
- Android Settings: "Force Stop" and "Clear Storage" are grayed out via `DISALLOW_APPS_CONTROL`.
- Time tampering: Blocked via `DISALLOW_CONFIG_DATE_TIME`.
- Uninstallation: Blocked at the OS kernel level via `setUninstallBlocked`.

---

## 🔄 7. Deprovisioning Protocol (Retiring a Device)

When a device is decommissioned or given to an employee leaving the organization:
1. Open FDC on the device.
2. Tap the brand crest **3 rapid times + 1.5s long press** (or tap Admin icon).
3. Authenticate with the Admin PIN or Emergency Master Key.
4. Tap **"Deprovision Device"**.
5. FDC will cleanly unsuspend all apps, unhide apps, clear all restrictions, and drop Device Owner status, returning the phone to a normal consumer device.

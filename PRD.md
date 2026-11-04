# Fahad Device Controller (FDC) — Product Requirements Document (PRD)
**Universal Android Enterprise Device Policy Controller & Adaptive Governor**  
*Document Version: 2.0.0 | Architecture: Universal Android Enterprise (AOSP DPC)*  
*Design Philosophy: Apple Human Interface Guidelines (Fluid Springs, Depth & Translucency, Direct Manipulation) on Modern Android (Jetpack Compose)*

---

## 1. Executive Summary

Fahad Device Controller (FDC) is an advanced, high-reliability Android Device Policy Controller (DPC) and productivity governance platform. Historically dependent on proprietary Samsung Knox frameworks, this Universal Compatibility Update (v2.0) decouples core device supervision from OEM-specific locks. FDC adopts the **Android Enterprise Device Owner (DO)** specification as its primary bedrock while treating vendor-specific tools (such as Samsung Knox, Xiaomi HyperOS/MIUI optimizations, ColorOS, OriginOS) as **optional, dynamically loaded hardware extensions**.

The application delivers an uncompromising **Apple Design System** user experience on Android: zero input latency, physical spring dynamics, frosted glass materials (`backdrop-filter`/blur surfaces), tactile haptic feedback, and crystal-clear transparency regarding what policies are actively enforced versus hardware-restricted.

---

## 2. Target Ecosystem & OEM Coverage Matrix

FDC is engineered to operate reliably across Android 8.0 (API 26) through Android 15 (API 35+), providing unified single-APK distribution for:

| Manufacturer / Brand | OS Environment | Primary Management Engine | Optional Extension Available |
| :--- | :--- | :--- | :--- |
| **Samsung** | One UI (Android 9–15) | Android Enterprise DO | Samsung Knox SDK (Conditional Reflection) |
| **Google Pixel** | Stock Android (Android 8–15) | Android Enterprise DO (Reference) | Pure AOSP System Controls |
| **Xiaomi / Redmi / POCO** | HyperOS / MIUI (Android 9–15) | Android Enterprise DO | AutoStart & Battery Optimization Bridge |
| **OPPO / OnePlus / Realme** | ColorOS / OxygenOS / Realme UI | Android Enterprise DO | Background Execution & Deep Freeze Bridge |
| **Vivo / iQOO** | Funtouch OS / OriginOS | Android Enterprise DO | High Background Power Bridge |
| **Motorola** | MyUX / Hello UI | Android Enterprise DO | Clean Moto AOSP Extensions |
| **Nothing** | Nothing OS (Android 13–15) | Android Enterprise DO | Clean Glyph / AOSP Bridge |
| **Other Android OEMs** | Standard AOSP Derivatives | Android Enterprise DO | Standard Telephony & Power Bridge |

---

## 3. The Three Operating Modes

FDC automatically detects its privileges upon startup and initializes into one of three distinct operational tiers:

```
+-------------------------------------------------------------------------------+
|                       FAHAD DEVICE CONTROLLER (FDC)                           |
+-------------------------------------------------------------------------------+
                                        |
                            Startup Hardware Probe
                                        |
                 +----------------------+----------------------+
                 |                                             |
       Device Owner Enrolled?                         No Device Owner
                 |                                             |
        +--------+--------+                           [ MODE 1: STANDARD ]
        |                 |                           - Normal User APK
   OEM Extension?         |                           - Pomodoro Study Timer
        |                 |                           - Protected Notes & Plan
  [ MODE 3: OEM EXT ]  [ MODE 2: DO MODE ]             - App Inventory Scan
  - Android DO Core    - Android DO Core              - Unsupported Features
  - Knox / HyperOS /   - App Hiding & Suspension        Explicitly Labeled
    ColorOS Bridges    - Uninstall & Reset Block
                       - Kiosk Lock Task Mode
```

### 3.1 Mode 1: Standard Mode (Unmanaged / Normal APK)
*Activated when FDC is sideloaded or installed from an app store without Device Owner provisioning.*
- **Available Capabilities:**
  - Secure Administrator PIN Authentication & Session Governance.
  - Interactive Study Timer with customizable focus intervals.
  - Encrypted Local Notes, Task Schedules, and Study Logs.
  - Device Application Inventory inspection (where allowed by `QUERY_ALL_PACKAGES`).
  - Supported Launcher and Home Screen shortcuts.
  - Parental guidance recommendations and non-intrusive focus prompts.
- **Enforcement Honesty Invariant:**
  - System-level toggles (e.g., Application Suspension, Factory Reset Protection, Stealth Hiding) are **visually locked** and badged as *"Requires Device Owner Enrollment"*.
  - The UI provides an interactive **Enrollment Guide** explaining the exact steps required.

### 3.2 Mode 2: Fully Managed Device Owner Mode (Android Enterprise)
*Activated when FDC is provisioned as the device's single Device Owner (`dpm set-device-owner` or Zero-Touch / QR / NFC provisioning).*
- **Universal DPC Capabilities:**
  - **Application Hiding:** Dynamic package invisibility via `setApplicationHidden(admin, pkg, true/false)`.
  - **Application Suspension:** Pausing execution and graying out apps via `setPackagesSuspended(admin, pkgs, true/false)`.
  - **Uninstall Prevention:** Granular package uninstall locks via `setUninstallBlocked(admin, pkg, true)`.
  - **Factory Reset Guard:** Blocking Settings-based wipes via `DISALLOW_FACTORY_RESET`.
  - **Lock Task (Kiosk) Mode:** Distraction-free study environments via `setLockTaskPackages()` and `startLockTask()`.
  - **System Restrictions:** Granular user restrictions (`DISALLOW_SAFE_BOOT`, `DISALLOW_ADD_USER`, `DISALLOW_MODIFY_ACCOUNTS`, `DISALLOW_INSTALL_UNKNOWN_SOURCES`).
  - **Real-Time Verification:** Every policy query reads directly from `DevicePolicyManager` to verify actual active status on the target hardware before showing an "Enforced" badge.

### 3.3 Mode 3: Manufacturer Extension Mode (Adaptive OEM Enhancements)
*Activated when Device Owner is verified AND supported vendor-specific management APIs are detected.*
- **Samsung Knox Integration:**
  - Evaluated **strictly via dynamic reflection**; FDC compiles cleanly without hard links to Samsung Knox JARs.
  - Checks `com.samsung.android.knox.EnterpriseDeviceManager`.
  - Enables hardware-level payload blocking, extended firmware lockups, and enhanced Knox Attestation when available.
  - **Non-Samsung Rule:** If device is Xiaomi, Pixel, OnePlus, etc., Knox modules are completely dormant and invisible, ensuring zero crash risk.
- **HyperOS / ColorOS / Funtouch Bridges:**
  - Guided deep-linking into OEM hidden settings (Autostart Whitelists, High Background Power Consumption exemptions, Aggressive RAM killer exemptions) so background services are never killed.

---

## 4. Startup Compatibility Detection Engine

Every launch executes an instant, asynchronous 7-step hardware probe:
1. **OS Architecture Probe:** Android Version, API Level, Security Patch Date, Build Fingerprint.
2. **OEM & Brand Probe:** Manufacturer, Brand, Model, Hardware Board, Custom ROM ID (MIUI/HyperOS, ColorOS, One UI).
3. **Privilege & Identity Probe:** `isDeviceOwnerApp()`, `isProfileOwnerApp()`, `isAdminActive()`.
4. **Management API Audit:** Test availability of `setPackagesSuspended`, `setApplicationHidden`, `setLockTaskPackages`, `addUserRestriction`.
5. **Vendor Extension Audit:** Probe for Samsung Knox, Xiaomi Security Center, ColorOS Power Genie, Vivo iManager.
6. **Live Compatibility Report:** Compile findings into a structured, exportable report.
7. **Feature Gating:** Dynamically toggle UI controls to eliminate false claims.

---

## 5. Official Provisioning & Enrollment Workflows

FDC strictly forbids misleading "fake elevation" or silent root exploits. It provides transparent, official Android Enterprise enrollment paths:

### 5.1 ADB Command Provisioning (Developer / IT Admin)
- Prerequisites: Device unmanaged, no Google or personal accounts configured, or provisioned immediately after unboxing.
- Command:
  ```bash
  adb shell dpm set-device-owner com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver
  ```
- Guided troubleshooting for `java.lang.IllegalStateException: Not allowed to set the device owner because there are already some accounts on the device`.

### 5.2 QR Code Provisioning (Factory Reset / Out-of-the-Box)
- Tap the welcome screen 6 times on a newly factory-reset phone.
- Scan generated FDC QR code containing standard Android Enterprise provisioning bundle:
  ```json
  {
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://raw.githubusercontent.com/fahad/fdc/release/fdc.apk",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": "SHA-256-FDC-HASH",
    "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": false
  }
  ```

---

## 6. Protection Behaviour & Security Boundaries

### 6.1 Administrator Authentication & Emergency Master Key
- High-security PIN protection using salted PBKDF2 / SHA-256 hashing stored in Android EncryptedSharedPreferences.
- **Cryptographic Emergency Master Key (`FDC-MASTER-XXXX-XXXX`):** A deterministic, salted recovery key that bypasses any active lockout, resets forgotten administrator PINs, and guarantees the owner can never be locked out of their device.
- Exponential backoff rate limiting: 5 failed attempts trigger a 60-second lockout; 10 failed attempts trigger a 15-minute lockout.
- Constant-time comparison (`MessageDigest.isEqual`) to eliminate timing side-channel attacks.
- Active admin session token valid for 5 minutes of inactivity.

### 6.2 Anti-Bypass & Tamper Resistance Invariant
To ensure no ordinary user can circumvent FDC without the administrator password or Master Key, Device Owner mode systematically locks down evasion vectors:
- **Uninstall Prevention:** `setUninstallBlocked(admin, fdcPackage, true)` blocks uninstall from launcher, app drawer, and system settings.
- **App Control Lock:** `DISALLOW_APPS_CONTROL` greys out "Force Stop", "Clear Storage", and "Clear Cache" in Android Settings.
- **Clock Manipulation Lock:** `DISALLOW_CONFIG_DATE_TIME` prevents modifying system date/time to tamper with study timers or bypass lockout countdowns.
- **Factory Reset Lock:** `DISALLOW_FACTORY_RESET` disables Settings-based factory reset.
- **Safe Boot Lock:** `DISALLOW_SAFE_BOOT` blocks restarting into Safe Mode.
- **Profile Evading Lock:** `DISALLOW_ADD_USER` prevents switching to guest mode or secondary user accounts.

### 6.2 Protection Boundaries (Honest Security Disclosure)
- **What FDC Enforces:**
  - Prevents ordinary users from uninstalling FDC via Settings or Launcher.
  - Prevents Settings-based factory resets.
  - Suspends and hides prohibited applications.
  - Restricts access to Safe Mode (via `DISALLOW_SAFE_BOOT`).
- **What No Android DPC Can Guarantee (Exposed Honestly):**
  - Physical EDL / Fastboot ROM reflashing via PC.
  - Hardware recovery key combinations (unless locked down via OEM Knox Customization on supported Samsung hardware).
  - Authorized OEM warranty repair center servicing.

### 6.3 Authorized Deprovisioning Flow
- A dedicated, audited deprovisioning workflow requiring Admin PIN verification.
- Safely clears all user restrictions, unhides and unsuspends all managed packages, removes Lock Task Mode, and calls `clearDeviceOwnerApp()` / `removeActiveAdmin()`.

---

## 7. Multi-Channel Admin Access Interface

To prevent lockouts and accommodate varying OEM telephony architectures:
1. **Primary Interface:** In-App PIN Prompt with biometric/keypad.
2. **Secret Tap Sequence:** 3 consecutive rapid taps followed by a 1.5-second long press on the FDC Top App Bar brand crest triggers the Admin Unlock Modal.
3. **Quick Settings Tile:** `FdcAdminTileService` enables immediate admin authentication from the Android notification shade.
4. **Telephony Secret Code (Optional Fallback):** Listens for `android.provider.Telephony.SECRET_CODE` dialer string (e.g., `*#*#1234#*#*`). Dynamically enabled only on devices with telephony hardware and permission clearance; never relied upon as the sole access point.

---

## 8. Apple Design System on Android

The UI is built using Jetpack Compose conforming to Apple's design philosophy:
- **Motion & Dynamics:** Critically damped spring physics (`dampingRatio = 1.0`, `stiffness = 300f`) for regular UI movement, and momentum-aware bounce (`dampingRatio = 0.8`) on flicks.
- **Direct Manipulation:** Instant touch-down press feedback (`scale(0.97f)` on press, not on release).
- **Materials:** Frosted translucent cards (`alpha 0.7f` with blur simulation and `0.5dp` glass stroke highlight).
- **Typography:** Refined hierarchy, optical leading, tight headline tracking (`-0.02em`), and clean body copy.
- **Clarity & Deference:** High contrast status pills, explicit tooltips explaining API constraints, and zero visual clutter.

---

## 9. MAMP PRO Admin Console & GitHub JSON Dynamic Configuration
*Inspired by MDMesh / Headwind MDM Open Architecture*

To enable centralized remote management without requiring static IP addresses baked into the Android APK:
1. **GitHub Dynamic URL Pointer:** The Android client queries a remote, publicly accessible GitHub JSON file (e.g. `https://raw.githubusercontent.com/.../fdc-config.json`).
2. **Dynamic Admin Server Resolution:** The GitHub JSON provides the active `admin_server_url` (pointing to the local MAMP PRO server at `http://192.168.31.105/fdc-admin` or a remote tunnel/domain).
3. **MAMP PRO Web Console:** A complete PHP/JS administrative suite hosted on macOS MAMP PRO:
   - Web-based fleet monitor showing connected device hardware, battery, and live heartbeat.
   - Master switches for Permanent 24/7 Strict Mode, Anti-Tamper Shield, Factory Reset Lock, and Safe Boot Lock.
   - Live Application Blacklist manager.
   - Automated `config_version` bumping and GitHub JSON exporter.
4. **Permanent Autonomic Re-enforcement:** Ingested remote policies are committed to `PersistentPolicyManager` and re-applied immediately upon device boot via `FdcBootReceiver`. Policy changes stay enforced **forever** until explicitly updated.


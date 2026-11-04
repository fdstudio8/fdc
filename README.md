# Fahad Device Controller (FDC) — Universal Android Enterprise

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%20to%2015-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Universal%20AOSP%20DPC-blue.svg)](https://developer.android.com/work/dpc)
[![Design](https://img.shields.io/badge/Design-Apple%20Human%20Interface%20Guidelines-black.svg?logo=apple)](https://developer.apple.com/design/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#)

**Fahad Device Controller (FDC)** is an enterprise-grade Android Device Policy Controller (DPC) and study governance platform built to support the widest possible ecosystem of Android devices without hard vendor dependencies.

Core supervision is built directly upon standard **Android Enterprise Device Owner** APIs. Manufacturer tools—including **Samsung Knox**, **Xiaomi HyperOS/MIUI**, **OPPO/Realme ColorOS**, and **Vivo OriginOS**—are treated as optional, reflectively loaded hardware extensions.

---

## 📱 Supported Manufacturers & Platforms

- **Samsung** (Galaxy S / Z / A series, One UI 5, 6, 7)
- **Google Pixel** (Pixel 6, 7, 8, 9, Stock Android 13–15)
- **Xiaomi / Redmi / POCO** (HyperOS, MIUI 14)
- **OPPO / OnePlus / Realme** (ColorOS, OxygenOS, Realme UI)
- **Vivo / iQOO** (Funtouch OS, OriginOS)
- **Motorola** (Edge, Moto G series, Hello UI)
- **Nothing** (Nothing Phone (1), (2), (2a), Nothing OS)
- **All other Android devices** running Android 8.0 (API 26) through Android 15 (API 35+)

---

## ⚡ Three Operating Modes

| Mode | Target Deployment | Capabilities |
| :--- | :--- | :--- |
| **Mode 1: Standard Mode** | Sideloaded or normal APK install | PIN Admin Auth, Pomodoro Study Timer, Encrypted Notes & Schedules, App Inventory Scanner. System policy toggles are safely locked with *"Requires Device Owner"* indicators. |
| **Mode 2: Device Owner Mode** | Official Android Enterprise provisioning | Full AOSP DPC features: Stealth App Hiding, App Suspension (monochrome disabled icons), App Uninstall Block, Settings Factory Reset Block, Kiosk Lock Task Mode, System Policy Restrictions. |
| **Mode 3: OEM Extension Mode** | Device Owner + Vendor SDK detected | Dynamic reflection into Samsung Knox (dormant on non-Samsung devices), Xiaomi AutoStart & Security Center bridges, Oplus Deep Sleep exemptions. |

---

## 🎨 Apple Human Interface Design System

Built from the ground up in **Jetpack Compose** following Apple design principles:
- **Fluid Springs:** Second-order differential springs (`dampingRatio = 1.0` for settle, `0.8` for momentum) with velocity inheritance.
- **Immediate Response:** Zero touch latency with tactile `scale(0.97f)` compression on pointer-down.
- **Frosted Materials:** Layered translucent card surfaces (`0.5dp` light-catching border).
- **Multi-Modal Feedback:** Synchronized physical spring animations and tactile haptic vibration patterns.

---

## 🚀 Enrollment Workflows

### 1. Developer / PC Provisioning via ADB
1. Go to **Settings -> Accounts** and remove all Google/Samsung/Xiaomi accounts (or use a newly wiped phone).
2. Install the APK:
   ```bash
   adb install app-debug.apk
   ```
3. Set FDC as the single Device Owner:
   ```bash
   adb shell dpm set-device-owner com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver
   ```

### 2. Out-of-the-Box QR Provisioning
1. Power on a newly factory-reset device.
2. Tap the initial **"Welcome"** screen **6 times** in rapid succession.
3. When the camera launches, scan the Android Enterprise provisioning QR code.

---

## 🔐 Multi-Channel Admin Access Interface

To guarantee zero accidental lockouts:
1. **Primary Interface:** In-App circular PIN keypad with rate-limited exponential lockout.
2. **Secret Tap Sequence:** Rapidly tap the top header brand crest **3 times within 800ms followed by a 1.5-second long press** from any screen.
3. **Quick Settings Tile:** Add the **"FDC Admin"** tile to the Android notification shade to trigger admin authentication from anywhere.
4. **Telephony Dialer Code:** Dial `*#*#1234#*#*` on telephony-supported smartphones.

---

## 📦 Project Documentation

- [`PRD.md`](PRD.md) — Comprehensive Product Requirements Document
- [`Architecture.md`](Architecture.md) — Multi-tier System Architecture & Dynamic Reflection
- [`design.md`](design.md) — Apple HIG design specifications and Compose token system
- [`memory.md`](memory.md) — Context retention, OEM fragmentation logs & ADRs
- [`rule.md`](rule.md) — Mandatory engineering invariants and security rules
- [`tracker.md`](tracker.md) — Implementation tracker & Multi-OEM test matrix


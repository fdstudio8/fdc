# Fahad Device Controller (FDC) — Project Memory & Knowledge Base
**Context Retention, OEM Fragmentation Log & Architectural Decision Records (ADRs)**  
*Document Version: 2.0.0 | Maintainer: Core Systems Architecture Team*

---

## 1. Project Background & Evolution

### 1.1 The Legacy Paradigm (v1.0)
FDC initially started as a Samsung-exclusive device management tool tightly coupled with the proprietary Samsung Knox Enterprise SDK (`com.samsung.android.knox.*`). While Knox offered deep system control on Galaxy smartphones, this created three critical bottlenecks:
1. **Total Incompatibility with Non-Samsung Hardware:** Installing v1.0 on a Xiaomi, Pixel, or OnePlus phone caused immediate `NoClassDefFoundError` crashes upon class loading.
2. **Knox License Key Dependency:** Knox SDK required active enterprise licenses (KLM/ELM keys) and Samsung Knox portal enrollment, creating severe friction for standard users and developers.
3. **Fragility Across Android Updates:** Major One UI updates frequently shifted Knox API endpoints and deprecated critical methods.

### 1.2 The Universal Paradigm (v2.0)
In v2.0, FDC underwent a ground-up architectural reimagining:
- **Universal Bedrock:** Standard AOSP **Android Enterprise Device Owner (DPC)** APIs were adopted as the sole primary framework. Every Android device running Android 8.0+ inherently possesses these APIs at the OS level.
- **OEM Extensions as Optional Plugins:** Samsung Knox was isolated behind a dynamic reflection facade (`SamsungKnoxExtension`). If the device is not Samsung or lacks Knox, the extension remains dormant.
- **Adaptive Capability Detection:** FDC scans the operating environment upon launch and dynamically configures its feature matrix.

---

## 2. OEM Fragmentation Knowledge Base

Different Android manufacturers modify the standard Android Open Source Project (AOSP) in ways that impact background execution, device management, and UI rendering. The following matrix documents proven behaviors and mitigations:

### 2.1 Samsung (One UI)
- **Behavior:** Highly compliant with Android Enterprise DO APIs. Provides standard DPC hooks.
- **Knox Opportunity:** Compatible devices support dynamic reflection into Knox Customization and Container APIs.
- **Quirk:** Battery optimization can restrict foreground service start if enrolled in "Deep Sleeping Apps".
- **Mitigation:** FDC registers `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` and verifies Device Owner status to bypass sleep restrictions.

### 2.2 Xiaomi / Redmi / POCO (MIUI & HyperOS)
- **Behavior:** Heavily customized Android skin with proprietary security layer (`SecurityCenter`).
- **Quirk 1:** "MIUI Optimization" can interfere with standard Android Enterprise provisioning if enabled during ADB command execution.
- **Quirk 2:** AutoStart is disabled by default for user applications. If FDC is killed, it won't restart on boot unless AutoStart is granted.
- **Mitigation:** When Xiaomi is detected, FDC guides the user to the native AutoStart manager (`com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity`).

### 2.3 OPPO / OnePlus / Realme (ColorOS / OxygenOS / Realme UI)
- **Behavior:** Unified codebase (Oplus OS). Strong background process termination ("Quick Freeze").
- **Quirk:** Kills foreground services if "Allow background activity" is unchecked in battery settings.
- **Mitigation:** FDC detects Oplus build markers (`ro.build.version.oplusrom`) and directs admins to the Oplus power optimization whitelist.

### 2.4 Vivo / iQOO (Funtouch OS / OriginOS)
- **Behavior:** Managed by Vivo's proprietary `iManager` (`com.iqoo.secure`).
- **Quirk:** Background network calls are restricted in sleep mode unless "High Background Power Consumption" is allowed.
- **Mitigation:** Detection of Vivo manufacturer flags triggers targeted intents to `iManager` power exception settings.

### 2.5 Google Pixel, Nothing, Motorola
- **Behavior:** Near-stock AOSP implementations with pure adherence to Android Enterprise CDD (Compatibility Definition Document).
- **Outcome:** Flawless reference execution for `setPackagesSuspended`, `setApplicationHidden`, `setLockTaskPackages`, and `addUserRestriction`.

---

## 3. Architectural Decision Records (ADRs)

### ADR-001: Separation of Core DPC from Vendor Extensions
- **Context:** Hard-linking to Samsung Knox JARs prevented compilation or runtime execution on non-Samsung devices.
- **Decision:** Implement a clean `OemExtension` interface with lazy, reflective class loading. The core APK links strictly against public Android SDK (`android.jar`).
- **Consequence:** One single universal APK runs safely on all Android OEMs without crashing.

### ADR-002: Real-Time Policy Verification vs. Optimistic State
- **Context:** Many management apps assume an API call succeeded and display an "Enforced" toggle even if the OEM silently ignored it or threw a security exception.
- **Decision:** All policy states must be verified via getter methods (e.g., `isPackageSuspended()`, `isApplicationHidden()`, `getUserRestrictions()`) before updating UI state.
- **Consequence:** Users and administrators always see the true, verified hardware state. Zero false claims.

### ADR-003: 4-Channel Administrative Access System
- **Context:** Relying solely on dialer codes (`*#1234#`) causes catastrophic lockouts on tablets, Wi-Fi devices, and modern Android 14+ devices where carrier apps restrict dialer broadcasts.
- **Decision:** Provide four redundant access channels: (1) In-App Keypad, (2) Secret Multi-Tap Header Gesture, (3) Quick Settings Tile, (4) Optional Dialer Secret Code.
- **Consequence:** Guaranteed administrator access across any form factor and OEM skin.

### ADR-004: Apple Design System on Android Jetpack Compose
- **Context:** Traditional Android enterprise apps are visual eyesores—clunky, clinical, and unresponsive.
- **Decision:** Adopt Apple Human Interface Guidelines translated to Compose: physical springs (`SpringSpec`), frosted glass materials, tactile haptic feedback, zero touch-down latency.
- **Consequence:** Outstanding user engagement, intuitive policy comprehension, and delightful tactile responsiveness.


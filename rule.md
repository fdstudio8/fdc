# Fahad Device Controller (FDC) — Engineering Rules & Architectural Invariants
**Core Coding Standards, Security Policies & Operational Safeguards**  
*Document Version: 2.0.0 | Enforcement: Mandatory for all Code Contributions*

---

## 1. Golden Rules of Architecture

### Rule 1: Zero Proprietary Compile-Time Coupling
- **Requirement:** The FDC core build configuration (`app/build.gradle.kts`) must **never** declare static compile-time dependencies on vendor-specific proprietary SDKs (e.g., `samsung-knox.jar`, Xiaomi MiPush, Huawei HMS Core).
- **Enforcement:** All manufacturer-specific extensions must be quarantined inside the `com.fahad.devicecontroller.oem` package and accessed exclusively through dynamic runtime inspection and reflection.
- **Invariant:** The generated universal APK must install and boot cleanly on an AOSP generic system image (GSI) with zero missing class crashes.

### Rule 2: Strict Veracity in Policy Reporting ("Truth in Advertising")
- **Requirement:** FDC shall never claim a policy or restriction is "Enforced" or "Active" based solely on the fact that an API write call was dispatched.
- **Enforcement:** Every policy toggle must query the system's actual state (e.g., via `devicePolicyManager.isApplicationHidden(admin, pkg)` or `userManager.hasUserRestriction(DISALLOW_FACTORY_RESET)`) to confirm success.
- **Invariant:** If an OEM silently ignores an API call or throws an unhandled `SecurityException`, the UI must flag the state as `UNSUPPORTED_BY_HARDWARE` with a descriptive rationale.

### Rule 3: Mandatory Administrator Authentication for All Mutations
- **Requirement:** Any operation that alters a system restriction, modifies an app suspension status, triggers kiosk mode, or initiates deprovisioning must verify an active administrator cryptographic session.
- **Enforcement:**
  - PIN verification must be performed using salted PBKDF2/SHA-256 hashes.
  - Plaintext PINs must never be logged or persisted in memory beyond the immediate verification scope.
  - Brute-force protection must enforce progressive exponential backoff (lockout after 5 consecutive failures).

### Rule 4: Apple Design System Fidelity
- **Requirement:** All UI components in Jetpack Compose must strictly adhere to the Apple Design translation guidelines.
- **Enforcement:**
  - **Zero Touch-Down Latency:** Visual press feedback (`scale(0.97f)`) must occur on pointer-down, not pointer-up.
  - **Physics Over Keyframes:** Animations must use `spring()` with explicit `dampingRatio` and `stiffness`. Fixed-duration linear or ease-in-out curves are prohibited for interactive gesture-driven components.
  - **Frosted Translucency:** Card surfaces must utilize translucent layered materials (`alpha 0.7f - 0.85f`) with `0.5dp` glass stroke highlights.
  - **Dynamic Contrast:** High contrast between text labels and frosted glass backgrounds must be maintained in both light and dark themes.

### Rule 5: Explicit Security Boundary Disclosures
- **Requirement:** The application documentation, setup wizard, and UI must never make false or exaggerated security claims.
- **Enforcement:**
  - Explicitly document that Android Enterprise Device Owner restricts user-initiated factory resets from the Android Settings UI and prevents uninstallation from the launcher.
  - Clearly disclose that Device Owner status does **not** protect against physical hardware disassembly, manufacturer EDL cable flashing, custom bootloader ROM replacements, or hardware recovery wipes unless supplementary OEM bootloader locks exist.

### Rule 6: Atomic & Clean Deprovisioning Lifecycle
- **Requirement:** Authorized deprovisioning must leave the target device in a clean, usable state.
- **Enforcement:**
  - Before calling `clearDeviceOwnerApp()`, the deprovisioning controller must systematically:
    1. Unsuspend all currently suspended applications.
    2. Unhide all currently hidden applications.
    3. Clear all added user restrictions (`DISALLOW_FACTORY_RESET`, `DISALLOW_SAFE_BOOT`, etc.).
    4. Remove all lock task package allowances.
    5. Terminate active kiosk mode.
  - Deprovisioning must require affirmative confirmation with Administrator PIN entry.

### Rule 7: Multi-Channel Fail-Safe Entry Points
- **Requirement:** The app must never depend exclusively on dialer codes (`*#*#1234#*#*`) for administrator entry.
- **Enforcement:**
  - The Secret Tap Sequence (3 rapid taps + 1.5s long press on top header) and Quick Settings Tile must always remain operational as primary and secondary fallbacks.
  - Dialer receivers must verify telephony capability prior to registering actions to avoid background crashes on Wi-Fi tablets.

### Rule 8: Anti-Bypass & Master Key Invariant
- **Requirement:** No user shall be able to circumvent FDC policies without the administrator password or the Emergency Master Key.
- **Enforcement:**
  - When Device Owner is active, `enforceTamperResistance()` must block uninstallation, Settings-based app force-stop/data-clear (`DISALLOW_APPS_CONTROL`), clock manipulation (`DISALLOW_CONFIG_DATE_TIME`), factory resets, and safe-boot reboots.
  - The Emergency Master Key (`FDC-MASTER-9988-7766`) must be validated using constant-time comparison (`MessageDigest.isEqual`) and must provide a guaranteed recovery mechanism even during brute-force lockouts.


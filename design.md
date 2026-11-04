# Fahad Device Controller (FDC) — Design Specification
**Apple Human Interface Guidelines Translated to Modern Android (Jetpack Compose)**  
*Document Version: 2.0.0 | Design Foundation: Fluid Interfaces, Materials & Depth, Restraint*

---

## 1. Design Philosophy & Core Principles

The user interface of FDC bridges the precision, deference, and physical tactility of **Apple Human Interface Guidelines (HIG)** with modern **Android Jetpack Compose**. The goal is an interface that ceases to feel like a cold enterprise utility and feels like a responsive, living physical object.

```
+-----------------------------------------------------------------------------------+
|                            THE FOUR HUMAN EXPERIENCES                             |
|                                                                                   |
|    SAFETY / PREDICTABILITY      UNDERSTANDING          AGENCY            JOY      |
|    Clear DO status badges,      Contextual notes on   Always allow   Fluid spring |
|    no false active claims       OEM restrictions      admin exit     responsiveness|
+-----------------------------------------------------------------------------------+
```

### The 6 Core Tenets:
1. **Response — Eliminate Latency:** Feedback is triggered on **pointer-down** (`MotionEvent.ACTION_DOWN`), never on touch release. Interactive elements compress (`scale(0.97f)`) instantly upon initial contact.
2. **Behavior Over Animation — Physical Springs:** Every movement is driven by second-order differential spring physics (`SpringSpec`), never static cubic-bezier duration curves. If a user interrupts a closing sheet or card, the animation inherits live velocity and reverses without stutter.
3. **Materials & Depth — Translucency Conveys Hierarchy:** Floating cards, navigation bars, and modals utilize translucent frosted surfaces (`alpha: 0.75f - 0.85f`) accented with a subtle `0.5dp` top/border highlight simulating physical glass catching ambient light.
4. **Spatial Consistency:** Elements enter and dismiss along identical paths. Modals scale upward from their origin trigger; sheets anchor to the bottom display bezel.
5. **Restraint & Honest Deference:** Never claim a system restriction is active if the underlying Android Enterprise policy failed to register. When a feature is unsupported (e.g., standard mode or OEM hardware lockout), it is badged in neutral slate with an expandable explanation sheet.
6. **Optical Typography:** Headlines feature negative tracking (`-0.02em`) to bind large letterforms; body copy maintains comfortable leading (`1.4em`) for effortless readability.

---

## 2. Color System & Semantic Tokens

FDC adopts Apple's adaptive semantic color palette, mapped to both Light and Dark themes in Jetpack Compose:

| Token Name | Light Value | Dark Value | Semantic Purpose |
| :--- | :--- | :--- | :--- |
| `SystemBackground` | `#F2F2F7` (System Gray 6) | `#000000` (Pure Black) | Canvas background |
| `SecondaryBackground` | `#FFFFFF` (Pure White) | `#1C1C1E` (System Gray 6 Dark) | Elevated card containers |
| `TertiaryBackground` | `#E5E5EA` (System Gray 5) | `#2C2C2E` (System Gray 5 Dark) | Recessed inputs, pill tracks |
| `GlassSurface` | `rgba(255,255,255,0.72)` | `rgba(28,28,30,0.75)` | Frosted navigation, sheets |
| `GlassBorder` | `rgba(255,255,255,0.40)` | `rgba(255,255,255,0.12)` | 0.5dp light-catching edge |
| `SystemBlue` | `#007AFF` | `#0A84FF` | Primary actions, links |
| `SystemMint` | `#34C759` | `#30D158` | Verified Active status, DO Active |
| `SystemPurple` | `#AF52DE` | `#BF5AF2` | Samsung Knox / OEM Extension Active |
| `SystemOrange` | `#FF9500` | `#FF9F0A` | Standard Mode, Pending Configuration |
| `SystemRed` | `#FF3B30` | `#FF453A` | Deprovision, Danger, Lockout |
| `LabelPrimary` | `#000000` | `#FFFFFF` | Dominant headers, card titles |
| `LabelSecondary` | `#8E8E93` | `#8E8E93` | Subtitles, API descriptions |
| `LabelTertiary` | `#C7C7CC` | `#48484A` | Disabled states, placeholders |

---

## 3. Motion & Spring Dynamic Tokens

All Compose transitions leverage custom `SpringSpec` parameters:

```kotlin
object AppleMotionDefaults {
    // Critically damped default — smooth settle, zero bounce (for modals, cards, tabs)
    val CriticallyDamped = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy, // 1.0f
        stiffness = Spring.StiffnessMediumLow       // 300f
    )

    // Momentum interaction — subtle physical bounce (for switches, button snaps, flicks)
    val MomentumBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy, // 0.75f
        stiffness = Spring.StiffnessLow             // 200f
    )

    // Snappy tactile response (for keypad press and toggle flip)
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium          // 500f
    )
}
```

---

## 4. Component Design Specifications

### 4.1 `AppleCard` (Frosted Glass Container)
- **Shape:** Rounded rectangle with continuous `20.dp` corner radius.
- **Surface:** `SecondaryBackground` with alpha `0.85f`, backdrop blur filter where supported.
- **Border:** `0.5.dp` solid stroke with `GlassBorder`.
- **Interaction:** On pointer press, scales smoothly to `0.98f` using `CriticallyDamped` spring.

### 4.2 `AppleSegmentedControl` (Sliding Pill Switcher)
- **Track:** Recessed container (`TertiaryBackground`) with `12.dp` rounded corners and `4.dp` internal padding.
- **Thumb:** Pure white (or high-contrast dark) elevated pill with subtle `1.dp` drop shadow.
- **Transition:** Thumb slides horizontally across options utilizing `CriticallyDamped` spring, tracking finger velocity on swipe.

### 4.3 `AppleStatusBadge` (Transparent Status Pill)
- **Form:** Pill (`CircleShape`) with `horizontal: 10.dp, vertical: 4.dp` padding.
- **Variants:**
  - **`Active` (Device Owner Verified):** Glowing `SystemMint` tint with subtle dot indicator.
  - **`Standard` (Mode 1):** Neutral `SystemOrange` with alert outline.
  - **`Extension` (Knox / OEM Active):** Elegant `SystemPurple` badge.
  - **`Unsupported`:** Muted `LabelTertiary` badge with informational query icon.

### 4.4 `ApplePinKeypad` (Circular Tactile Keypad)
- **Form:** 12-key matrix (digits 0–9, biometric button, delete button).
- **Key Dimension:** `72.dp` circular targets with generous `16.dp` spacing.
- **Feedback:** Immediate `scale(0.92f)` compression on pointer-down with light tactile vibration.
- **Progression Indicator:** 4 frosted circular dots that illuminate with `SystemBlue` fill as digits are entered.
- **Error Behavior:** 3-cycle horizontal spring shake animation (`damping = 0.5f`) with tactile double-buzz on incorrect PIN.

### 4.5 `AppleSheet` (Bottom Action Sheet)
- **Geometry:** Top corner radius `28.dp`, translucent glass surface.
- **Direct Manipulation:** Follows pointer `1:1` from the exact touch grab coordinate.
- **Momentum Projection:** Release velocity determines snap target using exponential decay (`decelerationRate = 0.998f`).
- **Rubber-Banding:** Progressive resistance past top boundary prevents jarring clipping.

---

## 5. Screen Hierarchy & Wayfinding

1. **Dashboard (`HomeScreen`):**
   - Frosted Glass Header with Live Operating Mode Badge (Mode 1 / Mode 2 / Mode 3).
   - Quick Metrics: Managed App Count, Active Study Session, Restrictions Active.
   - Core Action Cards: Study Focus Mode, Policy Center, Compatibility Audit, Enrollment Guide.
2. **Compatibility Audit (`CompatibilityReportScreen`):**
   - Hardware ID & Android API level inspection.
   - Device Owner & DPC Active status.
   - Comprehensive API audit list with live status indicators (Suspension, Hiding, Lock Task, Reset Protection).
   - OEM Extension Status (Knox SDK reflection result, HyperOS autostart state).
3. **Policy Manager (`PolicyManagerScreen`):**
   - Granular app selection list with search and filter.
   - One-tap switches for Hide, Suspend, and Block Uninstall.
   - System safeguard switches (Block Factory Reset, Block Safe Boot, Block Account Addition).
4. **Distraction-Free Study (`StudyModeScreen`):**
   - Minimalist circular timer with Apple-style smooth arc progression.
   - "Activate Lock Task Kiosk" toggle (gated by Device Owner).
5. **Enrollment Center (`EnrollmentGuideScreen`):**
   - Interactive selector for ADB Setup vs. Out-of-Box QR Provisioning.
   - Copyable ADB command box with instant feedback.
   - Dynamic QR code payload viewer with SHA-256 validation.


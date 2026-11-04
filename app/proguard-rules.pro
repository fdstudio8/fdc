# Proguard Rules for Fahad Device Controller
# Preserve DPC Receiver and Tile Service
-keep class com.fahad.devicecontroller.dpc.** { *; }
-keep class com.fahad.devicecontroller.admin.** { *; }
-keep class com.fahad.devicecontroller.core.model.** { *; }

# Preserve dynamic reflection classes for Knox and OEM extensions
-keep class com.samsung.android.knox.** { *; }
-dontwarn com.samsung.android.knox.**


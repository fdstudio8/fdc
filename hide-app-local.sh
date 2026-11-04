#!/bin/bash
# ==============================================================================
# FDC — Direct Local App Hider / Freezer (100% Offline / Zero Cloud)
# ==============================================================================

ADB="/Users/fahadahmad/Library/Android/sdk/platform-tools/adb"

if [ "$1" == "--unhide" ]; then
    TARGET=$2
    if [ -z "$TARGET" ]; then
        echo "Usage: ./hide-app-local.sh --unhide <package_name>"
        exit 1
    fi
    echo "🟢 Unhiding / Re-enabling: $TARGET"
    $ADB shell pm enable --user 0 "$TARGET"
    exit 0
fi

if [ "$1" == "--list" ]; then
    echo "🔍 Disabled / Hidden packages on device:"
    $ADB shell pm list packages -d --user 0
    exit 0
fi

TARGET=$1
if [ -z "$TARGET" ]; then
    echo "Usage:"
    echo "  ./hide-app-local.sh <package_name>           # Hides the app from launcher"
    echo "  ./hide-app-local.sh --unhide <package_name>  # Restores / unhides the app"
    echo "  ./hide-app-local.sh --list                   # Lists currently hidden apps"
    echo ""
    echo "Example:"
    echo "  ./hide-app-local.sh com.instagram.android"
    echo "  ./hide-app-local.sh com.google.android.youtube"
    exit 1
fi

echo "🚫 Hiding / Disabling package from launcher: $TARGET"
$ADB shell pm disable-user --user 0 "$TARGET"
echo "✅ Done! Package $TARGET is completely vanished from your phone's launcher and drawer."

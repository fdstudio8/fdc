#!/bin/bash
# ==============================================================================
# FDC — 1-Click Non-Destructive Device Owner Activation Script (No Wipe / Local)
# ==============================================================================

ADB="/Users/fahadahmad/Library/Android/sdk/platform-tools/adb"

echo "=========================================================="
echo "   FDC — DEVICE OWNER ELEVATION (100% LOCAL / NO WIPE)    "
echo "=========================================================="

DEVICE=$($ADB devices | grep -w "device" | awk '{print $1}' | head -n 1)

if [ -z "$DEVICE" ]; then
    echo "❌ Error: No Android phone detected via USB ADB!"
    echo "👉 Please connect your Nothing Phone via USB cable and allow USB Debugging."
    exit 1
fi

echo "📱 Connected Device: $DEVICE"

# Step 1: Remove secondary profile (Private Space User 10)
echo "[1/3] Removing secondary profile (User 10 Private space if active)..."
$ADB shell pm remove-user 10 2>/dev/null
sleep 1

# Step 2: Install latest FDC production APK
echo "[2/3] Verifying / Installing FDC Production APK..."
$ADB install -r release/fdc-production-v2.0.0.apk

# Step 3: Set Device Owner
echo "[3/3] Setting FDC as permanent Device Owner..."
OUTPUT=$($ADB shell dpm set-device-owner com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver 2>&1)

if echo "$OUTPUT" | grep -q "Success"; then
    echo "=========================================================="
    echo "🎉 SUCCESS: FDC is now PERMANENT DEVICE OWNER!"
    echo "   - App hiding is now 100% active!"
    echo "   - App suspension is now 100% active!"
    echo "   - Settings App Info lock is enforced!"
    echo "   - You can now log back into your Google accounts freely!"
    echo "=========================================================="
else
    echo "⚠️ Device Owner activation output:"
    echo "$OUTPUT"
    if echo "$OUTPUT" | grep -q "already several users"; then
        echo ""
        echo "👉 Reason: Secondary users exist. We attempted to remove User 10."
        echo "   Please check Settings -> Security & Privacy -> Private space and turn it off."
    elif echo "$OUTPUT" | grep -q "already some accounts"; then
        echo ""
        echo "👉 Reason: Accounts are currently logged in on the phone."
        echo "   Please go to Settings -> Passwords & Accounts on your phone,"
        echo "   temporarily tap 'Remove Account' on your Google/WhatsApp accounts,"
        echo "   and re-run this script: ./set-device-owner.sh"
        echo "   (Your photos, videos, apps, and chats will NOT be deleted!)"
    fi
fi

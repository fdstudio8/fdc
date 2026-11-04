#!/usr/bin/env bash
set -e

KEYSTORE_PATH="keystore/fdc-release.jks"
KEYSTORE_PASS="FdcEnterprise2026!"
KEY_ALIAS="fdc_release_key"
KEY_PASS="FdcEnterprise2026!"
BUILD_TOOLS_DIR="/Users/fahadahmad/Library/Android/sdk/build-tools/35.0.0"

# Production GitHub URLs (Replace username with your actual GitHub username)
GITHUB_USER="fahadahmad"
GITHUB_REPO="fdc"
GITHUB_RELEASE_TAG="v2.0.0"
GITHUB_APK_URL="https://github.com/$GITHUB_USER/$GITHUB_REPO/releases/download/$GITHUB_RELEASE_TAG/fdc-production.apk"
GITHUB_CONFIG_JSON_URL="https://raw.githubusercontent.com/$GITHUB_USER/fdc-fleet-config/main/config.json"

echo "=========================================================="
echo "   FDC — FULL AUTO-SETUP PRODUCTION RELEASE PIPELINE      "
echo "=========================================================="

mkdir -p keystore release

# Step 1: Create Production Keystore if not exists
if [ ! -f "$KEYSTORE_PATH" ]; then
    echo "[1/4] Generating Production Keystore..."
    /Users/fahadahmad/.jre21/Contents/Home/bin/keytool -genkeypair -v \
        -keystore "$KEYSTORE_PATH" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$KEYSTORE_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Fahad Ahmad, OU=Device Control, O=FDC Enterprise, L=New Delhi, ST=Delhi, C=IN"
    echo "Production keystore created at: $KEYSTORE_PATH"
else
    echo "[1/4] Using existing Production Keystore: $KEYSTORE_PATH"
fi

# Step 2: Assemble Release APK
echo "[2/4] Compiling and assembling Release APK with Gradle..."
./gradlew assembleRelease

UNSIGNED_APK="app/build/outputs/apk/release/app-release-unsigned.apk"
SIGNED_APK="release/fdc-production-v2.0.0.apk"

# Step 3: Sign Release APK with apksigner (V2 + V3 Enterprise Signing)
echo "[3/4] Signing APK with apksigner (V2 + V3 Enterprise Signing)..."
cp "$UNSIGNED_APK" "$SIGNED_APK"

"$BUILD_TOOLS_DIR/apksigner" sign \
    --ks "$KEYSTORE_PATH" \
    --ks-key-alias "$KEY_ALIAS" \
    --ks-pass "pass:$KEYSTORE_PASS" \
    --key-pass "pass:$KEY_PASS" \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --v3-signing-enabled true \
    "$SIGNED_APK"

"$BUILD_TOOLS_DIR/apksigner" verify -v "$SIGNED_APK"

# Step 4: Calculate SHA-256 Checksum (URL-Safe Base64)
echo "[4/4] Calculating Android Enterprise Checksum..."
SHA256_HEX=$(shasum -a 256 "$SIGNED_APK" | awk '{print $1}')
SHA256_BASE64=$(openssl dgst -binary -sha256 "$SIGNED_APK" | openssl base64 | tr '/+' '_-' | tr -d '=')

# Copy to MAMP PRO so it can also be served locally
cp "$SIGNED_APK" "/Applications/MAMP/htdocs/fdc-admin/fdc-production.apk" 2>/dev/null || true
cp "$SIGNED_APK" "/Users/fahadahmad/Sites/localhost/fdc-admin/fdc-production.apk" 2>/dev/null || true

# Generate GitHub-Powered Auto-Provisioning QR Code JSON
cat <<EOF > release/android-enterprise-github-qr.json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.fahad.devicecontroller/.dpc.FdcDeviceAdminReceiver",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "$GITHUB_APK_URL",
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": "$SHA256_BASE64",
  "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": true,
  "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": false,
  "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE": {
    "auto_setup": true,
    "github_config_url": "$GITHUB_CONFIG_JSON_URL",
    "permanent_lockdown": true,
    "anti_tamper_shield": true
  }
}
EOF

# Copy QR JSON to MAMP PRO directory
cp release/android-enterprise-github-qr.json /Applications/MAMP/htdocs/fdc-admin/qr-payload.json 2>/dev/null || true
cp release/android-enterprise-github-qr.json /Users/fahadahmad/Sites/localhost/fdc-admin/qr-payload.json 2>/dev/null || true

echo ""
echo "=========================================================="
echo "   PRODUCTION RELEASE READY!                              "
echo "=========================================================="
echo "Signed APK: $SIGNED_APK"
echo "SHA-256 (Hex): $SHA256_HEX"
echo "SHA-256 (Base64 URL-Safe): $SHA256_BASE64"
echo "GitHub Download Location: $GITHUB_APK_URL"
echo ""
echo "GitHub Auto-Provisioning QR JSON:"
cat release/android-enterprise-github-qr.json
echo ""

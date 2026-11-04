package com.fahad.devicecontroller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.core.model.PolicyEnforcementStatus
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * High-contrast Apple frosted status pill badge.
 */
@Composable
fun AppleStatusBadge(
    status: PolicyEnforcementStatus,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val colors = LocalAppleColors.current

    val (bgColor, textColor, label) = when (status) {
        PolicyEnforcementStatus.ENFORCED_ACTIVE -> Triple(
            colors.mint.copy(alpha = 0.15f),
            colors.mint,
            customLabel ?: "Active & Verified"
        )
        PolicyEnforcementStatus.PENDING_ELEVATION -> Triple(
            colors.orange.copy(alpha = 0.15f),
            colors.orange,
            customLabel ?: "Pending Elevation"
        )
        PolicyEnforcementStatus.REQUIRES_DEVICE_OWNER -> Triple(
            colors.tertiaryBackground,
            colors.secondaryLabel,
            customLabel ?: "Requires Device Owner"
        )
        PolicyEnforcementStatus.UNSUPPORTED_ON_HARDWARE -> Triple(
            colors.red.copy(alpha = 0.12f),
            colors.red,
            customLabel ?: "Hardware Locked"
        )
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(0.5.dp, textColor.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun AppleModeBadge(
    mode: OperatingMode,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current

    val (bgColor, textColor, label) = when (mode) {
        OperatingMode.STANDARD -> Triple(
            colors.orange.copy(alpha = 0.15f),
            colors.orange,
            "Standard Mode (Normal APK)"
        )
        OperatingMode.DEVICE_OWNER -> Triple(
            colors.mint.copy(alpha = 0.15f),
            colors.mint,
            "Device Owner Mode (Android Enterprise)"
        )
        OperatingMode.OEM_EXTENSION -> Triple(
            colors.purple.copy(alpha = 0.18f),
            colors.purple,
            "OEM Extension Mode (Knox/HyperOS)"
        )
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(0.5.dp, textColor.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}


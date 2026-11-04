package com.fahad.devicecontroller.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.core.model.OperatingMode
import com.fahad.devicecontroller.ui.theme.AppleTypography
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Frosted Apple Navigation Bar with Secret Multi-Tap Admin Entry Detector.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppleTopBar(
    operatingMode: OperatingMode,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current

    // Secret Tap State: 3 taps within 800ms followed by long press
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTimestamp by remember { mutableStateOf(0L) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.glassSurface,
        border = null,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTimestamp < 800) {
                                tapCount++
                            } else {
                                tapCount = 1
                            }
                            lastTapTimestamp = now
                        },
                        onLongClick = {
                            if (tapCount >= 3) {
                                tapCount = 0
                                onAdminClick()
                            }
                        }
                    )
            ) {
                Text(
                    text = "FDC",
                    style = AppleTypography.LargeTitle.copy(
                        fontSize = 26.sp,
                        color = colors.primaryLabel
                    )
                )
                Text(
                    text = "Universal Device Controller",
                    style = AppleTypography.Footnote.copy(
                        color = colors.secondaryLabel
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppleModeBadge(mode = operatingMode)

                // Dedicated visible Admin access button
                IconButton(
                    onClick = onAdminClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Access",
                        tint = colors.blue
                    )
                }
            }
        }
    }
}


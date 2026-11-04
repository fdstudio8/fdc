package com.fahad.devicecontroller.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fahad.devicecontroller.ui.theme.LocalAppleColors
import kotlinx.coroutines.launch

/**
 * iOS-style circular tactile keypad with PIN dots and physical error shake dynamics.
 */
@Composable
fun ApplePinKeypad(
    pinLength: Int = 4,
    currentPin: String,
    isError: Boolean,
    onPinDigitEntered: (Char) -> Unit,
    onPinDigitDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppleColors.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Shake offset for error state
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            coroutineScope.launch {
                // Perform tactile double haptic buzz
                triggerHapticFeedback(context, isError = true)
                // 3-cycle horizontal damped spring shake
                for (i in 0..2) {
                    shakeOffset.animateTo(
                        targetValue = 24f,
                        animationSpec = spring(stiffness = Spring.StiffnessHigh)
                    )
                    shakeOffset.animateTo(
                        targetValue = -24f,
                        animationSpec = spring(stiffness = Spring.StiffnessHigh)
                    )
                }
                shakeOffset.animateTo(0f)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Progress Dots
        Row(
            modifier = Modifier
                .offset(x = shakeOffset.value.dp)
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (i in 0 until pinLength) {
                val isFilled = i < currentPin.length
                val dotScale by animateFloatAsState(
                    targetValue = if (isFilled) 1.25f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "DotScale"
                )

                Box(
                    modifier = Modifier
                        .scale(dotScale)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isError -> colors.red
                                isFilled -> colors.blue
                                else -> colors.tertiaryBackground
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3x4 Keypad Grid
        val keys = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(' ', '0', '<')
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    Spacer(modifier = Modifier.width(12.dp))
                    when (key) {
                        ' ' -> {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                        '<' -> {
                            KeypadCircleButton(
                                onClick = {
                                    triggerHapticFeedback(context, isError = false)
                                    onPinDigitDeleted()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Delete",
                                    tint = colors.primaryLabel,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            KeypadCircleButton(
                                onClick = {
                                    triggerHapticFeedback(context, isError = false)
                                    onPinDigitEntered(key)
                                }
                            ) {
                                Text(
                                    text = key.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primaryLabel
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
    }
}

@Composable
private fun KeypadCircleButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = LocalAppleColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Immediate pointer-down compression
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "KeypadButtonScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) colors.tertiaryBackground else colors.secondaryBackground
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun triggerHapticFeedback(context: Context, isError: Boolean) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (isError) {
                VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1)
            } else {
                VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (isError) 150 else 20)
        }
    } catch (e: Throwable) {
        // Safe silence if vibration not permitted
    }
}


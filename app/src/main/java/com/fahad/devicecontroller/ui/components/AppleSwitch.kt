package com.fahad.devicecontroller.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fahad.devicecontroller.ui.theme.LocalAppleColors

/**
 * Apple-style tactile Switch toggle with spring motion.
 */
@Composable
fun AppleSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalAppleColors.current

    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.tertiaryLabel
            checked -> colors.mint
            else -> colors.tertiaryBackground
        },
        label = "SwitchTrackColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "SwitchThumbOffset"
    )

    val clickModifier = if (enabled && onCheckedChange != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onCheckedChange(!checked) }
        )
    } else Modifier

    Box(
        modifier = modifier
            .width(50.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(trackColor)
            .then(clickModifier),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(26.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 3.dp
        ) {}
    }
}


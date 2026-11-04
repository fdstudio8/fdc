package com.fahad.devicecontroller.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtendedAppleColors(
    val background: Color,
    val secondaryBackground: Color,
    val tertiaryBackground: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val primaryLabel: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val blue: Color,
    val mint: Color,
    val purple: Color,
    val orange: Color,
    val red: Color
)

val LocalAppleColors = staticCompositionLocalOf<ExtendedAppleColors> {
    error("No AppleColors provided")
}

@Composable
fun AppleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val extendedColors = if (darkTheme) {
        ExtendedAppleColors(
            background = AppleColor.SystemBackgroundDark,
            secondaryBackground = AppleColor.SecondaryBackgroundDark,
            tertiaryBackground = AppleColor.TertiaryBackgroundDark,
            glassSurface = AppleColor.GlassSurfaceDark,
            glassBorder = AppleColor.GlassBorderDark,
            primaryLabel = AppleColor.LabelPrimaryDark,
            secondaryLabel = AppleColor.LabelSecondaryDark,
            tertiaryLabel = AppleColor.LabelTertiaryDark,
            blue = AppleColor.SystemBlue,
            mint = AppleColor.SystemMint,
            purple = AppleColor.SystemPurple,
            orange = AppleColor.SystemOrange,
            red = AppleColor.SystemRed
        )
    } else {
        ExtendedAppleColors(
            background = AppleColor.SystemBackgroundLight,
            secondaryBackground = AppleColor.SecondaryBackgroundLight,
            tertiaryBackground = AppleColor.TertiaryBackgroundLight,
            glassSurface = AppleColor.GlassSurfaceLight,
            glassBorder = AppleColor.GlassBorderLight,
            primaryLabel = AppleColor.LabelPrimaryLight,
            secondaryLabel = AppleColor.LabelSecondaryLight,
            tertiaryLabel = AppleColor.LabelTertiaryLight,
            blue = AppleColor.SystemBlue,
            mint = AppleColor.SystemMint,
            purple = AppleColor.SystemPurple,
            orange = AppleColor.SystemOrange,
            red = AppleColor.SystemRed
        )
    }

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = AppleColor.SystemBlue,
            background = AppleColor.SystemBackgroundDark,
            surface = AppleColor.SecondaryBackgroundDark
        )
    } else {
        lightColorScheme(
            primary = AppleColor.SystemBlue,
            background = AppleColor.SystemBackgroundLight,
            surface = AppleColor.SecondaryBackgroundLight
        )
    }

    CompositionLocalProvider(LocalAppleColors provides extendedColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}


package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AureviaLightColorScheme = lightColorScheme(
    primary = DeepCharcoal,
    onPrimary = IvoryBackground,
    primaryContainer = AlabasterSurface,
    onPrimaryContainer = DeepCharcoal,
    secondary = ChampagneGold,
    onSecondary = IvorySurface,
    secondaryContainer = ChampagneGoldLight,
    onSecondaryContainer = DeepBronze,
    tertiary = DeepBronze,
    onTertiary = IvorySurface,
    tertiaryContainer = WarmStoneContainer,
    onTertiaryContainer = DeepCharcoal,
    background = IvoryBackground,
    onBackground = DeepCharcoal,
    surface = IvorySurface,
    onSurface = DeepCharcoal,
    surfaceVariant = AlabasterSurface,
    onSurfaceVariant = MutedTaupe,
    outline = WarmStoneOutline,
    outlineVariant = WarmStoneContainer,
    error = CrimsonAlert,
    onError = IvorySurface,
    errorContainer = CrimsonContainer,
    onErrorContainer = CrimsonAlert
)

private val AureviaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AureviaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AureviaLightColorScheme,
        typography = AureviaTypography,
        shapes = AureviaShapes,
        content = content
    )
}

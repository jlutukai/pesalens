package lutukai.pesalens.core.designsystem.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LocalPesaColors = staticCompositionLocalOf { PesaColors.Light }
private val LocalPesaType = staticCompositionLocalOf { PesaType.inter() }
private val LocalPesaShapes = staticCompositionLocalOf { PesaShapes() }

object PesaLensTheme {
    val colors: PesaColors
        @Composable @ReadOnlyComposable get() = LocalPesaColors.current
    val type: PesaType
        @Composable @ReadOnlyComposable get() = LocalPesaType.current
    val shapes: PesaShapes
        @Composable @ReadOnlyComposable get() = LocalPesaShapes.current
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PesaLensTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) PesaColors.Dark else PesaColors.Light
    val type = PesaType.inter()
    val shapes = PesaShapes()
    CompositionLocalProvider(
        LocalPesaColors provides colors,
        LocalPesaType provides type,
        LocalPesaShapes provides shapes,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colors.toColorScheme(),
            typography = type.toTypography(),
            shapes = shapes.toM3Shapes(),
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}

private fun PesaColors.toColorScheme() = lightColorScheme(
    primary = accent,
    onPrimary = background,
    primaryContainer = tintSubtle,
    onPrimaryContainer = accentText,
    secondaryContainer = tint,
    onSecondaryContainer = accentText,
    background = background,
    surface = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surface,
    surfaceContainerLow = surfaceAlt,
    onSurface = text,
    onBackground = text,
    onSurfaceVariant = textMuted,
    outline = border,
    outlineVariant = border,
    inverseSurface = text,
    inverseOnSurface = background,
    error = warnText,
    errorContainer = warnBackground,
    scrim = scrim,
)

private fun PesaType.toTypography() = Typography(
    displayLarge = display,
    displayMedium = display,
    displaySmall = headline,
    headlineLarge = headline,
    headlineMedium = titleLarge,
    headlineSmall = titleMedium,
    titleLarge = title,
    titleMedium = subtitle,
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = bodySmall,
    bodySmall = meta,
    labelLarge = label,
    labelMedium = kicker,
    labelSmall = caption,
)

private fun PesaShapes.toM3Shapes() = Shapes(
    extraSmall = RoundedCornerShape(5.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

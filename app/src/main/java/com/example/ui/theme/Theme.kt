package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val description: String) {
    SYSTEM("Padrão do Sistema", "Acompanha automaticamente a configuração do aparelho"),
    LIGHT("Modo Claro", "Fundo creme suave com detalhes verde-floresta"),
    DARK("Modo Escuro", "Fundo verde-floresta escuro com contrastes em creme")
}

// Consistent with the brand brief:
// Dark theme: Forest Green (#16261F) backdrop, with cream (#F7F4EC) text,
// refined elevated containers (#1E332A), and vibrant sage, orange and gold nutrition accents.
private val DarkColorScheme = darkColorScheme(
    primary = VeggieSage,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = VeggieSageLight,
    secondary = ProteinOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF47280E),
    onSecondaryContainer = ProteinOrangeLight,
    tertiary = CarbGold,
    onTertiary = ForestGreenDark,
    tertiaryContainer = Color(0xFF493A10),
    onTertiaryContainer = CarbGoldLight,
    background = ForestNightBackground,
    onBackground = TextPrimaryLight,
    surface = ForestGreenLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = ForestGreenContainer,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFF334F42),
    outlineVariant = Color(0xFF243B31)
)

// Light theme: Cream (#F7F4EC) backdrop, with forest green (#16261F) typography,
// clean white/cream cards, and balanced macro highlights.
private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = CreamBackground,
    primaryContainer = VeggieSageLight,
    onPrimaryContainer = ForestGreen,
    secondary = ProteinOrange,
    onSecondary = Color.White,
    secondaryContainer = ProteinOrangeLight,
    onSecondaryContainer = ForestGreen,
    tertiary = CarbGold,
    onTertiary = ForestGreen,
    tertiaryContainer = CarbGoldLight,
    onTertiaryContainer = ForestGreen,
    background = CreamBackground,
    onBackground = TextPrimaryDark,
    surface = CreamSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CreamBorder,
    outlineVariant = Color(0xFFD3CCC0)
)

@Composable
fun NutricaoNaPraticaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

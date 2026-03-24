package com.tradeguru.electrical.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TradeGuruColors(
    val tradeGreen: Color = TradeGreen,
    val tradeSurface: Color,
    val tradeInput: Color,
    val tradeLight: Color,
    val tradeBorder: Color,
    val tradeText: Color,
    val tradeTextSecondary: Color,
    val tradeBg: Color,
    val modeFaultFinder: Color,
    val modeLearn: Color,
    val modeResearch: Color
)

val LightTradeGuruColors = TradeGuruColors(
    tradeSurface = TradeSurfaceLight,
    tradeInput = TradeInputLight,
    tradeLight = TradeLightLight,
    tradeBorder = TradeBorderLight,
    tradeText = TradeTextLight,
    tradeTextSecondary = TradeTextSecondaryLight,
    tradeBg = TradeBgLight,
    modeFaultFinder = ModeFaultFinderLight,
    modeLearn = ModeLearnLight,
    modeResearch = ModeResearchLight
)

val DarkTradeGuruColors = TradeGuruColors(
    tradeSurface = TradeSurfaceDark,
    tradeInput = TradeInputDark,
    tradeLight = TradeLightDark,
    tradeBorder = TradeBorderDark,
    tradeText = TradeTextDark,
    tradeTextSecondary = TradeTextSecondaryDark,
    tradeBg = TradeBgDark,
    modeFaultFinder = ModeFaultFinderDark,
    modeLearn = ModeLearnDark,
    modeResearch = ModeResearchDark
)

val LocalTradeGuruColors = staticCompositionLocalOf { LightTradeGuruColors }

private val LightColorScheme = lightColorScheme(
    primary = TradeGreen,
    onPrimary = Color.White,
    surface = TradeSurfaceLight,
    onSurface = TradeTextLight,
    background = TradeBgLight,
    onBackground = TradeTextLight,
    surfaceVariant = TradeInputLight,
    onSurfaceVariant = TradeTextSecondaryLight,
    outline = TradeBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = TradeGreen,
    onPrimary = Color.White,
    surface = TradeSurfaceDark,
    onSurface = TradeTextDark,
    background = TradeBgDark,
    onBackground = TradeTextDark,
    surfaceVariant = TradeInputDark,
    onSurfaceVariant = TradeTextSecondaryDark,
    outline = TradeBorderDark
)

@Composable
fun TradeGuruTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val tradeGuruColors = if (darkTheme) DarkTradeGuruColors else LightTradeGuruColors

    CompositionLocalProvider(LocalTradeGuruColors provides tradeGuruColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TradeGuruTypography,
            content = content
        )
    }
}

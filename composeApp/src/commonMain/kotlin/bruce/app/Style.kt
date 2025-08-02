package bruce.app

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

class Style {
    companion object {
        private val darkBackgroundColor = Color(0xFF121212)
        private val purpleColor = Color(0xFF6200EE)
        private val inversePurple = Color(0xFF80FF00)
        val scheme = ColorScheme(
            primary = purpleColor,
            onPrimary = Color.White,
            primaryContainer = purpleColor,
            onPrimaryContainer = Color.White,
            inversePrimary = inversePurple,
            secondary = purpleColor,
            onSecondary = Color.White,
            secondaryContainer = purpleColor,
            onSecondaryContainer = Color.White,
            tertiary = Color.Magenta,
            onTertiary = Color.White,
            tertiaryContainer = Color.Magenta,
            onTertiaryContainer = Color.White,
            background = darkBackgroundColor,
            onBackground = Color.White,
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color.Black,
            onSurfaceVariant = Color.White,
            surfaceTint = purpleColor,
            inverseSurface = inversePurple,
            inverseOnSurface = Color.White,
            error = Color.Red,
            onError = Color.White,
            errorContainer = Color.Red,
            onErrorContainer = Color.White,
            outline = Color.Black,
            outlineVariant = Color.DarkGray,
            scrim = Color.White,
            surfaceBright = Color.White,
            surfaceDim = Color.White,
            surfaceContainer = Color.White,
            surfaceContainerHigh = Color.White,
            surfaceContainerHighest = Color.White,
            surfaceContainerLow = Color.White,
            surfaceContainerLowest = Color.White
        )
    }
}

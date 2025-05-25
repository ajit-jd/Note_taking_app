// E:\Kotlin2\Project7\app\src\main\java\com\example\project7\ui\theme\Theme.kt
package com.example.project7.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.project7.ui.screens.ThemeSetting
import com.example.project7.ui.screens.currentThemeSetting
// Import new colors
import com.example.project7.ui.theme.DarkNavy
import com.example.project7.ui.theme.LightOlive
import com.example.project7.ui.theme.LightSteelBlue
import com.example.project7.ui.theme.MediumNavy
import com.example.project7.ui.theme.OliveGreen
import com.example.project7.ui.theme.PaleOlive


// Define your Color Schemes (you can customize these)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
    // ... other colors
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    // ... other colors

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// Olive Color Scheme (Light)
private val OliveColorScheme = lightColorScheme(
    primary = OliveGreen,
    secondary = LightOlive,
    tertiary = PaleOlive
    // You can add more specific overrides for onPrimary, onSecondary, etc. if needed
    /* Example:
    onPrimary = Color.White, // Assuming text on OliveGreen should be white
    background = Color(0xFFFCFDF0), // A very light green/yellowish background
    surface = Color(0xFFF8FBF0),    // Similar to background or slightly different
    onBackground = Color(0xFF1A1C18), // Dark text for readability
    onSurface = Color(0xFF1A1C18),    // Dark text for readability
    */
)

// Navy Color Scheme (Dark)
private val NavyColorScheme = darkColorScheme(
    primary = LightSteelBlue, // Light text/icons on dark backgrounds
    secondary = MediumNavy,
    tertiary = DarkNavy
    // You can add more specific overrides for onPrimary, onSecondary, etc. if needed
    /* Example:
    onPrimary = DarkNavy, // Text on LightSteelBlue could be DarkNavy for contrast
    background = Color(0xFF000030), // Very dark blue background
    surface = Color(0xFF00003A),    // Slightly lighter or different dark blue for surfaces
    onBackground = Color(0xFFE0E0FF), // Light text for readability on dark background
    onSurface = Color(0xFFE0E0FF),    // Light text for readability on dark background
    */
)

@Composable

fun Project7Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val selectedThemeSetting = currentThemeSetting.value // Observe the global-like state

    val colorScheme = when (selectedThemeSetting) {
        ThemeSetting.OLIVE -> OliveColorScheme
        ThemeSetting.NAVY -> NavyColorScheme
        else -> if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            // Dynamic color scheme is determined by system theme for LIGHT, DARK, SYSTEM
            val useDynamicDark = when (selectedThemeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                else -> isSystemInDarkTheme() // ThemeSetting.SYSTEM
            }
            if (useDynamicDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            // Fallback to predefined schemes if dynamic color is off or not supported
            when (selectedThemeSetting) {
                ThemeSetting.LIGHT -> LightColorScheme
                ThemeSetting.DARK -> DarkColorScheme
                else -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme // ThemeSetting.SYSTEM
            }
        }
    }

    // Determine if the status/navigation bars should be light or dark based on the chosen scheme
    // This is crucial for text/icon visibility on these bars.
    // isSystemInDarkTheme() is not sufficient here as Olive is light and Navy is dark.
    val darkTheme = when (selectedThemeSetting) {
        ThemeSetting.LIGHT -> false
        ThemeSetting.DARK -> true
        ThemeSetting.OLIVE -> false // OliveColorScheme is light
        ThemeSetting.NAVY -> true   // NavyColorScheme is dark
        ThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }
    val view = LocalView.current

    // Inside Project6Theme composable in Theme.kt
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // window.statusBarColor = colorScheme.primary.toArgb() // DEPRECATED
            // window.navigationBarColor = colorScheme.surface.toArgb() // DEPRECATED
            // Set status bar and navigation bar appearance based on the effective darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming you have Typography.kt generated
        content = content
    )
}

// You'll also need Color.kt and Typography.kt in this package,
// usually generated by Android Studio New Project template.
// Make sure they exist. Example stubs:

// --- ui/theme/Color.kt ---
//package com.example.project5.ui.theme
//import androidx.compose.ui.graphics.Color
//val Purple80 = Color(0xFFD0BCFF)
//val PurpleGrey80 = Color(0xFFCCC2DC)
//val Pink80 = Color(0xFFEFB8C8)
//val Purple40 = Color(0xFF6650a4)
//val PurpleGrey40 = Color(0xFF625b71)
//val Pink40 = Color(0xFF7D5260)

// --- ui/theme/Type.kt ---
//package com.example.project5.ui.theme
//import androidx.compose.material3.Typography
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.sp
//// Set of Material typography styles to start with
//val Typography = Typography(
//    bodyLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
//)
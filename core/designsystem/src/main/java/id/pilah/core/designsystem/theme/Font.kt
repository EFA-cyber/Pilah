package id.pilah.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import id.pilah.core.designsystem.R

/**
 * Definisi font family sesuai aturan tipografi PRD (PILAH §7.3):
 * - Sans  -> `Geist Mono, ui-monospace, monospace`
 * - Serif -> `serif`
 * - Mono  -> `JetBrains Mono, monospace`
 */
object PilahFonts {

    val sans: FontFamily = FontFamily(
        Font(R.font.geist_mono_regular, FontWeight.Normal),
        Font(R.font.geist_mono_medium, FontWeight.Medium),
        Font(R.font.geist_mono_bold, FontWeight.Bold),
    )

    val serif: FontFamily = FontFamily.Serif

    val mono: FontFamily = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    )
}

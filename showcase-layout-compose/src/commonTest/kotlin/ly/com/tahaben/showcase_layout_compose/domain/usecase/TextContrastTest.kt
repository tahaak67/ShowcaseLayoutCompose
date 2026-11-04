package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TextContrastTest {

    @Test
    fun unspecifiedColorIsWhiteOnTheDefaultBlackOverlay() {
        assertEquals(Color.White, TextStyle.Default.withContrastingColor(Color.Black.copy(alpha = 0.9f)).color)
    }

    @Test
    fun unspecifiedColorIsBlackOnALightOverlayOrCard() {
        assertEquals(Color.Black, TextStyle.Default.withContrastingColor(Color.White.copy(alpha = 0.9f)).color)
        assertEquals(Color.Black, TextStyle.Default.withContrastingColor(Color.Yellow).color)
    }

    @Test
    fun anExplicitColorIsKept() {
        val style = TextStyle(color = Color.Black)
        assertSame(style, style.withContrastingColor(Color.Black))
    }

    @Test
    fun aBrushIsKept() {
        val style = TextStyle(brush = Brush.linearGradient(listOf(Color.Red, Color.Blue)))
        assertSame(style, style.withContrastingColor(Color.Black))
    }
}

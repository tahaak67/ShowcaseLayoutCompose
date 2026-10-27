package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class RoundedTargetPathTest {

    @Test
    fun largeTargetKeepsTheRequestedRadius() {
        assertEquals(28f, fittedCornerRadius(28f, Size(1000f, 400f)))
    }

    @Test
    fun smallSquareTargetIsCappedAtHalfItsSize() {
        // a 40px icon button with a 28px radius becomes a perfect circle instead of a notched one
        assertEquals(20f, fittedCornerRadius(28f, Size(40f, 40f)))
    }

    @Test
    fun thinTargetIsCappedByItsSmallerSide() {
        assertEquals(6f, fittedCornerRadius(28f, Size(300f, 12f)))
        assertEquals(6f, fittedCornerRadius(28f, Size(12f, 300f)))
    }

    @Test
    fun targetAnimatingUpFromZeroNeverGetsANegativeOrOversizedRadius() {
        assertEquals(0f, fittedCornerRadius(28f, Size(0f, 0f)))
        assertEquals(1.5f, fittedCornerRadius(28f, Size(3f, 50f)))
    }

    @Test
    fun negativeRadiusIsTreatedAsSquareCorners() {
        assertEquals(0f, fittedCornerRadius(-4f, Size(100f, 100f)))
    }
}

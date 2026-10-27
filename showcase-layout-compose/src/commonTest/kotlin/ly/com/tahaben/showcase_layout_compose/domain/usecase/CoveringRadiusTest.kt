package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoveringRadiusTest {

    private val canvas = Size(1080f, 2400f)

    @Test
    fun centredTargetNeedsHalfTheDiagonal() {
        val radius = coveringRadius(Offset(540f, 1200f), canvas)
        assertEquals(sqrt(540f * 540f + 1200f * 1200f), radius, 0.01f)
    }

    @Test
    fun cornerTargetNeedsTheWholeDiagonal() {
        val radius = coveringRadius(Offset(0f, 0f), canvas)
        assertEquals(sqrt(1080f * 1080f + 2400f * 2400f), radius, 0.01f)
    }

    @Test
    fun targetNearTheTopStillCoversTheBottomCorners() {
        // A balance card near the top of a phone: the old max(w, h) / 2 radius left the bottom
        // of the screen undimmed. Every corner must now be inside the circle.
        val center = Offset(540f, 400f)
        val radius = coveringRadius(center, canvas)
        val corners = listOf(Offset(0f, 0f), Offset(1080f, 0f), Offset(0f, 2400f), Offset(1080f, 2400f))
        corners.forEach { corner ->
            val distance = hypot(corner.x - center.x, corner.y - center.y)
            assertTrue(distance <= radius, "corner $corner is ${distance - radius}px outside the dim circle")
        }
    }

    @Test
    fun farthestCornerLiesExactlyOnTheCircle() {
        val center = Offset(900f, 300f)
        val radius = coveringRadius(center, canvas)
        assertEquals(hypot(900f, 2100f), radius, 0.01f)
    }
}

package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TouchBlockerBandsTest {

    private val canvas = IntSize(1080, 1920)

    private fun IntRect.contains(x: Int, y: Int) = x >= left && x < right && y >= top && y < bottom

    @Test
    fun bandsSurroundTheHole() {
        val bands = computeTouchBlockerBands(Rect(Offset(100f, 500f), Size(200f, 50f)), canvas)
        assertEquals(
            listOf(
                IntRect(0, 0, 1080, 500),
                IntRect(0, 550, 1080, 1920),
                IntRect(0, 500, 100, 550),
                IntRect(300, 500, 1080, 550),
            ),
            bands
        )
    }

    @Test
    fun everyPointIsCoveredExceptTheHole() {
        val hole = Rect(Offset(100f, 500f), Size(200f, 50f))
        val bands = computeTouchBlockerBands(hole, canvas)
        val probes = listOf(
            0 to 0, 1079 to 1919, 150 to 499, 150 to 550, 99 to 520, 300 to 520, // around the hole
        )
        probes.forEach { (x, y) -> assertTrue(bands.any { it.contains(x, y) }, "($x,$y) should be blocked") }
        listOf(100 to 500, 299 to 549, 200 to 520).forEach { (x, y) ->
            assertFalse(bands.any { it.contains(x, y) }, "($x,$y) should fall through")
        }
    }

    @Test
    fun fractionalHoleIsRoundedOutwards() {
        val bands = computeTouchBlockerBands(Rect(100.4f, 500.6f, 300.2f, 550.1f), canvas)
        assertEquals(IntRect(0, 0, 1080, 500), bands[0])
        assertEquals(IntRect(0, 551, 1080, 1920), bands[1])
        assertEquals(IntRect(0, 500, 100, 551), bands[2])
        assertEquals(IntRect(301, 500, 1080, 551), bands[3])
    }

    @Test
    fun holeIsClampedToTheCanvas() {
        val bands = computeTouchBlockerBands(Rect(-50f, 1900f, 100f, 2000f), canvas)
        assertEquals(IntRect(0, 0, 1080, 1900), bands[0])
        assertEquals(IntRect(0, 1920, 1080, 1920), bands[1])
        assertEquals(IntRect(0, 1900, 0, 1920), bands[2])
        assertEquals(IntRect(100, 1900, 1080, 1920), bands[3])
    }

    @Test
    fun emptyOrOffscreenHoleBlocksEverything() {
        val full = IntRect(0, 0, 1080, 1920)
        val empty = IntRect(0, 0, 0, 0)
        assertEquals(listOf(full, empty, empty, empty), computeTouchBlockerBands(Rect.Zero, canvas))
        assertEquals(
            listOf(full, empty, empty, empty),
            computeTouchBlockerBands(Rect(Offset(2000f, 2000f), Size(10f, 10f)), canvas)
        )
    }
}

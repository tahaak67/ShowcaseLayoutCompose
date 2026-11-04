package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.unit.Density
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShowcaseSpacingTest {

    private fun ShowcaseSpacing.asList(): List<Float> = listOf(
        textBaseOffset, cardInset, messageMaxWidthInset.toFloat(), circleExtraOffset, arrowStartAbove,
        arrowStartAside, arrowHeadGap, curvedArrowEndGap, curvedArrowEdgeRoom, targetTapTolerance,
        targetSafetyPadding, targetMessageWidthInset, greetingPadding,
    )

    @Test
    fun dpValuesMatchTheOldPixelsOnATypicalPhone() {
        val phone = ShowcaseSpacing.from(Density(2.75f)).asList()
        val legacy = LegacyPixelSpacing.asList()
        phone.zip(legacy).forEachIndexed { i, (actual, expected) ->
            // within 3% (or 2px for the small ones) of the pre 1.3.0 look
            assertTrue(abs(actual - expected) <= maxOf(2f, expected * 0.03f), "value #$i: $actual vs $expected")
        }
    }

    @Test
    fun spacingScalesWithDensity() {
        val oneX = ShowcaseSpacing.from(Density(1f))
        val fourX = ShowcaseSpacing.from(Density(4f))
        assertEquals(84f, oneX.textBaseOffset)
        assertEquals(336f, fourX.textBaseOffset)
        assertEquals(oneX.textBaseOffset - oneX.cardInset, oneX.customMessageGap)
    }
}

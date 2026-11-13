package ly.com.tahaben.showcase_layout_compose.domain.usecase

import ly.com.tahaben.showcase_layout_compose.model.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArrowSideTest {

    @Test
    fun theDefaultBottomArrowFollowsAMessagePlacedAbove() {
        assertEquals(Side.Top, arrowSideFacing(Side.Bottom, MessageSide.Above))
    }

    @Test
    fun aTopArrowFollowsAMessagePlacedBelow() {
        assertEquals(Side.Bottom, arrowSideFacing(Side.Top, MessageSide.Below))
    }

    @Test
    fun aMatchingSideIsKept() {
        assertEquals(Side.Top, arrowSideFacing(Side.Top, MessageSide.Above))
        assertEquals(Side.Bottom, arrowSideFacing(Side.Bottom, MessageSide.Below))
    }

    @Test
    fun leftAndRightAreKeptWhereverTheMessageIs() {
        for (side in MessageSide.entries) {
            assertEquals(Side.Left, arrowSideFacing(Side.Left, side))
            assertEquals(Side.Right, arrowSideFacing(Side.Right, side))
        }
    }

    @Test
    fun noArrowStaysNoArrow() {
        assertNull(arrowSideFacing(null, MessageSide.Above))
    }
}

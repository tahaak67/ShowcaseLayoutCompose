package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import kotlin.test.Test
import kotlin.test.assertEquals

class MessagePlacementTest {

    private val canvas = Size(1080f, 1920f)
    private val message = IntSize(300, 60)

    private fun place(
        gravity: Gravity = Gravity.Auto,
        target: Offset = Offset(100f, 500f),
        targetSize: Size = Size(200f, 50f),
        shape: TargetShape = TargetShape.RECTANGLE,
        anchorNearEdge: Boolean = false,
        isGreeting: Boolean = false,
        size: IntSize = message,
        arrow: Arrow? = null,
    ) = computeShowcaseMessagePlacement(
        messageSize = size,
        targetPosition = target,
        targetSize = targetSize,
        canvasSize = canvas,
        gravity = gravity,
        arrow = arrow,
        targetShape = shape,
        isGreeting = isGreeting,
        anchorNearEdge = anchorNearEdge,
    )

    // ---- text messages: historical behaviour ----

    @Test
    fun textGravityTopPutsTextTopAtFixedDistanceAboveTarget() {
        val placement = place(gravity = Gravity.Top)
        assertEquals(MessagePlacement(Offset(50f, 270f), MessageSide.Above), placement)
    }

    @Test
    fun textGravityBottomPutsTextBelowTarget() {
        val placement = place(gravity = Gravity.Bottom)
        assertEquals(MessagePlacement(Offset(50f, 780f), MessageSide.Below), placement)
    }

    @Test
    fun textAutoFallsBackToBottomWhenTopWouldBeOffscreen() {
        val placement = place(target = Offset(100f, 100f))
        assertEquals(MessagePlacement(Offset(50f, 380f), MessageSide.Below), placement)
    }

    @Test
    fun textAutoIgnoresMessageHeightWhenCheckingTop() {
        // a tall message still goes on top as long as its top-left fits
        val placement = place(size = IntSize(300, 600))
        assertEquals(MessageSide.Above, placement.side)
        assertEquals(270f, placement.offset.y)
    }

    @Test
    fun textGreetingStartsAtHalfHeightAndIsCenteredHorizontally() {
        val placement = place(isGreeting = true)
        assertEquals(MessagePlacement(Offset(390f, 960f), MessageSide.Center), placement)
    }

    @Test
    fun circleTargetAddsRadiusAndMarginToOffset() {
        val placement = place(gravity = Gravity.Top, targetSize = Size(100f, 100f), shape = TargetShape.CIRCLE)
        // 500 - (230 + 50 + 50)
        assertEquals(170f, placement.offset.y)
    }

    @Test
    fun messageIsPinnedToTargetEndWhenItWouldOverflowOnTheRight() {
        val placement = place(gravity = Gravity.Top, target = Offset(1000f, 500f), targetSize = Size(60f, 50f))
        assertEquals(760f, placement.offset.x)
    }

    @Test
    fun messageIsPinnedToTargetStartWhenItWouldOverflowOnTheLeft() {
        val placement = place(gravity = Gravity.Top, target = Offset(0f, 500f), targetSize = Size(60f, 50f))
        assertEquals(0f, placement.offset.x)
    }

    @Test
    fun curvedArrowCentersMessageOnScreen() {
        val placement = place(gravity = Gravity.Top, target = Offset(1000f, 500f), arrow = Arrow(curved = true))
        assertEquals(390f, placement.offset.x)
    }

    // ---- custom content: near edge anchoring ----

    @Test
    fun customTopAnchorsBottomEdgeAboveTarget() {
        val placement = place(gravity = Gravity.Top, anchorNearEdge = true)
        // 500 - 212 - 60
        assertEquals(MessagePlacement(Offset(50f, 228f), MessageSide.Above), placement)
    }

    @Test
    fun customBottomAnchorsTopEdgeBelowTarget() {
        val placement = place(gravity = Gravity.Bottom, anchorNearEdge = true)
        // 500 + 50 + 212
        assertEquals(MessagePlacement(Offset(50f, 762f), MessageSide.Below), placement)
    }

    @Test
    fun customAutoFlipsToBottomWhenWholeContentDoesNotFitAbove() {
        val placement = place(anchorNearEdge = true, size = IntSize(300, 400))
        assertEquals(MessagePlacement(Offset(50f, 762f), MessageSide.Below), placement)
    }

    @Test
    fun customAutoStaysOnTopWhenContentFits() {
        val placement = place(anchorNearEdge = true)
        assertEquals(MessageSide.Above, placement.side)
        assertEquals(228f, placement.offset.y)
    }

    @Test
    fun customContentWiderThanTargetRightEdgeIsKeptOnScreen() {
        // end half: anchoring the right edge to the target would push x negative
        val placement = place(gravity = Gravity.Top, target = Offset(585f, 1500f), targetSize = Size(300f, 100f),
            anchorNearEdge = true, size = IntSize(990, 300))
        assertEquals(0f, placement.offset.x)
        // start half: pinned to the target start, but never past the right edge
        val right = place(gravity = Gravity.Top, target = Offset(100f, 1500f), targetSize = Size(60f, 100f),
            anchorNearEdge = true, size = IntSize(990, 300))
        assertEquals(90f, right.offset.x)
    }

    @Test
    fun customGreetingIsCenteredOnBothAxes() {
        val placement = place(isGreeting = true, anchorNearEdge = true)
        assertEquals(MessagePlacement(Offset(390f, 930f), MessageSide.Center), placement)
    }

    // ---- TargetShowcaseLayout ----

    @Test
    fun targetLayoutMaxWidthMatchesHistoricalFormula() {
        assertEquals(880, targetLayoutMessageMaxWidth(canvas))
        assertEquals(1, targetLayoutMessageMaxWidth(Size(100f, 100f)))
    }

    @Test
    fun targetLayoutPlacesMessageAboveWhenItFits() {
        val center = Offset(540f, 1000f)
        val placement = computeTargetLayoutMessagePlacement(
            messageSize = message,
            itemCenter = center,
            verticalOffset = 25f,
            outer = Rect(center = center, radius = 960f),
            canvasSize = canvas,
        )
        // 1000 - 25 - 60 - 150
        assertEquals(MessagePlacement(Offset(390f, 765f), MessageSide.Above), placement)
    }

    @Test
    fun targetLayoutPlacesMessageBelowNearTheTopOfTheScreen() {
        val center = Offset(540f, 150f)
        val placement = computeTargetLayoutMessagePlacement(
            messageSize = message,
            itemCenter = center,
            verticalOffset = 25f,
            outer = Rect(center = center, radius = 960f),
            canvasSize = canvas,
        )
        // 150 + 25 + 150
        assertEquals(MessagePlacement(Offset(390f, 325f), MessageSide.Below), placement)
    }

    @Test
    fun targetLayoutKeepsMessageOnScreenHorizontally() {
        val center = Offset(1050f, 1000f)
        val placement = computeTargetLayoutMessagePlacement(
            messageSize = message,
            itemCenter = center,
            verticalOffset = 25f,
            outer = Rect(center = center, radius = 960f),
            canvasSize = canvas,
        )
        // 1080 - 300 - 150
        assertEquals(630f, placement.offset.x)
    }
}

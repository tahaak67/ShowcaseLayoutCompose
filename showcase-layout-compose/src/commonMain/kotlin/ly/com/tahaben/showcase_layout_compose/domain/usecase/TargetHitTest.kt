package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import kotlin.math.hypot
import kotlin.math.max

/**
 * Returns true if [tap] falls inside the target highlight described by [topLeft] and [size]
 * for the given [targetShape].
 *
 * Used to restrict advancing/dismissing the showcase to taps on the highlighted target.
 *
 * @param tap the tap position, in the same coordinate space as the drawn cutout.
 * @param topLeft the top-left corner of the target bounds.
 * @param size the size of the target bounds.
 * @param targetShape the shape of the target highlight.
 * @param tolerancePx expands the hit area on every side so tiny targets and finger slop still register.
 */
internal fun isTapInsideTarget(
    tap: Offset,
    topLeft: Offset,
    size: Size,
    targetShape: TargetShape,
    tolerancePx: Float = 0f,
): Boolean {
    val left = topLeft.x - tolerancePx
    val top = topLeft.y - tolerancePx
    val right = topLeft.x + size.width + tolerancePx
    val bottom = topLeft.y + size.height + tolerancePx
    return when (targetShape) {
        // The rounded-rectangle corners are approximated by the bounding box: the difference is a
        // few pixels at each corner and is irrelevant for tap intent.
        TargetShape.RECTANGLE, TargetShape.ROUNDED_RECTANGLE ->
            tap.x in left..right && tap.y in top..bottom

        TargetShape.CIRCLE -> {
            val centerX = topLeft.x + size.width / 2f
            val centerY = topLeft.y + size.height / 2f
            val radius = max(size.width, size.height) / 2f + tolerancePx
            hypot(tap.x - centerX, tap.y - centerY) <= radius
        }
    }
}

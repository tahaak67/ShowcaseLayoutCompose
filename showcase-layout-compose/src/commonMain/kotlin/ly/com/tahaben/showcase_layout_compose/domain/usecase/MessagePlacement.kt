package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import kotlin.math.max
import kotlin.math.min

/**
 *     Copyright 2026 Taha Ben Ashur (tahaak67)
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

/** Which side of the target a message ended up on. */
internal enum class MessageSide { Above, Below, Center }

/** Top-left of a message plus the side of the target it was placed on. */
internal data class MessagePlacement(val offset: Offset, val side: MessageSide)

/** Bounds of a custom message composable, reported from layout to the canvas drawing the arrow. */
internal data class PlacedMessage(val bounds: Rect, val side: MessageSide)

/** Horizontal room taken away from the canvas width when measuring a message in `ShowcaseLayout`. */
internal const val MESSAGE_MAX_WIDTH_INSET_PX = 90

/** Distance (px) between the target edge and the text top-left in `ShowcaseLayout`. */
internal const val TEXT_BASE_OFFSET_PX = 230f

/** Padding (px) the text card extends around the measured text in `ShowcaseLayout`. */
internal const val CARD_INSET_PX = 18f

/**
 * Distance (px) between the target edge and the near edge of a custom composable message. Equals
 * the visual gap a one-line text card has today, so arrows keep the same length.
 */
internal const val CUSTOM_MESSAGE_GAP_PX = TEXT_BASE_OFFSET_PX - CARD_INSET_PX

/**
 * Computes where a message goes in `ShowcaseLayout`.
 *
 * @param messageSize the measured message: the text bounds, or the custom composable's size.
 * @param targetPosition resting top-left of the target in layout space.
 * @param targetSize resting size of the target.
 * @param canvasSize size of the overlay.
 * @param isGreeting true for the greeting / index 0 step.
 * @param anchorNearEdge false keeps the historical text behaviour where [messageSize] height is
 * ignored and the returned offset is the text top-left. True (custom content) puts the message's
 * edge facing the target [CUSTOM_MESSAGE_GAP_PX] away from it, flips `Gravity.Auto` to the bottom
 * when the whole message would not fit above, and centers a greeting on both axes.
 * @param log receives human readable placement decisions.
 */
internal fun computeShowcaseMessagePlacement(
    messageSize: IntSize,
    targetPosition: Offset,
    targetSize: Size,
    canvasSize: Size,
    gravity: Gravity,
    arrow: Arrow?,
    targetShape: TargetShape,
    isGreeting: Boolean,
    anchorNearEdge: Boolean,
    log: (String) -> Unit = {},
): MessagePlacement {
    val targetY = targetPosition.y
    val targetHeight = targetSize.height
    val targetWidth = targetSize.width

    // Circle targets need extra room so the arrow can start outside the circle
    val additionalOffset = if (targetShape == TargetShape.CIRCLE) {
        maxOf(targetWidth, targetHeight) / 2 + 50
    } else {
        0f
    }

    val side: MessageSide
    val yOffset: Float
    if (isGreeting) {
        side = MessageSide.Center
        yOffset = if (anchorNearEdge) (canvasSize.height - messageSize.height) / 2 else canvasSize.height / 2
    } else if (anchorNearEdge) {
        val gap = CUSTOM_MESSAGE_GAP_PX + additionalOffset
        val topPosition = targetY - gap - messageSize.height
        val bottomPosition = targetY + targetHeight + gap
        when (gravity) {
            Gravity.Top -> {
                side = MessageSide.Above; yOffset = topPosition
            }

            Gravity.Bottom -> {
                side = MessageSide.Below; yOffset = bottomPosition
            }

            Gravity.Auto -> if (topPosition < 0) {
                log("Not enough space on top show msg on bottom")
                side = MessageSide.Below; yOffset = bottomPosition
            } else {
                log("message can be shown on top")
                side = MessageSide.Above; yOffset = topPosition
            }
        }
    } else {
        val baseOffset = TEXT_BASE_OFFSET_PX + additionalOffset
        when (gravity) {
            Gravity.Top -> {
                side = MessageSide.Above; yOffset = targetY - baseOffset
            }

            Gravity.Bottom -> {
                side = MessageSide.Below; yOffset = targetY + targetHeight + baseOffset
            }

            Gravity.Auto -> {
                val topPosition = targetY - baseOffset
                if (topPosition < 0) {
                    log("Not enough space on top show msg on bottom")
                    side = MessageSide.Below; yOffset = targetY + targetHeight + baseOffset
                } else {
                    log("message can be shown on top")
                    side = MessageSide.Above; yOffset = topPosition
                }
            }
        }
    }

    val halfWidth = canvasSize.width / 2
    // Int division on purpose: matches the historical text layout exactly
    val messageWidthHalf = messageSize.width / 2

    /*
    Determine the horizontal alignment of the message, we try to center it to the target but if
    the target is on the edge of the screen the message will get cut off, if that's the case we
    align the message Start or End to the target Start or End as appropriate
     */
    val xOffset = if (isGreeting || arrow?.curved == true) {
        halfWidth - messageWidthHalf
    } else {
        val targetX = targetPosition.x
        val additionalHorizontalOffset = if (targetShape == TargetShape.CIRCLE) {
            maxOf(targetWidth, targetHeight) / 2 * 0.3f
        } else {
            0f
        }
        val targetXMiddlePoint = targetX + (targetWidth / 2)
        when {
            (targetXMiddlePoint < halfWidth) -> {
                log("layout on start half")
                if ((targetXMiddlePoint - messageWidthHalf) < 0) {
                    targetX
                } else if (arrow?.targetFrom == Side.Right && targetShape == TargetShape.CIRCLE) {
                    targetXMiddlePoint - messageWidthHalf - additionalHorizontalOffset
                } else {
                    targetXMiddlePoint - messageWidthHalf
                }
            }

            (targetXMiddlePoint == halfWidth) -> {
                log("layout in middle")
                targetXMiddlePoint - messageWidthHalf
            }

            else -> {
                log("layout on end half")
                if (targetXMiddlePoint + messageWidthHalf > canvasSize.width) {
                    targetX + targetWidth - messageSize.width
                } else if (arrow?.targetFrom == Side.Left && targetShape == TargetShape.CIRCLE) {
                    targetXMiddlePoint - messageWidthHalf + additionalHorizontalOffset
                } else {
                    targetXMiddlePoint - messageWidthHalf
                }
            }
        }
    }
    // Custom content can be wider than the target's right edge allows; keep it on screen.
    // (Text keeps its historical placement untouched.)
    val clampedX = if (anchorNearEdge && messageSize.width <= canvasSize.width) {
        xOffset.coerceIn(0f, canvasSize.width - messageSize.width)
    } else {
        xOffset
    }
    return MessagePlacement(Offset(clampedX, yOffset), side)
}

/** Safety padding (px) used by `TargetShowcaseLayout` when placing a message next to a target. */
internal const val TARGET_LAYOUT_SAFETY_PADDING_PX = 100f

/** Maximum width (px) of a message placed next to a target in `TargetShowcaseLayout`. */
internal fun targetLayoutMessageMaxWidth(canvasSize: Size): Int {
    val outerDiameter = max(canvasSize.width, canvasSize.height)
    val calculatedWidth = min(
        canvasSize.width.toInt() - 200,
        (outerDiameter - 4 * TARGET_LAYOUT_SAFETY_PADDING_PX).toInt()
    )
    // Ensure the width is always positive to avoid a Constraints exception
    return max(1, calculatedWidth)
}

/**
 * Computes where a message goes in `TargetShowcaseLayout` for a (non greeting) target, clamped
 * inside both the screen and the outer circle. [gravity] picks the side: `Top` or `Bottom` when
 * the message fits there (the other side otherwise), `Auto` prefers above when it fits.
 *
 * @param messageSize the measured message: the text bounds, or the custom composable's size.
 * @param itemCenter center of the highlighted target.
 * @param verticalOffset distance from [itemCenter] to the target's top/bottom edge.
 * @param outer bounds of the outer circle around the target.
 * @param canvasSize size of the overlay.
 * @param gravity the side of the target the message asked for.
 */
internal fun computeTargetLayoutMessagePlacement(
    messageSize: IntSize,
    itemCenter: Offset,
    verticalOffset: Float,
    outer: Rect,
    canvasSize: Size,
    gravity: Gravity = Gravity.Auto,
    log: (String) -> Unit = {},
): MessagePlacement {
    val safetyPadding = TARGET_LAYOUT_SAFETY_PADDING_PX
    val messageHeight = messageSize.height
    val messageWidth = messageSize.width

    // Where the message would start on either side, and whether it stays on screen there
    val textAboveY = itemCenter.y - verticalOffset - messageHeight - safetyPadding * 1.5f
    val textBelowY = itemCenter.y + verticalOffset + safetyPadding * 1.5f
    val fitsAbove = textAboveY >= safetyPadding
    val fitsBelow = textBelowY + messageHeight <= canvasSize.height - safetyPadding
    val isTextAbove = when (gravity) {
        Gravity.Top -> fitsAbove || !fitsBelow
        Gravity.Bottom -> !fitsBelow && fitsAbove
        Gravity.Auto -> fitsAbove
    }

    val textY = if (!isTextAbove) {
        log("Text would be outside screen bounds if displayed above. Displaying below target instead.")
        // Make sure the message stays within the outer circle when displayed below
        min(textBelowY, outer.bottom - messageHeight - safetyPadding * 1.5f)
    } else {
        // Make sure the message stays within the outer circle when displayed above
        max(textAboveY, outer.top + safetyPadding * 1.5f)
    }

    if (isTextAbove && textY < outer.top + safetyPadding * 2) {
        log("Text is very close to the top edge of the outer circle.")
    } else if (!isTextAbove && textY + messageHeight > outer.bottom - safetyPadding * 2) {
        log("Text is very close to the bottom edge of the outer circle.")
    }

    // Ensure the message doesn't extend beyond the left or right edges of the screen and outer circle
    var textX = itemCenter.x - messageWidth / 2
    val horizontalSafetyPadding = safetyPadding * 1.5f

    if (textX < horizontalSafetyPadding) {
        log("Text would extend beyond left edge of screen. Adjusting position.")
        textX = horizontalSafetyPadding
    }
    if (textX + messageWidth > canvasSize.width - horizontalSafetyPadding) {
        log("Text would extend beyond right edge of screen. Adjusting position.")
        textX = canvasSize.width - messageWidth - horizontalSafetyPadding
    }
    if (textX < outer.left + horizontalSafetyPadding) {
        log("Text would extend beyond left edge of outer circle. Adjusting position.")
        textX = outer.left + horizontalSafetyPadding
    }
    if (textX + messageWidth > outer.right - horizontalSafetyPadding) {
        log("Text would extend beyond right edge of outer circle. Adjusting position.")
        textX = outer.right - messageWidth - horizontalSafetyPadding
    }
    // If after all adjustments the message still doesn't fit within screen bounds,
    // prioritize keeping it on screen over keeping it within the outer circle
    if (textX < horizontalSafetyPadding) {
        textX = horizontalSafetyPadding
        log("Text still extends beyond left edge of screen. Forcing on-screen position.")
    }
    if (textX + messageWidth > canvasSize.width - horizontalSafetyPadding) {
        textX = canvasSize.width - messageWidth - horizontalSafetyPadding
        log("Text still extends beyond right edge of screen. Forcing on-screen position.")
    }

    return MessagePlacement(
        Offset(textX, textY),
        if (isTextAbove) MessageSide.Above else MessageSide.Below
    )
}

package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

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

/**
 * Distances, in px, that the layouts use to place messages and arrows around a target.
 *
 * They are defined in dp and converted with the screen density ([from]), so the spacing looks the
 * same on a 1x desktop window and on a 4x tablet. Before 1.3.0 they were fixed pixel values; the dp
 * values are calibrated so a typical ~2.75x phone looks the same as it did then. The old pixel value
 * is noted on each property.
 */
internal data class ShowcaseSpacing(
    /** `ShowcaseLayout`: gap between the target edge and the text top-left (230px). */
    val textBaseOffset: Float,
    /** `ShowcaseLayout`: padding the text card extends around the measured text (18px). */
    val cardInset: Float,
    /** `ShowcaseLayout`: horizontal room taken off the canvas width when measuring a message (90px). */
    val messageMaxWidthInset: Int,
    /** `ShowcaseLayout`: extra gap for circle targets so the arrow starts outside the circle (50px). */
    val circleExtraOffset: Float,
    /** `ShowcaseLayout`: where a straight `Side.Top` arrow starts above a non circle target (180px). */
    val arrowStartAbove: Float,
    /** `ShowcaseLayout`: where a straight `Side.Bottom/Left/Right` arrow starts for a non circle target (200px). */
    val arrowStartAside: Float,
    /** `ShowcaseLayout`: gap between the arrow head and the target edge, added to the head size (25px). */
    val arrowHeadGap: Float,
    /** `ShowcaseLayout`: how far beside the target a curved arrow ends (50px). */
    val curvedArrowEndGap: Float,
    /** `ShowcaseLayout`: room a curved arrow needs right of the target before it ends on its left instead (80px). */
    val curvedArrowEdgeRoom: Float,
    /** Both layouts: extra hit area around the target for `advanceOnTargetTapOnly` (16px). */
    val targetTapTolerance: Float,
    /** `TargetShowcaseLayout`: padding around a message placed next to a target (100px). */
    val targetSafetyPadding: Float,
    /** `TargetShowcaseLayout`: horizontal room taken off the canvas width for a message (200px). */
    val targetMessageWidthInset: Float,
    /** `TargetShowcaseLayout`: padding of the background behind a text greeting (40px). */
    val greetingPadding: Float,
) {
    /** `ShowcaseLayout`: gap between the target edge and the near edge of a custom composable message. */
    val customMessageGap: Float get() = textBaseOffset - cardInset

    companion object {
        fun from(density: Density): ShowcaseSpacing = with(density) {
            ShowcaseSpacing(
                textBaseOffset = 84.dp.toPx(),
                cardInset = 6.5.dp.toPx(),
                messageMaxWidthInset = 33.dp.roundToPx(),
                circleExtraOffset = 18.dp.toPx(),
                arrowStartAbove = 65.dp.toPx(),
                arrowStartAside = 73.dp.toPx(),
                arrowHeadGap = 9.dp.toPx(),
                curvedArrowEndGap = 18.dp.toPx(),
                curvedArrowEdgeRoom = 29.dp.toPx(),
                targetTapTolerance = 6.dp.toPx(),
                targetSafetyPadding = 36.dp.toPx(),
                targetMessageWidthInset = 73.dp.toPx(),
                greetingPadding = 14.5.dp.toPx(),
            )
        }
    }
}

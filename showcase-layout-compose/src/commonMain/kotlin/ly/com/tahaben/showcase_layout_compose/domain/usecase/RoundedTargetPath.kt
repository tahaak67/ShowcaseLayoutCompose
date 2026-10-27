package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import kotlin.math.max

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
 * The corner radius that fits a target of [size]: never more than half its width or height.
 *
 * A radius larger than that makes the four corner arcs overlap and cross. On screen that showed as
 * a four-pointed star while a target animated up from zero size, and as a notched, not quite round
 * highlight on small targets such as a 40dp icon button with a 28dp radius.
 */
internal fun fittedCornerRadius(cornerRadiusPx: Float, size: Size): Float =
    max(0f, minOf(cornerRadiusPx, size.width / 2f, size.height / 2f))

/** Adds [rect] to the path as a rounded rectangle with a radius that fits it, see [fittedCornerRadius]. */
internal fun Path.addRoundedTarget(rect: Rect, cornerRadiusPx: Float) {
    val radius = fittedCornerRadius(cornerRadiusPx, rect.size)
    addRoundRect(RoundRect(rect, CornerRadius(radius, radius)))
}

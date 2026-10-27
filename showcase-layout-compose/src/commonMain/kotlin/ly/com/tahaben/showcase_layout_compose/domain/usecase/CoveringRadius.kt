package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.sqrt

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
 * Radius of a circle centred on [center] that covers all of [canvasSize]: the distance from the
 * centre to the farthest corner. `TargetShowcaseLayout` dims this circle around the target, so no
 * part of the layout is left undimmed wherever the target sits.
 */
internal fun coveringRadius(center: Offset, canvasSize: Size): Float {
    val dx = max(center.x, canvasSize.width - center.x)
    val dy = max(center.y, canvasSize.height - center.y)
    return sqrt(dx * dx + dy * dy)
}

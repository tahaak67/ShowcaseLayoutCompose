package ly.com.tahaben.showcase_layout_compose.domain.usecase

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.math.ceil
import kotlin.math.floor

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
 * Splits a [canvas] into the four rectangles (top, bottom, left, right) that surround [hole].
 *
 * The result always has four entries in that order; a band that would be empty has a zero
 * width or height. The hole is rounded *outwards* to whole pixels and clamped to the canvas, so
 * rounding never shrinks it below the real target bounds. A hole outside the canvas, or empty,
 * yields a top band covering the whole canvas and three empty bands.
 *
 * Used by the overlay while a step is locked: each band gets a pointer-input node that swallows
 * touches, and the hole has none, so touches there fall through to the target underneath.
 */
internal fun computeTouchBlockerBands(hole: Rect, canvas: IntSize): List<IntRect> {
    val empty = IntRect(0, 0, 0, 0)
    val full = IntRect(0, 0, canvas.width, canvas.height)
    if (hole.isEmpty || canvas.width <= 0 || canvas.height <= 0) {
        return listOf(full, empty, empty, empty)
    }
    val left = floor(hole.left).toInt().coerceIn(0, canvas.width)
    val top = floor(hole.top).toInt().coerceIn(0, canvas.height)
    val right = ceil(hole.right).toInt().coerceIn(0, canvas.width)
    val bottom = ceil(hole.bottom).toInt().coerceIn(0, canvas.height)
    if (right <= left || bottom <= top) {
        return listOf(full, empty, empty, empty)
    }
    return listOf(
        /* top */ IntRect(0, 0, canvas.width, top),
        /* bottom */ IntRect(0, bottom, canvas.width, canvas.height),
        /* left */ IntRect(0, top, left, bottom),
        /* right */ IntRect(right, top, canvas.width, bottom),
    )
}

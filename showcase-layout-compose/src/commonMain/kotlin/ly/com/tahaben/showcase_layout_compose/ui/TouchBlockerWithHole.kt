package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import ly.com.tahaben.showcase_layout_compose.domain.usecase.computeTouchBlockerBands

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

private const val BAND_COUNT = 4

/**
 * An invisible, full-size layer that swallows touches everywhere except inside [hole].
 *
 * It is four [Box]es (top, bottom, left and right bands) each carrying a pointer-input node, laid
 * out around the hole; the hole itself has no node at all. Compose's hit test therefore finds
 * nothing of the overlay there and continues to the content composed underneath, which receives
 * the whole gesture as if the overlay did not exist: a text field gets focus (and the soft
 * keyboard), a button clicks, a list scrolls.
 *
 * [hole] is read inside the measure pass, so reading animated snapshot state in it re-lays out
 * the bands as the target moves. [onTap] receives taps on the bands in this layer's coordinates.
 */
@Composable
internal fun TouchBlockerWithHole(
    hole: () -> Rect,
    onTap: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            repeat(BAND_COUNT) {
                Box(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures { onTap(it) }
                    }
                )
            }
        }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val bands = computeTouchBlockerBands(hole(), IntSize(width, height))
        val placeables = measurables.mapIndexed { i, measurable ->
            val band = bands[i]
            measurable.measure(Constraints.fixed(band.width.coerceAtLeast(0), band.height.coerceAtLeast(0)))
        }
        layout(width, height) {
            placeables.forEachIndexed { i, placeable ->
                val band = bands[i]
                placeable.place(band.left, band.top)
            }
        }
    }
}

package ly.com.tahaben.showcase_layout_compose.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 *     Copyright 2023 Taha Ben Ashur (tahaak67)
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
 *
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 15,Sep,2022
 */

/**
 * Arrow
 *
 * An arrow drawn from the message to the target. Arrows are drawn by `ShowcaseLayout` only;
 * `TargetShowcaseLayout` ignores [ShowcaseMsg.arrow].
 *
 * @param targetFrom the side of the target the arrow comes from, Ex: Side.Right. Since 1.3.0 a
 * `Side.Top` or `Side.Bottom` arrow follows the message: it comes from whichever side the message
 * ended up on, so `Gravity.Auto` and a gravity that contradicts this value still give a matching
 * arrow. `Side.Left` and `Side.Right` are always kept.
 * @param curved draw a curvy arrow from the middle of the screen to target, works best if the target is on the right/left edge of screen.
 * @param animationDuration the time taken to animate the arrow in milliseconds.
 * @param head The shape of the arrow head, if null only the line will be drawn without the arrow head.
 * @param headSize size of the arrow head, default is 9.dp. Before 1.3.0 this was a `Float` in
 * pixels (default 25f); divide an old value by ~2.75 for the same look on a typical phone.
 * @param color color of the arrow.
 * @param animSize animate the head size from 0 to [headSize] when entering and reverse it on exit.
 **/

data class Arrow(
    val targetFrom: Side = Side.Bottom,
    val curved: Boolean = false,
    val animationDuration: Int = 1000,
    val head: Head? = Head.TRIANGLE,
    val headSize: Dp = 9.dp,
    val color: Color = Color.White,
    val animSize: Boolean = true
)

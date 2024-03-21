package ly.com.tahaben.showcase_layout_compose.model

import androidx.compose.ui.graphics.Color

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
 * @param targetFrom the direction from where the arrow will point at the target, Ex: Side.Right.
 * @param curved draw a curvy arrow from the middle of the screen to target, works best if the target is on the right/left edge of screen.
 * @param animationDuration the time taken to animate the arrow in milliseconds.
 * @param hasHead if false only the line will be drawn without the arrow head.
 * @param headSize size of the arrow head default is 25.
 * @param color color of the arrow.
 **/

data class Arrow(
    val targetFrom: Side = Side.Bottom,
    val curved: Boolean = false,
    val animationDuration: Int = 1000,
    val hasHead: Boolean = true,
    val headSize: Float = 25f,
    val color: Color = Color.White
)

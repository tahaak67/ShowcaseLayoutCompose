package ly.com.tahaben.showcase_layout_compose.model

import androidx.compose.ui.graphics.Color

/**
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 15,Sep,2022
 */

/**
 * Arrow
 *
 * @param targetFrom the direction from where the arrow will point at the target, Ex: Side.Right.
 * @param animateFromMsg draw a curvy arrow from the middle of the screen to target, works best if the target is on the right/left edge of screen.
 * @param animationDuration the time taken to animate the arrow in milliseconds.
 * @param color color of the arrow.
 **/

data class Arrow(
    val targetFrom: Side = Side.Bottom,
    val animateFromMsg: Boolean = false,
    val animationDuration: Int = 2000,
    val hasHead: Boolean = true,
    val color: Color = Color.White
)

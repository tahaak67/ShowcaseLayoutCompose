package ly.com.tahaben.showcase_layout_compose.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
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
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 1,August,2022
 */

/**
 * ShowcaseMsg
 *
 * @param text the text of the message.
 * @param textStyle text style.
 * @param msgBackground background color for the text, default is Transparent. Note: in `TargetShowcaseLayout` this also acts as the overlay color for this step and takes precedence over `ShowcaseLayoutDefaults.Colors.overlayColor`.
 * @param roundedCorner the corner radius value in dp.
 * @param gravity display the message below or on top of target.
 * @param arrow add an arrow.
 * @param enterAnim enter animation.
 * @param exitAnim exit animation.
 * @param content an optional custom composable to show instead of the text, for example an image
 * or a card with an icon and a button. Use the trailing-lambda constructor to create one:
 * `ShowcaseMsg(arrow = Arrow()) { Card { ... } }`. The composable owns its own look, so when `content`
 * is set [text], [textStyle] and [roundedCorner] are ignored; [msgBackground] is ignored by
 * `ShowcaseLayout` but still acts as the overlay color in `TargetShowcaseLayout`. [gravity], [arrow],
 * [enterAnim] and [exitAnim] keep working and the arrow points at the composable. The lambda receives
 * a [ShowcaseMsgScope] so buttons inside it can call `next()`, `previous()` or `skip()`.
 * Native interop views (e.g. `UIKitView`) inside `content` are not supported: they ignore the fade
 * animation and swallow touches.
 **/
data class ShowcaseMsg(
    val text: AnnotatedString,
    val textStyle: TextStyle = TextStyle(color = Color.Black),
    val msgBackground: Color? = null,
    val roundedCorner: Dp = 0.dp,
    val gravity: Gravity = Gravity.Auto,
    val arrow: Arrow? = null,
    val enterAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    val exitAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    val content: (@Composable ShowcaseMsgScope.() -> Unit)? = null,
) {
    constructor(
        text: String,
        textStyle: TextStyle = TextStyle(color = Color.Black),
        msgBackground: Color? = null,
        roundedCorner: Dp = 0.dp,
        gravity: Gravity = Gravity.Auto,
        arrow: Arrow? = null,
        enterAnim: MsgAnimation = MsgAnimation.FadeInOut(),
        exitAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    ) : this(
        AnnotatedString(text),
        textStyle,
        msgBackground,
        roundedCorner,
        gravity,
        arrow,
        enterAnim,
        exitAnim
    )

    /**
     * Creates a message whose body is a custom composable instead of text.
     *
     * ```
     * ShowcaseMsg(gravity = Gravity.Bottom, arrow = Arrow(color = Color.White)) {
     *     Card { Text("Tap here to open the menu") }
     * }
     * ```
     *
     * @param gravity display the message below or on top of target.
     * @param arrow add an arrow pointing from the composable to the target.
     * @param enterAnim enter animation.
     * @param exitAnim exit animation.
     * @param msgBackground only used by `TargetShowcaseLayout`, where it acts as the overlay color
     * for this step; `ShowcaseLayout` ignores it because the composable draws its own background.
     * @param content the composable to show; it receives a [ShowcaseMsgScope].
     */
    constructor(
        gravity: Gravity = Gravity.Auto,
        arrow: Arrow? = null,
        enterAnim: MsgAnimation = MsgAnimation.FadeInOut(),
        exitAnim: MsgAnimation = MsgAnimation.FadeInOut(),
        msgBackground: Color? = null,
        content: @Composable ShowcaseMsgScope.() -> Unit,
    ) : this(
        text = AnnotatedString(""),
        msgBackground = msgBackground,
        gravity = gravity,
        arrow = arrow,
        enterAnim = enterAnim,
        exitAnim = exitAnim,
        content = content
    )
}

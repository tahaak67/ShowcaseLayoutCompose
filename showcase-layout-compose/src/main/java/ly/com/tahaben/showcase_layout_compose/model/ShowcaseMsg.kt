package ly.com.tahaben.showcase_layout_compose.model

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
 * @param msgBackground background color for the text, default is Transparent.
 * @param roundedCorner the corner radius value in dp.
 * @param gravity display the message below or on top of target.
 * @param arrow add an arrow.
 * @param enterAnim enter animation.
 * @param exitAnim exit animation.
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


}

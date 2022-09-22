package ly.com.tahaben.showcase_layout_compose.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
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
 **/
data class ShowcaseMsg(
    val text: String,
    val textStyle: TextStyle = TextStyle(color = Color.Black),
    val msgBackground: Color? = null,
    val roundedCorner: Dp = 0.dp,
    val gravity: Gravity = Gravity.Auto,
    val arrow: Arrow? = null
)

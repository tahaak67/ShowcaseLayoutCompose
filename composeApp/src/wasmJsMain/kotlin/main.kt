import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.CanvasBasedWindow
import showcase_layout_compose_kmp.composeapp.generated.resources.Res

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        val fontFamilyResolver = LocalFontFamilyResolver.current

        LaunchedEffect(Unit) {
            val notoEmojisBytes = Res.readBytes("/font/noto_color_emoji_regular.ttf")
            val fontFamily = FontFamily(listOf(Font("NotoColorEmoji", notoEmojisBytes)))
            fontFamilyResolver.preload(fontFamily)
        }
        App(openUrl = UrlLauncherWeb()::openUrl , onWebLoadFinish = ::onLoadFinished)
    }
}
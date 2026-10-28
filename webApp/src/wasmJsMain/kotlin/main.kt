import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import showcase_layout_compose_kmp.shared.generated.resources.Res

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "ComposeTarget"){
        val fontFamilyResolver = LocalFontFamilyResolver.current

        LaunchedEffect(Unit) {
            val notoEmojisBytes = Res.readBytes("/font/noto_color_emoji_regular.ttf")
            val fontFamily = FontFamily(listOf(Font("NotoColorEmoji", notoEmojisBytes)))
            fontFamilyResolver.preload(fontFamily)
        }
        // index.html?scenario=banking&autoplay=1&layout=showcase opens a scenario directly, runs its
        // tour hands-free and picks the layout (target | showcase)
        val scenarioParam = demoParam("scenario")
        App(
            openUrl = UrlLauncherWeb()::openUrl,
            onWebLoadFinish = ::onLoadFinished,
            startScenario = Scenario.entries.firstOrNull { it.name.equals(scenarioParam, ignoreCase = true) },
            autoPlay = demoParam("autoplay") == "1",
            targetLayout = when (demoParam("layout")) { "target" -> true; "showcase" -> false; else -> null }
        )
    }
}
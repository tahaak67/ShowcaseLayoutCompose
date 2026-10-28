import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import scenarios.banking.BankingScreen
import scenarios.chat.ChatScreen
import scenarios.music.MusicScreen

/**
 * Entry point of the demo on every platform. Shows the scenario picker first; each scenario is a
 * self-contained screen that wraps its UI in a showcase layout.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
/**
 * @param startScenario open this scenario right away instead of the picker (deep links on the web).
 * @param autoPlay when a start scenario is given, run its tour hands-free.
 * @param targetLayout force TargetShowcaseLayout (true) or ShowcaseLayout (false) for the start scenario.
 */
fun App(
    openUrl: (String) -> Boolean,
    onWebLoadFinish: () -> Unit = {},
    startScenario: Scenario? = null,
    autoPlay: Boolean = false,
    targetLayout: Boolean? = null,
) {
    LaunchedEffect(Unit) { onWebLoadFinish() }

    var scenario by remember { mutableStateOf(startScenario) }
    val backToPicker = { scenario = null }

    BackHandler(enabled = scenario != null) { backToPicker() }

    Crossfade(targetState = scenario) { current ->
        when (current) {
            null -> ScenarioPickerScreen(openUrl = openUrl, onOpen = { scenario = it })
            Scenario.BANKING -> BankingScreen(onBack = backToPicker, autoPlay = autoPlay, targetLayout = targetLayout ?: true)
            Scenario.PLAYGROUND -> PlaygroundScreen(openUrl = openUrl, onBack = backToPicker)
            Scenario.MUSIC -> MusicScreen(onBack = backToPicker, autoPlay = autoPlay, targetLayout = targetLayout ?: true)
            Scenario.CHAT -> ChatScreen(onBack = backToPicker, autoPlay = autoPlay, targetLayout = targetLayout ?: false)
        }
    }
}

@Composable
fun AppPreview() {
    App(openUrl = { false })
}

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun App(openUrl: (String) -> Boolean, onWebLoadFinish: () -> Unit = {}) {
    LaunchedEffect(Unit) { onWebLoadFinish() }

    var scenario by remember { mutableStateOf<Scenario?>(null) }
    val backToPicker = { scenario = null }

    BackHandler(enabled = scenario != null) { backToPicker() }

    Crossfade(targetState = scenario) { current ->
        when (current) {
            null -> ScenarioPickerScreen(openUrl = openUrl, onOpen = { scenario = it })
            Scenario.BANKING -> BankingScreen(onBack = backToPicker)
            Scenario.PLAYGROUND -> PlaygroundScreen(openUrl = openUrl, onBack = backToPicker)
            Scenario.MUSIC -> MusicScreen(onBack = backToPicker)
            Scenario.CHAT -> ChatScreen(onBack = backToPicker)
        }
    }
}

@Composable
fun AppPreview() {
    App(openUrl = { false })
}

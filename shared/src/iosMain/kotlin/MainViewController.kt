import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App(openUrl = UrlLauncherIos()::openUrl) }
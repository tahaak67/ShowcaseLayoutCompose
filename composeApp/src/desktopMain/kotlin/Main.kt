import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "slc test") {
        App(openUrl = UrlLuancherDesktop()::openUrl)
    }
}
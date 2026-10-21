import java.awt.Desktop
import java.net.URI

class UrlLuancherDesktop(): UrlLauncher{
    override fun openUrl(url: String): Boolean {
        return try {
            Desktop.getDesktop().browse(URI(url))
            true
        } catch (e: UnsupportedOperationException) {

            false
        }
    }

}
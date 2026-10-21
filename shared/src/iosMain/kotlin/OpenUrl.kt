import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class UrlLauncherIos: UrlLauncher{
    override fun openUrl(url: String): Boolean{
        val nsurl = NSURL.URLWithString(url) ?: return false
        return if (UIApplication.sharedApplication.canOpenURL(nsurl)) {
            UIApplication.sharedApplication.openURL(nsurl)
            true
        } else {
            // Handle case where the app can't open the URL (optional)
            false
        }
    }
}

 
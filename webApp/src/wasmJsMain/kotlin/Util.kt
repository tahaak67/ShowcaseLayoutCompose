external fun openUrlWeb(url: String)
class UrlLauncherWeb() : UrlLauncher {
    override fun openUrl(url: String): Boolean {
        return try {
            openUrlWeb(url)
            true
        } catch (ex: Exception) {

            false
        }
    }
}

external fun onLoadFinished()

/** A query parameter of the page URL, or an empty string. */
external fun demoParam(name: String): String
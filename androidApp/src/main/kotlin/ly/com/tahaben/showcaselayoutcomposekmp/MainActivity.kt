package ly.com.tahaben.showcaselayoutcomposekmp

import App
import UrlLauncherAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val urlLauncherAndroid = UrlLauncherAndroid(applicationContext)
        setContent {
            App(openUrl = urlLauncherAndroid::openUrl)
        }
    }
}

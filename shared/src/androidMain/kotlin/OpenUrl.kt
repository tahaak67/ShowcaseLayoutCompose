import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat


class UrlLauncherAndroid(private val context: Context): UrlLauncher{

    override fun openUrl(url: String): Boolean{
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, intent, null)
            true
        }catch (e: Exception){

            false
        }
    }
}
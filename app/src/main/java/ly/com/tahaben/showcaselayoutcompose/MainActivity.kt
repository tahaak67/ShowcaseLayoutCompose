package ly.com.tahaben.showcaselayoutcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ly.com.tahaben.showcaselayoutcompose.ui.AboutScreen
import ly.com.tahaben.showcaselayoutcompose.ui.MainScreen
import ly.com.tahaben.showcaselayoutcompose.ui.theme.ShowcaseLayoutComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            ShowcaseLayoutComposeTheme {
                NavHost(
                    navController = navController,
                    startDestination = "Home"
                ) {
                    composable("Home") {
                        MainScreen(
                            tip = "Hi there, this is just some text.",
                            isGrayscaleEnabled = true,
                            isInfiniteScrollBlockerEnabled = false,
                            isNotificationFilterEnabled = true,
                            navigateToAbout = { navController.navigate("About") }
                        )
                    }
                    composable("About") {
                        AboutScreen(onNavigateUp = { navController.navigateUp() })
                    }
                }

            }
        }
    }
}
package ly.com.tahaben.showcaselayoutcompose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ly.com.tahaben.showcaselayoutcompose.ui.MainScreen
import ly.com.tahaben.showcaselayoutcompose.ui.theme.ShowcaseLayoutComposeTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 15,Sep,2022
 */
class ShowcaseE2E {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {

        composeRule.setContent {
            ShowcaseLayoutComposeTheme {
                MainScreen(
                    tip = "Hi there, this is just some text.",
                    isGrayscaleEnabled = true,
                    isInfiniteScrollBlockerEnabled = false,
                    isNotificationFilterEnabled = true,
                    navigateToAbout = { }
                )
            }
        }
    }

    @Test
    fun showcase_displayed_and_responds_to_taps() {
        //Tests that showcase layout is displayed and switches to next target correctly on tapping
        //finally makes sure showcase layout disappears after last target
        composeRule
            .onNodeWithTag("canvas")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithText("Track your phone usage from here")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithText("From drop down menu, you can access the About app screen!")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithText("Useful tip tho :P")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .assertDoesNotExist()
    }
}
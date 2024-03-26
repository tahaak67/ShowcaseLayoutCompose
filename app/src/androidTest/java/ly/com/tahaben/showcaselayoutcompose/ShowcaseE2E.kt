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

    /**
    Tests that showcase layout is displayed and switches to next target correctly on tapping
    finally makes sure showcase layout disappears after last target
     */
    @Test
    fun showcase_displayed_and_responds_to_taps() {
        /**
        Wait 500 milliseconds before testing since we set the showcase layout to show after
        a delay see LaunchedEffect block in [MainScreen]
         */
        composeRule.mainClock.advanceTimeBy(500)
        composeRule
            .onNodeWithTag("canvas")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .assertDoesNotExist()
        composeRule
            .onNodeWithText("Usage")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .assertDoesNotExist()
        composeRule
            .onNodeWithText("Hello")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag("canvas")
            .performClick()
        composeRule
            .onNodeWithTag("canvas")
            .assertDoesNotExist()
    }
}
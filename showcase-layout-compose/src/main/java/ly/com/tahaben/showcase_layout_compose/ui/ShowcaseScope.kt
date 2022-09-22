package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.runtime.Composable
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg

/**
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 1,August,2022
 */

interface ShowcaseScope {

    /**
     * Showcase ShowcaseScope
     *
     * @param k key of current item MUST start at 1 and increment by 1 for each composable inside this ShowcaseLayout.
     * @param message a message to display when showcasing this composable.
     **/
    @Composable
    fun Showcase(k: Int, message: ShowcaseMsg?, itemContent: @Composable () -> Unit)

}
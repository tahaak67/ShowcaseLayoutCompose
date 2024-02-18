package ly.com.tahaben.showcase_layout_compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg

/**
 *     Copyright 2023 Taha Ben Ashur (tahaak67)
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * Created by Taha Ben Ashur (https://github.com/tahaak67) on 1,August,2022
 */

interface ShowcaseScope {

    /**
     * Showcase ShowcaseScope
     *
     * @param index key of current item MUST start at 1 and increment by 1 for each composable inside this ShowcaseLayout.
     * @param message a message to display when showcasing this composable.
     **/
    @Composable
    fun Showcase(index: Int, message: ShowcaseMsg?, itemContent: @Composable () -> Unit)

    /**
     * Showcase ShowcaseScope
     *
     * @param index key of current item **MUST** start at 1 and increment by 1 for each composable inside this ShowcaseLayout.
     * @param message a message to display when showcasing this composable.
     **/
    fun Modifier.showcase(index: Int, message: ShowcaseMsg?): Modifier


    var showcaseEventListener: ShowcaseEventListener?

    /**
     * registerEventListener ShowcaseScope
     *
     * @param eventListener the [ShowcaseEventListener] to use.
     **/
    fun registerEventListener(eventListener: ShowcaseEventListener)

    suspend fun showcaseItem(index: Int)
    suspend fun showcaseItemFinished()

}
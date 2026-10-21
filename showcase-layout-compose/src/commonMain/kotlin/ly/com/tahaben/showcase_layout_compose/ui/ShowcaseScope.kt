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
     * @param canProceed when false this step is **locked**: tapping the overlay no longer advances or
     * dismisses it, touches inside the target's bounds reach the target itself (so a text field gets
     * focus and the keyboard, a button clicks, ...) and the showcase advances by itself as soon as this
     * becomes true. Read it from snapshot state, e.g. `var done by remember { mutableStateOf(false) }`.
     * `next()`, `previous()` and `skip()` from a custom message are not blocked. Defaults to true.
     * @param onProceed called once when the showcase leaves this step, whatever ends it: a tap,
     * `next()`/`previous()`/`skip()`, the condition of a locked step being met, the last step
     * finishing, or a single item being dismissed. Use it to clean up after the step, for example
     * to hide the soft keyboard a locked text-field step opened: `LocalSoftwareKeyboardController`.
     **/
    @Composable
    fun Showcase(
        index: Int,
        message: ShowcaseMsg?,
        canProceed: Boolean = true,
        onProceed: (() -> Unit)? = null,
        itemContent: @Composable () -> Unit
    )

    /**
     * Showcase ShowcaseScope
     *
     * @param index key of current item **MUST** start at 1 and increment by 1 for each composable inside this ShowcaseLayout.
     * @param message a message to display when showcasing this composable.
     * @param canProceed when false this step is **locked**: tapping the overlay no longer advances or
     * dismisses it, touches inside the target's bounds reach the target itself (so a text field gets
     * focus and the keyboard, a button clicks, ...) and the showcase advances by itself as soon as this
     * becomes true. Read it from snapshot state, e.g. `var done by remember { mutableStateOf(false) }`.
     * `next()`, `previous()` and `skip()` from a custom message are not blocked. Defaults to true.
     * @param onProceed called once when the showcase leaves this step, whatever ends it: a tap,
     * `next()`/`previous()`/`skip()`, the condition of a locked step being met, the last step
     * finishing, or a single item being dismissed. Use it to clean up after the step, for example
     * to hide the soft keyboard a locked text-field step opened: `LocalSoftwareKeyboardController`.
     **/
    fun Modifier.showcase(
        index: Int,
        message: ShowcaseMsg?,
        canProceed: Boolean = true,
        onProceed: (() -> Unit)? = null
    ): Modifier


    var showcaseEventListener: ShowcaseEventListener?

    /**
     * registerEventListener ShowcaseScope
     *
     * @param eventListener the [ShowcaseEventListener] to use.
     **/
    fun registerEventListener(eventListener: ShowcaseEventListener)

    suspend fun showcaseItem(index: Int)
    suspend fun showGreeting(message: ShowcaseMsg)
}
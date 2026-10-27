package ly.com.tahaben.showcase_layout_compose.model

/**
 *     Copyright 2026 Taha Ben Ashur (tahaak67)
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
 */

/**
 * Receiver of a custom [ShowcaseMsg.content] composable. Lets the content drive the showcase,
 * for example from a "Next", "Back" or "Skip" button inside a card.
 *
 * Taps on parts of the content that are not clickable still fall through to the showcase layout
 * and advance it exactly like a tap anywhere else on the overlay.
 */
interface ShowcaseMsgScope {

    /**
     * Advance to the next target, or dismiss the message when showing a single item
     * ([ly.com.tahaben.showcase_layout_compose.ui.ShowcaseScope.showcaseItem]) or a single greeting
     * ([ly.com.tahaben.showcase_layout_compose.ui.ShowcaseScope.showGreeting]).
     * Not affected by `advanceOnTargetTapOnly`, and not blocked by a step whose `canProceed` is
     * false: disable your own button with `enabled = canProceed` if you want that.
     */
    fun next()

    /**
     * Go back to the previous target. Does nothing when [hasPrevious] is false.
     */
    fun previous()

    /**
     * True when there is a step before the current one in a full showcase (the current index is
     * greater than `initIndex`). Always false while showing a single item or a single greeting.
     * Backed by snapshot state, so something like `Button(enabled = hasPrevious)` updates on its own.
     */
    val hasPrevious: Boolean

    /**
     * End the whole showcase now: the overlay fades out and `onFinish` is called. When showing a
     * single item or a single greeting this just dismisses it.
     */
    fun skip()
}

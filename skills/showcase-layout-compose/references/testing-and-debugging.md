# Testing and debugging tours

## Contents
- [Turn on logging first](#turn-on-logging-first)
- [Symptom → cause → fix](#symptom--cause--fix)
- [Compile errors](#compile-errors)
- [UI tests](#ui-tests)

## Turn on logging first

```kotlin
TargetShowcaseLayout(isShowcasing = isShowcasing, onFinish = { isShowcasing = false }) {
    registerEventListener(object : ShowcaseEventListener {
        override fun onEvent(level: Level, event: String) {
            if (level != Level.VERBOSE) println("Tour [$level] $event")
        }
    })
    // ...
}
```

The INFO lines tell the story:

- `moving to index N` and `finished`
- `step N locked until its condition is met`, then `condition met, advancing`
- placement decisions: `Not enough space on top show msg on bottom`, and similar

`VERBOSE` fires on every frame, so only enable it to inspect raw positions.

## Symptom → cause → fix

| Symptom | Likely cause | Fix |
|---|---|---|
| Tour ends after step k, and later steps never show | Index gap (1, 2, 4); a duplicated index; or step k+1's target isn't composed (off-screen lazy item, collapsed section, another tab, `if` branch not taken). The tour counts registered targets to decide it's over. | Make indices 1..N contiguous and unique. Keep every target composed when the tour starts, or see `scrolling-and-lazy-lists.md`. |
| Nothing appears when `isShowcasing = true` | The layout's parent is a `Column`/`Row`, or a `ColumnScope` slot such as sheet or card content, so the overlay is laid out below the content | Put the layout in a stacking parent, or wrap it in `Box(Modifier.fillMaxSize())` |
| The highlight sits in the top-left corner with zero size | The step started before its target was laid out or registered: the tour started on the first frame, or the target appears later (loading state) | Start from `LaunchedEffect` after the data is there, plus a short delay |
| The highlight is offset by the status bar or app bar height | Library ≤1.1.x with a layout that doesn't start at the window origin | Upgrade to ≥1.2.0 |
| The highlight is where the element *was* | The element moved during its step (scroll, animation, keyboard insets, expanding content) | Settle the layout before the step. Scroll in the previous step's `onProceed` (`scrolling-and-lazy-lists.md`). Don't animate targets during their step. |
| ≤1.2.x: on the first step, `ShowcaseLayout`'s highlight flies in from the top-left corner | The tour started before its first target was laid out (`isShowcasing = true` on the first frame) | Start from `LaunchedEffect` with a short delay, or upgrade to 1.3.0 |
| The element's corners are dimmed, as if the highlight were slightly too small | `cornerRadius` is larger than the element's own corner radius, or `CIRCLE` on a rounded square (the default Material 3 FAB) | Lower `cornerRadius` to the smallest corner radius among the targets, or use `RECTANGLE` |
| An elevated element (a FAB, a raised card) looks cut out, with its shadow missing | The cutout is exactly the element's bounds: ≤1.2.x, or `targetPadding = 0.dp` | 1.3.0's default `targetPadding` (4dp), or a larger one |
| The highlight is larger or smaller than the element | `Modifier.showcase` sits at the wrong place in the modifier chain, or the target is a wrapper `Box` rather than the element | Move `.showcase(...)` earlier (to include padding) or later (to hug the content), or onto the element itself |
| ≤1.2.x: after adding `.showcase()` the element itself shrinks or shifts, and the highlight is tiny or missing | `x.showcase(...)` chained after other modifiers applied them twice | Write `x.then(Modifier.showcase(...))`, or upgrade to 1.3.0 |
| ≤1.2.x: `TargetShowcaseLayout` never shows step 1's custom card | The tour started on the first frame (`isShowcasing` initially true) | Start from `LaunchedEffect`, or upgrade to 1.3.0 |
| A replay or `showcaseItem` right after a `ShowcaseLayout` tour shows nothing, or jumps to an old target | `ShowcaseLayout` resets its step `animationDuration + 250 ms` after `onFinish` | Wait at least that long after `onFinish` |
| `onProceed` ran several times for one step | The user tapped again while the step's message was fading out | Make `onProceed` idempotent |
| A locked text field can't be tapped again once the keyboard is open | The keyboard moved the field out of the step's touch hole, which doesn't follow the target | Complete the step from an IME action, or lock a field that doesn't move (`overlays-bars-and-insets.md`) |
| The text message is invisible | ≤1.2.x: the default black text on the default black overlay. Any version: an explicit color close to the overlay's. | Upgrade to 1.3.0 (automatic contrast), or set `textStyle = TextStyle(color = ...)` |
| No arrow | `TargetShowcaseLayout`, which draws no arrows (1.3.0 logs a WARNING); or the step is the greeting, which never gets one | Use `ShowcaseLayout` for arrows |
| ≤1.2.x: the arrow comes from the wrong side or crosses the text | `Arrow.targetFrom` doesn't match where the text message is (default `Side.Bottom` vs `Gravity.Auto` → above). 1.3.0 makes Top/Bottom arrows follow the message. | Set `gravity` explicitly and the matching `targetFrom`, or upgrade |
| The message is cut off at the top or bottom | `ShowcaseLayout` forces `Gravity.Top`/`Bottom` even when there's no room | Use the opposite gravity or `Gravity.Auto`. (`TargetShowcaseLayout` flips by itself.) |
| A long text message covers its target (`ShowcaseLayout`) | Text above a target starts 84dp above it and grows downward | Shorten it, use `Gravity.Bottom`, or use a custom composable message |
| The tour starts again right after finishing | `onFinish` doesn't set `isShowcasing = false` | Reset it in `onFinish` |
| "Replay" starts in the middle, or the screen is untappable with no overlay visible | `isShowcasing` was set to `false` mid-tour, so the layout kept its step | End tours with `skip()` (`message-cards.md` → "Ending the tour from outside the card") |
| A locked step doesn't lock | `canProceed` was already `true` when the step was reached | Lock on something done *during* the tour (a counter or flag reset on replay) |
| A locked step: the element can't be tapped | The tap is outside the registered bounds (registered on a wrapper, or a `CIRCLE` cutout drawn larger than the bounds), or a custom card overlaps the element | Register the element itself. Move the card with `gravity`. |
| Users skip a locked step with "Next" | `next()` bypasses locks by design | Hide Next on locked cards, or use `enabled = canProceed` |
| Taps on a card's text advance the tour | Non-clickable parts of a card fall through to the overlay by design | Set `advanceOnTargetTapOnly = true` if that's unwanted |
| The keyboard stays open after a text-field step | Nothing closed it | `onProceed = { focusManager.clearFocus(); keyboard?.hide() }` |
| A hint shown with `showcaseItem` never appears | Called outside the content lambda, called while a tour runs, or the index isn't registered (not composed) | Call it inside the content lambda from `LaunchedEffect`/`rememberCoroutineScope`, when no tour is running, after the target is composed |
| The screen reader doesn't read tour messages | Text messages are drawn on a canvas | Use custom composable messages |

## Compile errors

| Error | Meaning |
|---|---|
| `Unresolved reference: showcase` / `showcaseItem` / `registerEventListener` | Called outside the layout's content lambda. Pass a `Modifier` down from inside the lambda, or give the child a `ShowcaseScope` receiver. |
| `Type mismatch: ... Float ... Dp` on `headSize` | 1.3.0 made `Arrow.headSize` a `Dp`. Use `headSize = 9.dp` (old px ÷ ~2.75). |
| `'TargetShowcaseLayout(...)' is deprecated ... lineThickness` | Remove `lineThickness` from `TargetShowcaseLayout` |
| `Unresolved reference: canProceed` / `ShowcaseMsgScope`, or no lambda overload for `ShowcaseMsg { }` | The project is on ≤1.1.x. Upgrade to ≥1.2.0. |
| `Unresolved reference: colors` / `ShowcaseLayoutDefaults` | ≤1.0.x. Upgrade, or use `isDarkLayout`. |
| `Gravity` type mismatch | `android.view.Gravity` was imported. Import `ly.com.tahaben.showcase_layout_compose.model.Gravity`. |
| `@Composable invocations can only happen from the context of a @Composable function` on `ShowcaseLayoutDefaults.colors(...)` | It's composable. Call it in composition, not in a ViewModel or a top-level `val`. |

## UI tests

These rules were verified with Compose Multiplatform 1.12 desktop tests on both layouts:

1. **`TargetShowcaseLayout` never goes idle.** Its pulse is an infinite animation. With the default
   auto-advancing test clock, `setContent`, `waitForIdle()`, `waitUntil {}` and even plain node
   queries hang until they time out. Set `mainClock.autoAdvance = false` **before** `setContent`,
   and move time yourself with `mainClock.advanceTimeBy(ms)`. A frozen clock doesn't advance inside
   `waitUntil`, so use `advanceTimeBy` or `mainClock.advanceTimeUntil { condition }`.
2. **`ShowcaseLayout` has no infinite animation,** so the default clock plus `waitForIdle()` works.
   The frozen-clock style works for both layouts.
3. **Leave enough time between taps.**
   - A tap is only taken once the previous step's message has faded out: `exitAnim`, 500 ms by
     default.
   - Taps during the fade restart it and call that step's `onProceed` again, though the tour
     doesn't skip ahead.
   - `TargetShowcaseLayout` dims step 1 only after about
     `animationDuration + 500 + animationDuration` ms.
   - `onFinish` fires about `500 + animationDuration / 2` ms after the last tap.
   - Advancing 3000 ms per step is safe at the default durations.
   - Expose `animationDuration` (and the message animations) as parameters, so tests can shorten
     them.
4. **How to advance:**
   - Click the overlay node. Its test tag is `"canvas"` for `ShowcaseLayout` and
     `"circleModeCanvas"` for `TargetShowcaseLayout`. Use `performClick()`, or
     `performTouchInput { click(center) }`.
   - Or click a card button: `onNodeWithText("Next").performClick()`. A custom card is only in the
     tree while it's visible, so advance the clock past its fade-in first.
5. **Text messages aren't in the semantics tree.** `onNodeWithText("your message")` finds nothing.
   Assert on custom card text, on `onFinish` and `onProceed` calls, or on pixels (helper below).
6. **Start the tour from `LaunchedEffect` in the screen,** as in production. On ≤1.2.x, starting
   with `isShowcasing = true` on the first frame makes `ShowcaseLayout`'s cutout fly in from the
   top-left corner.

Dependencies:

- **Compose Multiplatform:** `org.jetbrains.compose.ui:ui-test:<compose-multiplatform version>`, plus
  `compose.desktop.currentOs` for desktop tests. Use `androidx.compose.ui.test.v2.runComposeUiTest`
  (CMP 1.12+; older versions have `androidx.compose.ui.test.runComposeUiTest`).
- **Android-only projects:** `createComposeRule()` from `androidx.compose.ui:ui-test-junit4`, with
  the same clock rules (`composeTestRule.mainClock.autoAdvance = false` for
  `TargetShowcaseLayout`).

```kotlin
@OptIn(ExperimentalTestApi::class)
class OnboardingTourTest {

    @Test
    fun tourRunsToTheEndAndIsMarkedSeen() = runComposeUiTest {
        mainClock.autoAdvance = false              // required for TargetShowcaseLayout, harmless for ShowcaseLayout
        var finished = 0
        setContent { HomeScreen(tourSeen = false, onTourSeen = { finished++ }) }

        repeat(3) {                                // steps 1..3 (add one more for a greeting)
            mainClock.advanceTimeBy(3_000)         // let the step settle
            onNodeWithTag("circleModeCanvas").performClick()   // "canvas" for ShowcaseLayout
        }
        mainClock.advanceTimeBy(3_000)
        assertEquals(1, finished)
    }

    @Test
    fun cardButtonsDriveTheTour() = runComposeUiTest {
        mainClock.autoAdvance = false
        var finished = 0
        setContent { HomeScreen(tourSeen = false, onTourSeen = { finished++ }) }
        mainClock.advanceTimeBy(3_000)
        onNodeWithText("Skip").performClick()      // custom cards are real composables
        mainClock.advanceTimeBy(3_000)
        assertEquals(1, finished)
    }
}

/** True when the element tagged [tag] is inside the cutout: its center pixel shows its own, undimmed color. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.isSpotlit(tag: String, color: Color): Boolean {
    val center = onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.center
    val pixel = onRoot().captureToImage().toPixelMap()[center.x.toInt(), center.y.toInt()]
    return abs(pixel.red - color.red) < 0.05f && abs(pixel.green - color.green) < 0.05f && abs(pixel.blue - color.blue) < 0.05f
}
```

For pixel checks, compare against "not the element's own color" rather than an exact dimmed value.
`TargetShowcaseLayout`'s pulse ring brightens pixels near the target.

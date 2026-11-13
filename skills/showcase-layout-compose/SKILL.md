---
name: showcase-layout-compose
description: Builds onboarding tours, coach marks, feature spotlights and "what's new" hints in Jetpack Compose and Compose Multiplatform apps with the ShowcaseLayoutCompose library (ly.com.tahaben:showcase-layout-compose) - ShowcaseLayout, TargetShowcaseLayout, Modifier.showcase, ShowcaseMsg, Arrow, showcaseItem, showGreeting, locked steps (canProceed/onProceed) and custom message cards. Use it whenever someone wants to highlight, showcase, spotlight, introduce or explain parts of a Compose UI to users (a first-run walkthrough, a guided tour, a "tap here" hint, a tooltip that dims the rest of the screen, an interactive tutorial step), even if they don't name the library, and whenever code imports ly.com.tahaben.showcase_layout_compose. Also use it to debug tours (stops early, highlight in the wrong place, invisible text), to write UI tests for tours, and to upgrade to 1.3.0.
license: Apache-2.0
metadata:
  library: "ly.com.tahaben:showcase-layout-compose"
  library-version: "1.3.0"
---

# ShowcaseLayoutCompose

ShowcaseLayoutCompose dims a screen and highlights one element at a time with a message next to it.
It runs on Android, iOS, desktop (JVM) and web (wasmJs), in plain Jetpack Compose or Compose
Multiplatform `commonMain`, with the same artifact and imports everywhere. It depends only on Compose
foundation, so it works with Material 3, Material 2 or a custom design system.

The mental model, which explains most of the rules below:

1. A **layout** (`ShowcaseLayout` or `TargetShowcaseLayout`) wraps a screen. It composes your content
   and then draws an overlay on top of it.
2. Inside the layout's content lambda (receiver `ShowcaseScope`), elements register as **targets**
   with `Modifier.showcase(index, message)`. Registration happens during layout, through
   `onGloballyPositioned`.
3. Steps run in index order: `0` is the optional greeting, then targets `1, 2, 3, ...`. A tap
   advances, and after the last step `onFinish` is called.

This file is the workflow plus the rules that prevent the common bugs. Load these reference files
when you need them:

| File | Load it when |
|---|---|
| `references/api.md` | You need an exact parameter, default, per-layout behavior, the 1.3.0 migration, or code for an older version |
| `references/message-cards.md` | You build tour cards (title, body, step dots, Back/Skip/Next), a welcome card, or styled text messages |
| `references/scrolling-and-lazy-lists.md` | Any target sits in a scrollable screen, a `LazyColumn`/`LazyRow`/grid, or below the fold |
| `references/overlays-bars-and-insets.md` | Targets are in top/bottom bars, FABs, dialogs, bottom sheets, menus, or the screen uses edge-to-edge/IME insets |
| `references/testing-and-debugging.md` | You write UI tests for a tour, or a tour misbehaves (ends early, wrong spot, invisible text, stuck step) |

## Workflow

### 1. Read the project before writing code

Look for these, and use what you find instead of assuming:

- **The dependency and its version.** Search `gradle/libs.versions.toml` and every
  `build.gradle(.kts)` for `showcase-layout-compose`.
- **Platform.** It's either an Android app module or a KMP `commonMain` source set. In KMP the
  dependency goes in `commonMain.dependencies { }`.
- **Design system.** Find the app's theme, colors and typography, so messages match the app.
- **The screen.** Find its root composable (often a `Scaffold`), the elements to highlight, and
  whether any of them live in a lazy list, a dialog or a bottom sheet.
- **Persistence.** For first-run tours, find where the app keeps flags. That may be DataStore,
  SharedPreferences, `multiplatform-settings`, a settings repository, or a ViewModel/state holder.

### 2. Make sure the version supports what you'll write

| Feature | Since |
|---|---|
| `ShowcaseLayout`, `Modifier.showcase`, `ShowcaseMsg` (String/AnnotatedString), `Arrow`, `Head`, `Gravity.Auto`, `MsgAnimation`, greeting, `registerEventListener` | 1.0.4 |
| Multiplatform, `showcaseItem(index)`, `showGreeting(msg)` | 1.0.5 |
| `TargetShowcaseLayout` | 1.0.6 |
| `targetShape` / `cornerRadius` on `ShowcaseLayout` | 1.0.7 |
| `colors = ShowcaseLayoutDefaults.colors(...)`, `advanceOnTargetTapOnly` (`isDarkLayout` deprecated) | 1.1.0 |
| Custom composable messages `ShowcaseMsg { ... }` + `ShowcaseMsgScope`, locked steps `canProceed` / `onProceed`, `gravity` in `TargetShowcaseLayout`, correct highlight when the layout isn't at the window origin | 1.2.0 |
| `Arrow.headSize` is `Dp` (**breaking**), spacing in dp, readable default text color, enter/exit durations honored, `lineThickness` deprecated on `TargetShowcaseLayout`, fix for modifiers chained before `.showcase()`, Top/Bottom arrows follow the message, first cutout placed on its target (no fly-in from the corner), `targetPadding` (4dp space around every target by default) | 1.3.0 |

- **No dependency:** add `implementation("ly.com.tahaben:showcase-layout-compose:1.3.0")`, or a
  version-catalog entry if the project uses one. Add it to the module that owns the UI.
- **Version too old for the request:** propose bumping it, explain why in one sentence, and mention
  the 1.3.0 `headSize` change if you cross it (see `references/api.md`). Don't write code against
  an API the project doesn't have. If the developer can't upgrade, `references/api.md` → "Older
  versions" shows the equivalents.
- **Between versions:** most of this skill applies from 1.2.0 on. Where 1.3.0 behaves differently,
  that's called out.

### 3. Pick the mode

| The developer wants | Use |
|---|---|
| A walkthrough of several elements in order (onboarding, a new screen's tour) | **Tour**: `isShowcasing = true` → greeting (optional) → steps 1..N → `onFinish` |
| One hint on demand ("New!" badge, a help icon, after the user does X) | **Single item**: `showcaseItem(index)`, dismissed by a tap, doesn't need `isShowcasing`, never calls `onFinish` |
| A full-screen message with no target ("What's new in 2.0") | **Standalone greeting**: `showGreeting(ShowcaseMsg(...))` |
| The user must *do* something before continuing (type, toggle, tap the button) | Any of the above plus a **locked step**: `canProceed = false` until done (section 7) |

A target registered with `Modifier.showcase` can be part of a tour and can also be shown on its own
with `showcaseItem`. Don't start a single item while a tour is running, because both drive the same
step counter.

### 4. Pick the layout

The two layouts look different, and some options only work in one of them:

| | `TargetShowcaseLayout` | `ShowcaseLayout` |
|---|---|---|
| Look | Spotlight. The highlight morphs from target to target (`animateToNextTarget`), with a pulsing ring (`pulseColor`) | Full-screen dim with a cutout that slides between targets |
| Arrows (`ShowcaseMsg.arrow`) | **Not drawn** (logs a WARNING since 1.3.0) | Yes: straight or curved, 4 head styles |
| Text message background | No card. `msgBackground` recolors the **overlay** for that step (at 90% alpha) | `msgBackground` is a card behind the text, with `roundedCorner` |
| Default shape | `ROUNDED_RECTANGLE`, `cornerRadius = 8.dp` | `RECTANGLE` (`cornerRadius = 16.dp` applies when ROUNDED_RECTANGLE) |
| `gravity` Top/Bottom | Preference. Flips to the other side if it doesn't fit | Forced, even if it ends up off screen |

Choose from the request:

- It mentions arrows, or "pointing at" an element → `ShowcaseLayout`.
- It mentions a spotlight, pulse, glow, focus ring, or morphing between elements →
  `TargetShowcaseLayout`.
- The project already uses one of them → use that one, for consistency.
- Nothing points either way → `TargetShowcaseLayout`. Say so in your summary, and note that switching
  is a one-word change, apart from arrows.

### 5. Wire up a tour

The template below is a first-run tour with themed cards. Adapt the names, theme, targets and
storage to the project.

```kotlin
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayoutDefaults
import ly.com.tahaben.showcase_layout_compose.ui.TargetShowcaseLayout

@Composable
fun HomeScreen(
    tourSeen: Boolean,          // read from the app's settings store
    onTourSeen: () -> Unit,     // persist it
) {
    var isShowcasing by remember { mutableStateOf(false) }
    // Start after the first layout pass, so targets have registered their bounds, and after any
    // enter animation, so the highlight lands where the element ends up.
    LaunchedEffect(tourSeen) {
        if (!tourSeen) {
            delay(600)
            isShowcasing = true
        }
    }

    TargetShowcaseLayout(
        isShowcasing = isShowcasing,
        onFinish = {                 // runs after the last step AND after skip()
            isShowcasing = false     // required, or the tour starts over
            onTourSeen()
        },
        greeting = ShowcaseMsg { WelcomeCard(scope = this) },   // step 0, centered
        targetShape = TargetShape.ROUNDED_RECTANGLE,
        cornerRadius = 16.dp,        // ≤ every target's own corners (M3 FAB: 16dp), or it clips them (rule 7)
        animationDuration = 700,
        colors = ShowcaseLayoutDefaults.colors(
            overlayColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f),
            pulseColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        // `this` is ShowcaseScope: Modifier.showcase is available here and in nested lambdas
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        IconButton(
                            onClick = onSearch,
                            modifier = Modifier.showcase(
                                index = 1,
                                message = tourStep { TourCard(1, 3, "Search", "Find anything in your library.", this) },
                            ),
                        ) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAdd,
                    modifier = Modifier.showcase(
                        index = 3,
                        message = tourStep(Gravity.Top) { TourCard(3, 3, "Add", "Create a new item.", this) },
                    ),
                ) { Icon(Icons.Default.Add, contentDescription = "Add") }
            },
        ) { padding ->
            HomeContent(
                modifier = Modifier.padding(padding),
                // child composables outside this lambda can't call Modifier.showcase: pass the modifier down
                filterChipModifier = Modifier.showcase(
                    index = 2,
                    message = tourStep { TourCard(2, 3, "Filters", "Narrow the list down.", this) },
                ),
            )
        }
    }
}

/** Every step shares the same look; only the card content differs. */
private fun tourStep(
    gravity: Gravity = Gravity.Auto,
    content: @Composable ShowcaseMsgScope.() -> Unit,
) = ShowcaseMsg(
    gravity = gravity,
    enterAnim = MsgAnimation.FadeInOut(350),
    exitAnim = MsgAnimation.FadeInOut(250),
    content = content,
)
```

`TourCard` and `WelcomeCard` are templates from `references/message-cards.md`. Restyle them with the
app's theme.

Also offer a way to see the tour again, such as a "Show tour" menu item or help icon that sets
`isShowcasing = true`. Add it if the screen has an obvious place for it.

**Persisting "seen".** If the request implies first-run onboarding, save the flag in `onFinish`,
using the storage the project already has. If there's none, expose `tourSeen` / `onTourSeen`
parameters like the template does, and ask before adding a persistence dependency. A one-off hint
shown with `showcaseItem` that should appear only once needs the same treatment: set its flag when
you trigger it.

### 6. Messages

**Default style:** one-off hints get a text `ShowcaseMsg`. Multi-step tours get custom composable
cards with progress and Back/Skip/Next, built from the app's theme (`references/message-cards.md`).
The developer's explicit request always wins.

**Text messages:** `ShowcaseMsg("text", textStyle = ..., msgBackground = ..., roundedCorner = ...,
gravity = ..., arrow = ...)`.

- Text is drawn on a canvas, so it wraps automatically but **screen readers can't see it**. Use a
  custom composable when accessibility matters; those are real composables with semantics.
- **Readable color (1.3.0):** if `textStyle` sets no color, the text is white or black, whichever
  contrasts with what's behind it. That's the `msgBackground` card in `ShowcaseLayout` when set,
  otherwise the overlay. On ≤1.2.x the default is **black**, which is invisible on the default black
  overlay, so always pass `textStyle = TextStyle(color = ...)` there. If the overlay is mostly
  transparent (alpha < 0.5), set the color yourself; the automatic choice only looks at the overlay
  color.
- **In `ShowcaseLayout`**, text placed above a target starts 84dp above it and grows *downward*. Long
  text placed above (Top or Auto) can cover the target. Keep it to about two lines, use
  `Gravity.Bottom`, or use a custom composable, which is anchored by its edge facing the target.

**Custom composable messages:** `ShowcaseMsg(gravity, arrow, enterAnim, exitAnim, msgBackground) { ... }`.

- The receiver is `ShowcaseMsgScope`: `next()`, `previous()`, `skip()` (ends the tour and calls
  `onFinish`), and `hasPrevious`.
- Give the card a fixed or capped width (`Modifier.width(300.dp)` or `widthIn(max = 320.dp)`), never
  `fillMaxSize`/`fillMaxWidth`. The library measures it at most screen width minus a margin and
  keeps it on screen.
- Taps on non-clickable parts of the card advance the tour like a tap on the overlay. Buttons handle
  their own clicks.
- `text`, `textStyle` and `roundedCorner` are ignored. `msgBackground` is ignored by `ShowcaseLayout`;
  in `TargetShowcaseLayout` it's still that step's overlay color.
- Native interop views (`UIKitView`, `AndroidView`) inside the card aren't supported.
- With a greeting (step 0), `hasPrevious` is already true on step 1, and `previous()` reopens the
  greeting. Hide "Back" on step 1 unless that's wanted.

**Gravity:** `Gravity.Auto` (the default) prefers above the target. Set `Gravity.Bottom` for targets
near the top of the screen and `Gravity.Top` for targets near the bottom, such as FABs and bottom
bars. That keeps cards away from status and navigation bars.

**Arrows (`ShowcaseLayout` only):** `Arrow(targetFrom = Side.Top, head = Head.TRIANGLE, headSize = 9.dp,
color = Color.White, curved = false, animSize = true, animationDuration = 1000)`. The line width is
the layout's `lineThickness`.

- **Set an explicit `gravity` and the matching `targetFrom`:** `Gravity.Top` → `Side.Top`,
  `Gravity.Bottom` → `Side.Bottom`.
  - From 1.3.0 a `Side.Top`/`Side.Bottom` arrow follows the message by itself, for text and custom
    messages alike, so a mismatch no longer draws it from the wrong side. Matching them still says
    what you mean.
  - On ≤1.2.x a text message's arrow uses `targetFrom` as given. The default `Side.Bottom` with
    `Gravity.Auto` (text usually above) draws the arrow from below while the text sits above, so
    matching them is required there.
- `Side.Left`/`Side.Right` are kept as given; the message is still above or below.
- For a custom composable message, the arrow starts at the card edge facing the target.
- `curved = true` centers the message horizontally and curves to the target. It looks best for
  targets near the left or right edge.
- `headSize` is `Dp` from 1.3.0 (default `9.dp`). On ≤1.2.x it's a pixel `Float` (default `25f`).

### 7. Locked steps (the user must act)

```kotlin
var name by remember { mutableStateOf("") }
val keyboard = LocalSoftwareKeyboardController.current
val focusManager = LocalFocusManager.current

OutlinedTextField(
    value = name,
    onValueChange = { name = it },
    modifier = Modifier.showcase(
        index = 2,
        message = tourStep { TourCard(2, 3, "Your name", "Type it to continue.", this, locked = true) },
        canProceed = name.isNotBlank(),        // snapshot state; the tour moves on by itself once true
        onProceed = {                          // runs once when the step ends, however it ends
            focusManager.clearFocus()
            keyboard?.hide()
        },
    ),
)
```

How a locked step behaves:

- **While `canProceed` is false:**
  - Taps on the overlay do nothing.
  - Touches **inside the target's rectangular bounds** reach the real element, so a text field gets
    focus and the keyboard, a button clicks, a switch toggles.
  - Touches elsewhere are swallowed.
- **When `canProceed` turns true:** the tour advances by itself.
- **The step locks only if `canProceed` is already false when it's reached.** For an action the user
  may already have done (a switch already on), lock on a change made during the tour, not on the
  absolute state. Use a counter or `actionDoneDuringTour` flag, and reset it when the tour replays.
- **`next()`, `previous()` and `skip()` bypass the lock.** Keep a Skip button as an escape hatch, and
  hide or disable Next (`enabled = canProceed`) so it can't skip the action.
- **`onProceed` cleans up after the step**, for example by hiding the soft keyboard. It works on any
  step, not just locked ones.
- **`onProceed` can run more than once** if the user taps again during the step's exit fade. Keep it
  idempotent: hiding the keyboard, saving a flag, or an instant scroll is fine; incrementing a
  counter or navigating isn't.
- **A locked step can't follow its field if the keyboard moves it.** If the keyboard pushes the field
  up (`imePadding`, `adjustResize`), the cutout and the touch hole stay where the field was. Lock
  fields that don't move, and let the action complete from the keyboard too, such as the IME
  Send/Done action (see `references/overlays-bars-and-insets.md`).
- **With `TargetShape.CIRCLE`**, the visible circle can be larger than the pass-through rectangle.

### 8. Single hint and standalone greeting

```kotlin
@Composable
fun EditorScreen(
    showExportHint: Boolean,          // e.g. "user saved for the first time && hint not seen", from the app's store
    onExportHintShown: () -> Unit,    // persist that the hint was shown
    onExport: () -> Unit,
) {
    // a layout used only for on-demand hints: isShowcasing stays false
    TargetShowcaseLayout(isShowcasing = false, onFinish = {}) {
        val scope = rememberCoroutineScope()        // created inside the content lambda
        LaunchedEffect(showExportHint) {
            if (showExportHint) {
                delay(300)              // let the screen settle
                showcaseItem(1)         // suspend member of ShowcaseScope; a tap dismisses it
                onExportHintShown()
            }
        }
        Column(Modifier.fillMaxSize()) {           // the screen's own UI
            IconButton(
                onClick = onExport,
                modifier = Modifier.showcase(
                    index = 1,
                    message = ShowcaseMsg("Export your data from here", gravity = Gravity.Bottom),
                ),
            ) { Icon(Icons.Default.Share, contentDescription = "Export") }
            Button(onClick = { scope.launch { showGreeting(ShowcaseMsg("Version 2.0 is here!")) } }) {
                Text("What's new")
            }
        }
    }
}
```

- `showcaseItem` and `showGreeting` are suspend members of `ShowcaseScope`. Call them inside the
  content lambda from `LaunchedEffect` or `rememberCoroutineScope().launch`.
- Both return right away rather than waiting for the dismissal. Use `onProceed` on the target to
  react when a single item is dismissed.
- Events from a ViewModel: collect them in a `LaunchedEffect` inside the content lambda and call
  `showcaseItem` there.

### 9. Before you finish

Read your code against these rules, then compile the module. Fix anything that fails.

- [ ] The layout's parent stacks children (rule 1).
- [ ] Every highlighted element is inside the content lambda (rule 2).
- [ ] Indices are unique, start at 1, and have no gaps; each target is composed when its step comes
      (rule 3).
- [ ] `onFinish` sets `isShowcasing = false` and saves "seen" if needed (rule 4).
- [ ] The tour starts from a `LaunchedEffect` after layout, once the data it highlights is present
      (rule 5).
- [ ] Text messages are readable: on ≤1.2.x, `textStyle` has a color.
- [ ] Arrows only on `ShowcaseLayout`, with `targetFrom` matching `gravity`. No `lineThickness` on
      `TargetShowcaseLayout` (1.3.0 deprecates it).
- [ ] `cornerRadius` is no larger than any target's own corner radius, and `CIRCLE` is used only
      when every target is round (rule 7).
- [ ] Locked steps can't be skipped by "Next", and they keep a Skip.
- [ ] Imports come from `ly.com.tahaben.showcase_layout_compose.*`. A Kotlin `Gravity` is the
      library's (`...model.Gravity`), not `android.view.Gravity`.

In your summary, tell the developer:

- which layout you chose and why
- how the tour starts and how to replay it
- where "seen" is stored
- anything you couldn't do, such as a target inside a dialog

## Rules that prevent the common bugs

1. **Put the layout where its parent stacks children.** The layout emits your content and its
   overlay as siblings in the caller's layout; it doesn't wrap them in a `Box`.
   - Inside a `Column`/`Row`, the overlay is laid out after your content and is squeezed out or
     misplaced.
   - Good parents: `setContent { }` / `ComposeViewport { }` / `Window { }` roots, a `Box`,
     `Surface`, `Crossfade`/`AnimatedContent`, or a navigation destination.
   - If unsure, wrap it in `Box(Modifier.fillMaxSize()) { ... }`.
   - Give the content lambda a single root, usually the screen's `Scaffold`. Several root
     composables would be stacked on top of each other.
2. **Everything you highlight must be inside the content lambda.**
   - `Modifier.showcase` is a member extension of `ShowcaseScope`, so it only resolves there,
     including nested lambdas like `Scaffold` slots.
   - For child composables, either pass a `Modifier` parameter built at the call site (preferred),
     or give the child a `ShowcaseScope` receiver (`fun ShowcaseScope.FilterBar()`).
   - To dim the top bar, bottom bar and FAB too, wrap the whole `Scaffold`. The overlay covers only
     the layout's own bounds.
3. **Indices are `1..N`, unique, with no gaps, and each target is composed when its step starts.**
   - The tour decides it's over by counting registered targets.
   - With a gap (1, 2, 4), steps after the gap never show.
   - A reused index overwrites the earlier target.
   - A target that isn't composed (an off-screen lazy item, a collapsed section, another tab) ends
     the tour early or highlights a stale position.
   - `index = 0` throws, because 0 is reserved for the greeting.
   - See `references/scrolling-and-lazy-lists.md`.
4. **`onFinish` must set `isShowcasing = false`.** `onFinish` runs after the last step and after
   `skip()`. If `isShowcasing` stays true, the tour restarts.
   - `ShowcaseLayout` resets its step counter `animationDuration + 250 ms` after `onFinish`. Wait at
     least that long before calling `showcaseItem` or replaying the tour from code.
   - To end a tour early, call `skip()` from a card instead of flipping `isShowcasing` to false
     mid-tour. Flipping it leaves the layout on its current step for the next run.
5. **Start the tour after the screen is laid out and its content has loaded.**
   - Use `LaunchedEffect(...) { delay(300..900); isShowcasing = true }`, keyed on whatever the tour
     depends on (data loaded, tour not seen yet).
   - On ≤1.2.x, starting before the first target is laid out (`isShowcasing = true` on the first
     frame) makes `ShowcaseLayout`'s cutout fly in from the top-left corner, and hides a custom card
     on step 1 of `TargetShowcaseLayout`. 1.3.0 fixes both, but data still has to be on screen
     before the tour starts.
   - A step's highlight position, and a locked step's touch hole, are taken when the step starts and
     don't follow the target. Don't animate, scroll or resize a target while its own step is
     showing. Scroll *between* steps, as `references/scrolling-and-lazy-lists.md` shows.
6. **Start the modifier chain with `Modifier.showcase(...)`, or append it with
   `.then(Modifier.showcase(...))`.** It highlights the bounds at the point in the chain where it
   sits:
   - At the start, the highlight includes padding added after it.
   - Appended at the end, it hugs the content.
   - On ≤1.2.x, avoid `Modifier.padding(..).size(..).showcase(..)`. That chained form applied the
     earlier modifiers twice and shrank or offset both the element and its highlight; 1.3.0 fixes
     it.
7. **Pick a shape that never clips a target.** The cutout is the target's bounds plus `targetPadding`
   (1.3.0, 4dp by default), with the corners grown by the same amount. Wherever the corners are
   rounder than the element's own, they still dim the element's corners. Shape and radius are per
   layout, not per step, so they must suit every target in the tour.
   - Keep the default `targetPadding`. It leaves room for the element's shadow, so FABs and cards
     don't look cut out. Raise it for small icons; set `0.dp` only for an exact fit. On ≤1.2.x there
     is no padding: an elevated element always loses its shadow.
   - `RECTANGLE` never clips.
   - `ROUNDED_RECTANGLE`: set `cornerRadius` to the **smallest** corner radius among the targets.
     A smaller radius is always safe: a round button just shows a sliver of background in the
     cutout's corners. A larger one clips. It's clamped to half the target's smaller side.
   - Material 3 defaults: `FloatingActionButton` 16dp, `SmallFloatingActionButton` 12dp,
     `LargeFloatingActionButton` 28dp, `ExtendedFloatingActionButton` 16dp, `Card` 12dp, chips 8dp,
     `OutlinedTextField` 4dp, `Button`/`IconButton` fully round. Check `MaterialTheme.shapes` and
     each component's `shape` argument for overrides.
   - A target with no visible edge (a row of chips, a header area) takes any radius.
   - `CIRCLE` has a diameter equal to the target's larger side. It covers only round elements
     (icon buttons, avatars, a `CircleShape` FAB) and clips rounded squares such as the default
     Material 3 FAB. Use it only when every target is round.
8. **Use one layout per screen, and don't nest layouts.** Give each screen or destination its own
   layout and state.
   - Targets stay registered for as long as the layout lives. If a replay must start clean (a
     target was removed, or a far item was registered during the last run), hoist the screen state
     above the layout and wrap the layout in `key(tourRun) { ... }`. Increment `tourRun` on replay.
9. **Colors.**
   - `ShowcaseLayoutDefaults.colors(overlayColor, pulseColor)` is a `@Composable` function; call it
     in composition.
   - The overlay's own alpha is honored. The default is `Color.Black.copy(alpha = 0.9f)`.
   - For dark UIs, use a light overlay such as `Color.White.copy(alpha = 0.9f)`, with dark message
     text.
   - `pulseColor` affects only `TargetShowcaseLayout`.
   - `isDarkLayout` is deprecated; replace it with `colors`.
10. **Logging.**
    - `registerEventListener(object : ShowcaseEventListener { ... })` inside the content lambda shows
      every step change, lock and placement decision.
    - Filter out `Level.VERBOSE`, which fires on every frame.
    - Remove the listener, or gate it on debug builds, before shipping.

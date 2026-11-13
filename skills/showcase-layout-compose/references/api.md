# ShowcaseLayoutCompose API reference (1.3.0)

## Contents
- [Imports](#imports)
- [ShowcaseLayout](#showcaselayout)
- [TargetShowcaseLayout](#targetshowcaselayout)
- [ShowcaseScope (content receiver)](#showcasescope-content-receiver)
- [ShowcaseMsg](#showcasemsg)
- [ShowcaseMsgScope (custom message receiver)](#showcasemsgscope-custom-message-receiver)
- [Arrow, Head, Side](#arrow-head-side)
- [Gravity, TargetShape, MsgAnimation](#gravity-targetshape-msganimation)
- [ShowcaseLayoutDefaults.colors](#showcaselayoutdefaultscolors)
- [Event listener](#event-listener)
- [How a step advances](#how-a-step-advances)
- [Message placement and size limits](#message-placement-and-size-limits)
- [Migrating to 1.3.0](#migrating-to-130)
- [Older versions](#older-versions)

## Imports

```kotlin
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayout
import ly.com.tahaben.showcase_layout_compose.ui.TargetShowcaseLayout
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseLayoutDefaults
import ly.com.tahaben.showcase_layout_compose.ui.ShowcaseScope          // only for `fun ShowcaseScope.Child()` receivers
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsg
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope    // only when a helper takes the scope as a parameter/receiver
import ly.com.tahaben.showcase_layout_compose.model.Arrow
import ly.com.tahaben.showcase_layout_compose.model.Head
import ly.com.tahaben.showcase_layout_compose.model.Side
import ly.com.tahaben.showcase_layout_compose.model.Gravity
import ly.com.tahaben.showcase_layout_compose.model.TargetShape
import ly.com.tahaben.showcase_layout_compose.model.MsgAnimation
import ly.com.tahaben.showcase_layout_compose.domain.Level
import ly.com.tahaben.showcase_layout_compose.domain.ShowcaseEventListener
```

You never import `Modifier.showcase`, `Showcase`, `showcaseItem`, `showGreeting` or
`registerEventListener`. They are members of `ShowcaseScope` and resolve inside the layout's
content lambda.

## ShowcaseLayout

```kotlin
@Composable
fun ShowcaseLayout(
    isShowcasing: Boolean,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    lineThickness: Dp = 5.dp,
    targetShape: TargetShape = TargetShape.RECTANGLE,
    cornerRadius: Dp = 16.dp,
    targetPadding: Dp = ShowcaseLayoutDefaults.TargetPadding,   // 4.dp; 1.3.0
    advanceOnTargetTapOnly: Boolean = false,
    colors: ShowcaseLayoutDefaults.Colors = ShowcaseLayoutDefaults.colors(),
    content: @Composable ShowcaseScope.() -> Unit,
)
```

| Param | Meaning |
|---|---|
| `isShowcasing` | `true` runs the tour from `initIndex`. Set it back to `false` in `onFinish`. |
| `initIndex` | Step to start from. `0` = greeting (if `greeting != null`), else coerced to `1`. Use `1` to skip a defined greeting, or `k` to resume. |
| `animationDuration` | ms for the cutout to move between targets and for the overlay fade. The arrow starts drawing after this delay. |
| `onFinish` | Called after the last step is dismissed, and after `skip()`. Not called for `showcaseItem` / `showGreeting`. |
| `greeting` | Step 0: full-screen dim with no cutout, message centered. A tap anywhere continues. |
| `lineThickness` | Arrow line width. |
| `targetShape` / `cornerRadius` | Cutout shape. The radius is used for `ROUNDED_RECTANGLE` and clamped to half the target's smaller side. |
| `targetPadding` | 1.3.0, default 4dp. Space added around every target: the cutout, the tap area, a locked step's touch area and message/arrow placement all use the grown bounds, and `ROUNDED_RECTANGLE` corners grow by the same amount (concentric with the element). It keeps elevated elements' shadows visible, so a FAB doesn't look cut out. `0.dp` gives the exact bounds of ≤1.2.x. |
| `advanceOnTargetTapOnly` | Only taps inside the target (plus 6dp of slop) advance. The greeting still advances on any tap. Doesn't pass the tap through to the element. |
| `colors` | `overlayColor` (alpha honored). `pulseColor` is unused here. |

The deprecated overload `ShowcaseLayout(isShowcasing, isDarkLayout, ...)` maps `isDarkLayout = true`
to `colors(overlayColor = Color.White.copy(alpha = 0.9f))`.

## TargetShowcaseLayout

```kotlin
@Composable
fun TargetShowcaseLayout(
    isShowcasing: Boolean,
    initIndex: Int = 0,
    animationDuration: Int = 1000,
    onFinish: () -> Unit,
    greeting: ShowcaseMsg? = null,
    targetShape: TargetShape = TargetShape.ROUNDED_RECTANGLE,
    cornerRadius: Dp = 8.dp,
    targetPadding: Dp = ShowcaseLayoutDefaults.TargetPadding,   // 4.dp; 1.3.0
    animateToNextTarget: Boolean = true,
    advanceOnTargetTapOnly: Boolean = false,
    colors: ShowcaseLayoutDefaults.Colors = ShowcaseLayoutDefaults.colors(),
    content: @Composable ShowcaseScope.() -> Unit,
)
```

The parameters are the same as `ShowcaseLayout`, with these differences:

| Param / behavior | TargetShowcaseLayout |
|---|---|
| `animateToNextTarget` | `true`: the highlight morphs position and size to the next target. `false`: it shrinks away and expands at the next one. Single items always expand. |
| Pulse | A ring pulses outward from the target forever while a step shows, colored `colors.pulseColor` (default white). |
| Arrows | **Not drawn.** `ShowcaseMsg.arrow` is ignored and a WARNING event is logged. |
| `lineThickness` | Deprecated in 1.3.0; it never had an effect here. The overload that takes it still compiles, with a warning. |
| Text message on a step | No card. Drawn above or below the target, inside the dimmed area, max width `min(screenW − 73dp, max(screenW, screenH) − 144dp)`. |
| `msgBackground` on a step | Becomes that step's overlay color at 90% alpha (it ignores `overlayColor`'s alpha). It animates between steps, so each step can tint the screen differently. |
| Text greeting | Centered at 80% of the screen width. `msgBackground` (if set) draws a padded box behind it with `roundedCorner`, and also colors the overlay. |
| `gravity` | `Top`/`Bottom` are preferences: if the message doesn't fit on that side, it goes on the other. `Auto` prefers above. |
| Enter/exit fade | Uses the message's `enterAnim`/`exitAnim` duration (1.3.0). On ≤1.2.x it was always `animationDuration / 3`. |

## ShowcaseScope (content receiver)

```kotlin
fun Modifier.showcase(index: Int, message: ShowcaseMsg?, canProceed: Boolean = true, onProceed: (() -> Unit)? = null): Modifier
@Composable fun Showcase(index: Int, message: ShowcaseMsg?, canProceed: Boolean = true, onProceed: (() -> Unit)? = null, itemContent: @Composable () -> Unit)
suspend fun showcaseItem(index: Int)
suspend fun showGreeting(message: ShowcaseMsg)
fun registerEventListener(eventListener: ShowcaseEventListener)
var showcaseEventListener: ShowcaseEventListener?
```

- **`Modifier.showcase`** registers the element's bounds, at its position in the modifier chain,
  under `index`. Indices must be ≥ 1.
  - On ≤1.2.x, `x.showcase(...)` chained after other modifiers applied `x` twice. Write
    `Modifier.showcase(...).then(x)` or `x.then(Modifier.showcase(...))`, which work in every
    version.
  - `message = null` highlights the element without a message.
  - `canProceed = false` locks the step (see SKILL.md section 7).
  - `onProceed` fires once when the step ends: a tap, `next`/`previous`/`skip`, the lock condition
    becoming true, the tour finishing, or a single item being dismissed.
- **`Showcase(index, message) { content }`** is the older wrapper form. It adds a `Box` around
  `content`, which can change layout (alignment, weight). Prefer the modifier.
- **`showcaseItem(index)`** shows one registered target, ignoring `isShowcasing`. A tap (or `next()`)
  dismisses it. It respects `canProceed`.
- **`showGreeting(msg)`** shows a full-screen message with no target. A tap dismisses it.
- Don't call `showcaseItem` or `showGreeting` while a tour is running. All three modes drive the
  same step counter.

## ShowcaseMsg

```kotlin
// text
ShowcaseMsg(
    text: String,                       // or AnnotatedString for bold spans, paragraph styles, ...
    textStyle: TextStyle = TextStyle.Default,  // 1.3.0: unspecified color = automatic white/black
    msgBackground: Color? = null,
    roundedCorner: Dp = 0.dp,
    gravity: Gravity = Gravity.Auto,
    arrow: Arrow? = null,
    enterAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    exitAnim: MsgAnimation = MsgAnimation.FadeInOut(),
)

// custom composable (1.2.0+)
ShowcaseMsg(
    gravity: Gravity = Gravity.Auto,
    arrow: Arrow? = null,
    enterAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    exitAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    msgBackground: Color? = null,       // TargetShowcaseLayout only: the step's overlay color
) { /* @Composable ShowcaseMsgScope.() -> Unit */ }
```

| Field | ShowcaseLayout | TargetShowcaseLayout |
|---|---|---|
| `textStyle` | Used for text. The color is automatic when unspecified (1.3.0); on ≤1.2.x the default is black. | Same |
| `msgBackground` (text) | A card behind the text, padded 6.5dp, corners `roundedCorner`. The card fades in *before* the text. | The step's overlay color (90% alpha). Greeting: a box behind the text. |
| `msgBackground` (custom) | Ignored | The step's overlay color |
| `roundedCorner` | The card corners | Greeting box corners only |
| `gravity` | Forced Top/Bottom. Auto = above if it fits. | Preference, flips if needed |
| `arrow` | Drawn | Ignored (WARNING logged) |
| `enterAnim` / `exitAnim` | `FadeInOut(ms)` or `None` | Same (durations honored from 1.3.0) |

`ShowcaseMsg` is a data class. Build messages in composition, or in plain functions like the
`tourStep()` helper in SKILL.md. Lambdas can't be compared, so custom messages are never "equal";
that's fine.

## ShowcaseMsgScope (custom message receiver)

```kotlin
interface ShowcaseMsgScope {
    fun next()              // next step; last step → finish (onFinish). Dismisses a single item/greeting.
    fun previous()          // back one step; no-op when !hasPrevious
    val hasPrevious: Boolean // index > initIndex in a tour; always false for single item/greeting. Snapshot-backed.
    fun skip()              // end the tour now (onFinish). Dismisses a single item/greeting.
}
```

None of these are blocked by `advanceOnTargetTapOnly` or by a locked step.

## Arrow, Head, Side

```kotlin
data class Arrow(
    val targetFrom: Side = Side.Bottom,   // which side of the target the arrow enters from
    val curved: Boolean = false,          // curve from the (horizontally centered) message to the target
    val animationDuration: Int = 1000,    // ms to draw the line; the head then grows if animSize
    val head: Head? = Head.TRIANGLE,      // CIRCLE, TRIANGLE, SQUARE, ROUND_SQUARE, or null for a plain line
    val headSize: Dp = 9.dp,              // 1.3.0; Float px (default 25f) before
    val color: Color = Color.White,
    val animSize: Boolean = true,         // grow the head from 0 on enter, shrink on exit
)
```

`ShowcaseLayout` only. The geometry is all in dp from 1.3.0:

- **Text message, non-circle target, straight arrow:** the line runs from a fixed point (65dp above
  for `Side.Top`, 73dp away for the other sides) to the target edge, stopping `headSize + 9dp` short.
  It doesn't start at the text, so keep text short.
- **Circle target:** the line starts at the message card edge.
- **Custom composable message:** the line starts at the middle of the card edge facing the target.
- **A `Side.Top`/`Side.Bottom` arrow follows the message (1.3.0):** it comes from whichever side the
  message ended up on, so `Gravity.Auto` or a contradicting `gravity` still gives a matching arrow.
  On ≤1.2.x only custom messages did this; a text message used `targetFrom` as given, and the
  defaults (`Side.Bottom` with `Gravity.Auto`, text usually above) drew the arrow on the far side.
- **`targetFrom = Side.Left/Right`:** kept as given. The message is still above or below; the arrow
  just enters from the side, which suits targets near a screen edge.

## Gravity, TargetShape, MsgAnimation

- `Gravity.Top`, `Gravity.Bottom`, `Gravity.Auto`. This is the library's `model.Gravity`, not
  `android.view.Gravity`.
- `TargetShape.CIRCLE` (radius = max(w, h) / 2), `RECTANGLE`, `ROUNDED_RECTANGLE` (radius clamped to
  half the smaller side). The cutout is the target's bounds plus `targetPadding` (1.3.0, 4dp), with
  the corners grown by the same amount. A `cornerRadius` larger than the element's own corners, or a
  circle on a rounded square, still clips the element (SKILL.md rule 7).
- `MsgAnimation.FadeInOut(duration = 500)`, `MsgAnimation.None`.

## ShowcaseLayoutDefaults.colors

```kotlin
@Composable
fun colors(
    overlayColor: Color = Color.Black.copy(alpha = 0.9f),
    pulseColor: Color = Color.White,          // TargetShowcaseLayout only
): ShowcaseLayoutDefaults.Colors
```

Good starting points:

- Default: `Color.Black.copy(alpha = 0.9f)`.
- Brand: `brandDark.copy(alpha = 0.9f)` with `pulseColor = brandAccent`.
- Dark UI: `Color.White.copy(alpha = 0.92f)` with dark message text and a saturated `pulseColor`.
- Material 3: `MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f)`, or `inverseSurface` for a
  light-on-dark look.


`ShowcaseLayoutDefaults.TargetPadding` is the default `targetPadding` (4dp, 1.3.0).

## Event listener

```kotlin
registerEventListener(object : ShowcaseEventListener {
    override fun onEvent(level: Level, event: String) {
        if (level != Level.VERBOSE) println("Tour: $event")  // or Log.d / Napier / Kermit
    }
})
```

Levels:

- `ERROR` / `WARNING`: e.g. an arrow passed to `TargetShowcaseLayout`.
- `INFO`: moving to an index, finished, locked, unlocked, placement decisions.
- `DEBUG`: single item or greeting requested.
- `VERBOSE`: every recomposition or draw; very noisy.

## How a step advances

| Input | Normal step | Locked step (`canProceed = false`) | Greeting |
|---|---|---|---|
| Tap anywhere on the overlay | Next (or finish on the last) | Ignored | Next |
| Same, with `advanceOnTargetTapOnly = true` | Only taps inside the target count | Ignored | Next on any tap |
| Tap on the target's bounds | Treated as an overlay tap (the element doesn't receive it) | **Passes through to the element** | — |
| Tap on a non-clickable part of a custom card | Same as an overlay tap | Ignored | Same as an overlay tap |
| `next()` / `previous()` / `skip()` | Work | Work (bypass the lock) | Work |
| `canProceed` becomes true | — | Auto-advance | — |

The system back button isn't handled by the layout. See `message-cards.md` → "Ending the tour from
outside".

## Message placement and size limits

| | ShowcaseLayout | TargetShowcaseLayout |
|---|---|---|
| Text width | screen − 33dp | `min(screenW − 73dp, max(screenW, screenH) − 144dp)`; greeting 80% of the width |
| Custom card width | Measured at most screen − 33dp, then clamped on screen | Measured at most the same as text, then kept ≥ 54dp from the side edges |
| Gap to the target | Text: 84dp to the text top-left (circle targets add radius + 18dp). Custom: 77.5dp to the near edge. | 54dp from the target edge, kept inside the screen |
| Greeting | Centered | Centered |

## Migrating to 1.3.0

| Before | After |
|---|---|
| `Arrow(headSize = 25f)` (px) | `Arrow(headSize = 9.dp)`. Divide the old value by about 2.75 for the same look on a typical phone. **Doesn't compile until changed.** |
| `TargetShowcaseLayout(..., lineThickness = x)` | Remove `lineThickness` (deprecated, never had an effect) |
| Message spacing in fixed px | dp. Looks the same on ~2.75x phones; desktop, web and tablets now get consistent spacing. |
| Default text black | Unspecified → automatic white/black. Explicit colors are unchanged. |
| `TargetShowcaseLayout` fade = `animationDuration / 3`; `ShowcaseLayout` exit used `enterAnim.duration` | Both honor `enterAnim` / `exitAnim` durations |
| `handleMessageEnterAnimation` / `handleMessageExitAnimation` (public by mistake) | Deprecated; don't call them |
| `Modifier.padding(..).showcase(..)` applied `padding` (and anything else before `.showcase`) twice | Fixed: the chained form registers the element's real bounds |
| `TargetShowcaseLayout` started with `isShowcasing = true` on the first frame never showed a custom card on step 1 | Fixed |
| `ShowcaseLayout` started before its first target was laid out: the cutout flew in from the top-left corner | Fixed: the cutout appears on its first target |
| The cutout was exactly the element's bounds, so an elevated element (a FAB) lost its shadow and looked cropped | `targetPadding` (default 4dp) adds space around every target. Pass `targetPadding = 0.dp` for the old exact fit |
| A text message's arrow used `targetFrom` as given, so `Side.Bottom` (default) with text above came from the far side | `Side.Top`/`Side.Bottom` follow the message's side, as custom messages already did |

## Older versions

If the project can't upgrade, write against what it has:

- **≤1.2.x:**
  - `headSize` is a pixel `Float` (`headSize = 25f`).
  - Start modifier chains with `Modifier.showcase(...)`, or use `.then(Modifier.showcase(...))`;
    never `x.showcase(...)` after other modifiers.
  - Start tours from `LaunchedEffect`, not with `isShowcasing = true` on the first frame.
  - Always set `textStyle = TextStyle(color = ...)`.
  - Don't pass `lineThickness` to `TargetShowcaseLayout` (it's ignored).
  - Spacing is in px, so it looks big on 1x desktop/web and tight on dense tablets.
- **≤1.1.x:**
  - No custom composable messages, `ShowcaseMsgScope`, `canProceed` or `onProceed`.
  - Messages are text only.
  - A tap or `advanceOnTargetTapOnly` are the only ways to advance.
  - `TargetShowcaseLayout` ignores `gravity`.
- **1.0.x:**
  - Use `isDarkLayout = true/false` instead of `colors`.
  - No `advanceOnTargetTapOnly`.
  - `targetShape` on `ShowcaseLayout` needs 1.0.7+.
  - `TargetShowcaseLayout` needs 1.0.6+.

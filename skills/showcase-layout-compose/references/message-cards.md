# Message cards and text messages

## Contents
- [Text messages](#text-messages)
- [Tour cards (custom composable messages)](#tour-cards-custom-composable-messages)
- [Welcome card (greeting)](#welcome-card-greeting)
- [Ending the tour from outside the card](#ending-the-tour-from-outside-the-card)
- [Copy and localization](#copy-and-localization)

The templates use Material 3, so adapt them to the project's design system:

- **Material 2:** use `MaterialTheme.colors` / `typography.h6` / `body2`.
- **Custom theme:** use its tokens.

Keep the structure, which is what makes them work well inside a showcase. The cards get a fixed
width, use surface colors that stand out against the overlay, and hold the Back/Skip/Next logic.

## Text messages

Use text for one-off hints and very simple tours. The text is drawn on the overlay's canvas, so
it's cheap, but screen readers don't see it.

**ShowcaseLayout: a card behind the text, with an arrow.** Match `targetFrom` to `gravity`.

```kotlin
ShowcaseMsg(
    "Start a new conversation from here",
    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onPrimary),
    msgBackground = MaterialTheme.colorScheme.primary,
    roundedCorner = 12.dp,
    gravity = Gravity.Top,
    arrow = Arrow(targetFrom = Side.Top, head = Head.CIRCLE, color = MaterialTheme.colorScheme.primary),
)
```

**TargetShowcaseLayout: the text sits on the dimmed area.** There's no card and no arrow.

```kotlin
ShowcaseMsg(
    "Swipe a card left to archive it",
    textStyle = MaterialTheme.typography.titleMedium,   // 1.3.0: unspecified color → white/black automatically
    gravity = Gravity.Bottom,
)
```

**Rich text** (bold spans, centered paragraphs) uses an `AnnotatedString`:

```kotlin
val text = buildAnnotatedString {
    withStyle(ParagraphStyle(textAlign = TextAlign.Center)) {
        append("Welcome to ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Notes") }
        append("\nTap anywhere to continue")
    }
}
ShowcaseMsg(text, textStyle = MaterialTheme.typography.titleLarge)
```

Material `typography` styles leave the color unspecified, so on 1.3.0 the automatic contrast
applies. On ≤1.2.x the default is black: add `.copy(color = Color.White)` for a dark overlay or
`Color.Black` for a light one.

## Tour cards (custom composable messages)

```kotlin
import ly.com.tahaben.showcase_layout_compose.model.ShowcaseMsgScope

/**
 * The message card of one tour step: progress, title, body and Back / Skip / Next.
 * A [locked] step hides Next and shows [lockedHint]: the tour continues by itself once the user
 * performs the action (canProceed on the step's Modifier.showcase), Skip stays as an escape hatch.
 */
@Composable
fun TourCard(
    step: Int,
    total: Int,
    title: String,
    body: String,
    scope: ShowcaseMsgScope,
    locked: Boolean = false,
    lockedHint: String = "Try it to continue",
) {
    Card(
        modifier = Modifier.width(300.dp),          // fixed width: never fillMaxWidth inside a showcase
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepDots(current = step, total = total)
                Spacer(Modifier.weight(1f))
                Text(
                    "$step of $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (locked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        lockedHint,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { scope.skip() }) { Text("Skip") }
                }
            } else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // with a greeting, step 1's "previous" is the greeting: don't offer Back there
                    if (step > 1 && scope.hasPrevious) {
                        TextButton(onClick = { scope.previous() }) { Text("Back") }
                    }
                    Spacer(Modifier.weight(1f))
                    if (step < total) TextButton(onClick = { scope.skip() }) { Text("Skip") }
                    Button(onClick = { scope.next() }) { Text(if (step == total) "Done" else "Next") }
                }
            }
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            val active = i + 1 == current
            Box(
                Modifier
                    .height(6.dp)
                    .width(if (active) 18.dp else 6.dp)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(50),
                    )
            )
        }
    }
}
```

Use it from a step. The lambda's receiver is the `ShowcaseMsgScope`:

```kotlin
Modifier.showcase(
    index = 2,
    message = ShowcaseMsg(gravity = Gravity.Bottom) {
        TourCard(step = 2, total = 4, title = "Filters", body = "Narrow the list down.", scope = this)
    },
)
```

Notes:

- **Total step count.** Keep it in one constant (`private const val TOUR_STEPS = 4`), so the dots
  and the "Done" label stay right when steps are added.
- **Taps that fall through.** Taps on the card's text or padding advance the tour like a tap on the
  overlay. That's intended. If it surprises users on a step, set `advanceOnTargetTapOnly = true` on
  the layout so only the target or the buttons advance.
- **Arrows on cards (`ShowcaseLayout`).** Pass
  `arrow = Arrow(color = ..., head = Head.CIRCLE, headSize = 6.dp)` to `ShowcaseMsg(...) { }`. The
  arrow starts at the card edge facing the target.
- **Per-step overlay tint (`TargetShowcaseLayout`).** `ShowcaseMsg(msgBackground = color) { ... }`.

## Welcome card (greeting)

```kotlin
@Composable
fun WelcomeCard(
    scope: ShowcaseMsgScope,
    title: String = "Welcome!",
    body: String = "A quick look around. It takes less than a minute.",
) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = { scope.next() }, modifier = Modifier.fillMaxWidth()) { Text("Start the tour") }
            TextButton(onClick = { scope.skip() }) { Text("Maybe later") }   // skip() → onFinish
        }
    }
}

// on the layout:
greeting = ShowcaseMsg(enterAnim = MsgAnimation.FadeInOut(400)) { WelcomeCard(scope = this) }
```

"Maybe later" calls `skip()`, which calls `onFinish`. Decide whether that counts as "seen". It
usually does, because users shouldn't be nagged on every launch. If it shouldn't, track the reason
yourself: set a flag in the button before calling `skip()`.

## Ending the tour from outside the card

The layout doesn't handle the system back button, and there's no public "stop" call. Setting
`isShowcasing = false` mid-tour leaves the layout on its current step for the next run. To end
cleanly from outside the overlay (back press, a close button in the app bar, an autoplay timer),
let the card on screen publish its scope and call `skip()` on it:

```kotlin
/** The ShowcaseMsgScope of the card on screen, so code outside the overlay can drive the tour. */
class TourHandle { var scope: ShowcaseMsgScope? = null }

val tourHandle = remember { TourHandle() }

// inside TourCard / WelcomeCard (pass the handle in):
SideEffect { handle.scope = scope }

// back press while the tour runs: end it the same way the Skip button does
BackHandler(enabled = isShowcasing) { tourHandle.scope?.skip() }
```

- **Android:** `androidx.activity.compose.BackHandler`.
- **Compose Multiplatform:** `androidx.compose.ui.backhandler.BackHandler`, with
  `@OptIn(ExperimentalComposeUiApi::class)`.
- With text-only messages there's no scope to call. Use cards if the tour must be cancellable from
  outside.

## Copy and localization

Put tour strings in the project's string resources if it has them (Android `strings.xml`, or
Compose Multiplatform `Res.string` with `stringResource`). Messages are built in composition, so
`stringResource(...)` works inside `ShowcaseMsg { ... }` and when building text messages.

Keep copy short:

- a title of 2–5 words
- a body of one or two sentences that says what the element does for the user
- for locked steps, a hint phrased as an action ("Tap the eye icon to continue")

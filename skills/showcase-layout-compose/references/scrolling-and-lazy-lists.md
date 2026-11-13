# Scrolling screens and lazy lists

## Contents
- [What you're working with](#what-youre-working-with)
- [Pick an approach](#pick-an-approach)
- [Targets visible at the start (best)](#targets-visible-at-the-start-best)
- [Non-lazy scrolling content: scroll in onProceed](#non-lazy-scrolling-content-scroll-in-onproceed)
- [A far-away lazy item: finish the tour, then scroll and show it](#a-far-away-lazy-item-finish-the-tour-then-scroll-and-show-it)
- [ShowcaseLayout only: scroll a lazy list in onProceed](#showcaselayout-only-scroll-a-lazy-list-in-onproceed)

## What you're working with

These facts were verified on both layouts:

- **A step's highlight position is taken when the step starts.** It doesn't follow a target that
  moves later. If you scroll during a step, the cutout stays where the element was.
- **A target only exists once it has been composed and laid out.** Lazy items that are off screen
  aren't composed, so they aren't registered. The tour counts registered targets, so it **ends
  before** an unregistered step: `onFinish` is called and the step is never shown.
- **Registrations stay after an element leaves composition.** An item scrolled away keeps its last
  known position, so a later step on it highlights the old spot.
- **There's a window for scrolling between two steps.** When a step ends, its message fades out.
  That's `exitAnim`, `FadeInOut` at 500 ms by default. The next step's position is taken after
  that fade, so a scroll started in the ending step's `onProceed` has landed by then. With
  `MsgAnimation.None` there's no window.
- **`TargetShowcaseLayout` decides "next step or finish" at the moment of the tap.** A lazy item
  scrolled into view during the fade is too late for it. `ShowcaseLayout` decides after the fade.

## Pick an approach

| Situation | Approach |
|---|---|
| Every target is on screen when the tour starts | Nothing special (best) |
| Targets are in a `Column(Modifier.verticalScroll(...))` or similar non-lazy container, some below the fold | Scroll in the previous step's `onProceed` |
| One target is far down a `LazyColumn`/`LazyRow`/grid/pager (either layout) | Finish the tour on the visible targets, then scroll and `showcaseItem` the far one |
| Same, and the screen uses `ShowcaseLayout` | Or scroll the list in the previous step's `onProceed` |

Tell the developer which approach you used and why, especially when a requested element is
highlighted separately or replaced by an always-visible entry point.

## Targets visible at the start (best)

- **Explain a list by highlighting its first visible item** ("Each row is a payment; tap one for
  the receipt") rather than a specific item further down.
- **Highlight always-composed entry points** (top bar actions, FAB, bottom bar) instead of
  far-away items.
- **Start the tour while the list is at the top,** right after it loads. If the screen can open
  mid-list (restored scroll position), scroll to the top before setting `isShowcasing = true`.

## Non-lazy scrolling content: scroll in onProceed

Every child of a non-lazy container is composed, so every target is registered. You only have to
bring the next target into view before its step starts. Do it from the previous step's
`onProceed`, with an instant scroll:

```kotlin
val scrollState = rememberScrollState()
val scope = rememberCoroutineScope()
val margin = with(LocalDensity.current) { 32.dp.roundToPx() }
var chartTop by remember { mutableIntStateOf(0) }

TargetShowcaseLayout(isShowcasing = isShowcasing, onFinish = { isShowcasing = false; onTourSeen() }) {
    Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
        BalanceHeader(
            modifier = Modifier.showcase(
                index = 1,
                message = tourStep { TourCard(1, 2, "Balance", "Your total across accounts.", this) },
                // step 1 is ending: bring step 2's target into view during the exit fade
                onProceed = { scope.launch { scrollState.scrollTo((chartTop - margin).coerceAtLeast(0)) } },
            ),
        )
        // ... long content ...
        SpendingChart(
            modifier = Modifier
                .onGloballyPositioned { chartTop = it.positionInParent().y.roundToInt() }  // y inside the Column
                .then(Modifier.showcase(2, tourStep { TourCard(2, 2, "Spending", "Where your money went.", this) })),
        )
    }
}
```

- **Use an instant scroll (`scrollTo`, `scrollToItem`).** An animated scroll (`animateScrollTo`)
  only works if it finishes within the fade. It worked with the default 500 ms fade and failed with
  `MsgAnimation.None`.
- **Keep the ending step's `exitAnim` a `FadeInOut`** (the default). With `MsgAnimation.None`,
  `TargetShowcaseLayout` takes the next position before the scroll lands.
- **`onProceed` also runs on `skip()` and `previous()`.** A scroll on skip is harmless. But going
  back from a later step doesn't scroll back, so hide "Back" in tours that scroll.

## A far-away lazy item: finish the tour, then scroll and show it

This works in both layouts. Run the tour on the visible targets, then scroll the list and show the
far item as a single hint. Register the far item only after the tour, so it can't join the tour at
a stale position:

```kotlin
val listState = rememberLazyListState()
var isShowcasing by remember { mutableStateOf(false) }
var showExportHint by remember { mutableStateOf(false) }
val animationDuration = 700

TargetShowcaseLayout(
    isShowcasing = isShowcasing,
    animationDuration = animationDuration,
    onFinish = { isShowcasing = false; showExportHint = true },
) {
    LaunchedEffect(showExportHint) {
        if (!showExportHint) return@LaunchedEffect
        delay(animationDuration + 500L)    // ShowcaseLayout resets its step animationDuration + 250 ms after onFinish
        listState.scrollToItem(exportItemIndex)
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.index == exportItemIndex } }.first { it }
        withFrameNanos { }                 // one frame so the item registers its bounds
        showcaseItem(3)
    }
    LazyColumn(state = listState) {
        item { SummaryCard(Modifier.showcase(1, msgSummary)) }
        itemsIndexed(transactions) { i, tx ->
            TransactionRow(tx, if (i == 0) Modifier.showcase(2, msgRow) else Modifier)
        }
        item {
            ExportButton(
                modifier = if (showExportHint) {
                    Modifier.showcase(3, msgExport, onProceed = { onTourSeen() })   // runs when the hint is dismissed
                } else Modifier,
            )
        }
    }
}
```

- **The wait after `onFinish` is required for `ShowcaseLayout`.** It resets its step counter
  `animationDuration + 250 ms` after `onFinish`, and a single item started before that is
  overwritten. `TargetShowcaseLayout` needs only the frame wait, but keeping the delay doesn't hurt.
- **Persist "seen" once the last part has been shown,** for example in the far item's `onProceed`.
  If the user skips the main tour, decide whether the hint should still follow. Usually it
  shouldn't: track the skip and leave `showExportHint` false.
- **For a `LazyRow`, grid or pager,** use the same pattern with its own state (`LazyGridState`,
  `PagerState.scrollToPage`).
- **Replays need a fresh layout.**
  - Once the far item has registered, it stays registered at its old position for as long as the
    layout lives, even after `showExportHint` goes back to false. A replayed tour would then
    include it as a normal step, at a stale spot.
  - Hoist the screen state (`listState`, and anything else the content remembers) above the layout,
    and wrap the layout in `key(tourRun) { ... }`. Increment `tourRun` (and reset `showExportHint`)
    when the user asks for the tour again.
  - Scroll the list back to the top before starting the replay.

## ShowcaseLayout only: scroll a lazy list in onProceed

`ShowcaseLayout` checks for the next step after the exit fade, so the far item can join the tour if
it's scrolled in during that fade:

```kotlin
TransactionRow(
    tx,
    Modifier.showcase(
        index = 2,
        message = msgRow,          // exitAnim must stay FadeInOut (the default)
        onProceed = { scope.launch { listState.scrollToItem(exportItemIndex) } },
    ),
)
// ... the export item: Modifier.showcase(3, msgExport)
```

This fails in `TargetShowcaseLayout` (the tour ends after step 2) and with `MsgAnimation.None`. If
the screen might switch layouts, use the previous approach.

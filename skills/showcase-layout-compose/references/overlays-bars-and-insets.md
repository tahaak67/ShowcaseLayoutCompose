# Overlays, bars and insets

## Contents
- [Where the layout goes](#where-the-layout-goes)
- [Top bars, bottom bars, FABs and navigation](#top-bars-bottom-bars-fabs-and-navigation)
- [Dialogs, bottom sheets, menus and popups](#dialogs-bottom-sheets-menus-and-popups)
- [Edge-to-edge and system bars](#edge-to-edge-and-system-bars)
- [The soft keyboard](#the-soft-keyboard)
- [Snackbars, toasts and other feedback](#snackbars-toasts-and-other-feedback)

## Where the layout goes

The overlay covers exactly the bounds of the layout's parent. The layout composes your content,
then adds a `fillMaxSize()` overlay *as a sibling*. That gives two requirements:

1. **The parent must stack its children.**
   - Good parents:
     - the root of `setContent { }`, `ComposeViewport { }` or `Window { }`
     - `Box`, `Surface`, `Crossfade`, `AnimatedContent`
     - a navigation destination's content
   - Bad parents: `Column`, `Row`, `LazyColumn` items, `ColumnScope` slots (a `ModalBottomSheet`'s
     content, `Card`'s content). In these the overlay is laid out after the content instead of on
     top of it.
   - When in doubt, wrap it:

   ```kotlin
   Box(Modifier.fillMaxSize()) {
       TargetShowcaseLayout(isShowcasing = isShowcasing, onFinish = { isShowcasing = false }) {
           Screen()
       }
   }
   ```

2. **Everything you want dimmed and everything you highlight must be inside it.** Elements outside
   the layout's bounds stay bright. Elements outside its content lambda can't call
   `Modifier.showcase`.

## Top bars, bottom bars, FABs and navigation

- **Wrap the whole `Scaffold`, not just the scaffold's content.** The `topBar`, `bottomBar` and
  `floatingActionButton` slot lambdas are nested inside the content lambda, so `Modifier.showcase`
  works there directly.
- **App-shell navigation:** a shell `Scaffold` holds a `NavigationBar` while each destination has
  its own `Scaffold`.
  - A layout inside a destination can't dim or highlight the shell's navigation bar.
  - To include the nav bar in a tour, put the layout around the shell `Scaffold`, at the level that
    contains both.
  - Pass the target modifiers down to destinations, or expose a `ShowcaseScope` through a
    `CompositionLocal`. Keep the tour's state with the shell.
  - Otherwise, keep tours per destination and leave the nav bar out.
- **`NavigationBarItem` / `BottomNavigationItem`:**
  - Put `Modifier.showcase` on the item's `modifier`. That highlights the whole item cell (icon and
    label).
  - To highlight just the icon, put it on the `Icon` inside the `icon` slot. A glyph has no visible
    edge, so any shape works for it. If the tour also highlights cards or a FAB, keep `cornerRadius`
    at or below their corner radius (SKILL.md rule 7).
- **Large top app bars that collapse while scrolling:** start the tour before the user scrolls, or
  scroll back to the top first. A collapsed bar moves its actions.

## Dialogs, bottom sheets, menus and popups

`Dialog`, `AlertDialog`, `ModalBottomSheet`, `DropdownMenu`, `ExposedDropdownMenu`, `Popup` and
tooltips render in **their own window**, above the screen's overlay.

- **The screen's overlay can't dim them, and their elements can't be targets** of the screen's
  layout. Their positions live in another window, so the highlight would land in the wrong place.
- **To explain the element that opens them,** highlight the button, chip or field that opens the
  dialog or menu, and describe what will appear.
- **To tour the contents of a sheet or dialog,** give it its own layout inside its content,
  wrapped in a `Box` (sheet content is a `ColumnScope`). Use separate `isShowcasing` state, started
  once the sheet is open. It dims only the sheet.

  ```kotlin
  ModalBottomSheet(onDismissRequest = onDismiss) {
      Box {
          var sheetTour by remember { mutableStateOf(false) }
          LaunchedEffect(Unit) { delay(400); sheetTour = !sheetTourSeen }
          TargetShowcaseLayout(isShowcasing = sheetTour, onFinish = { sheetTour = false; onSheetTourSeen() }) {
              SheetContent(applyButtonModifier = Modifier.showcase(1, ShowcaseMsg("Apply your filters here")))
          }
      }
  }
  ```

- **Close a menu or dialog before a tour starts.** Otherwise it sits above the overlay.

## Edge-to-edge and system bars

- A layout at the window root, with edge-to-edge enabled, draws its overlay behind the status and
  navigation bars too, so the whole window dims.
- Targets are converted into the layout's own coordinates. From 1.2.0, a layout that doesn't start
  at the window origin still lines up: inset padding, a parent top bar, a split pane. On ≤1.1.x the
  highlight was offset by the layout's distance from the window origin, which is another reason to
  upgrade.
- Messages aren't inset-aware. The overlay covers the full window, so a message can be placed under
  the status bar, a display cutout, or the gesture navigation area:
  - Targets near the top: `gravity = Gravity.Bottom`.
  - Targets near the bottom (bottom bar, FAB): `gravity = Gravity.Top`.
  - `TargetShowcaseLayout` flips to the other side when the preferred side doesn't fit.
    `ShowcaseLayout` doesn't flip, so set gravity explicitly there.

## The soft keyboard

Locked steps let touches reach the target, so a text field can be focused during the tour and the
keyboard opens normally. Close it when the step ends:

```kotlin
val keyboard = LocalSoftwareKeyboardController.current
val focusManager = LocalFocusManager.current
Modifier.showcase(
    index = 3,
    message = ...,
    canProceed = sentDuringTour > 0,
    onProceed = { focusManager.clearFocus(); keyboard?.hide() },  // also covers skip()/next()
)
```

- **The keyboard can push a field out of its own highlight.**
  - The highlight and the locked step's touch hole are taken when the step starts, and they don't
    follow a target that moves. This was verified in both layouts.
  - When the keyboard pushes a field up (`imePadding()`, `adjustResize`, edge-to-edge IME insets),
    the field ends up outside the cutout.
  - The field keeps focus, so typing still works.
  - But a second tap on the field, or on a Send button that moved with it, is swallowed by the
    overlay.
- **For bottom-anchored fields** (chat composers, bottom search bars):
  - Let the condition complete from the keyboard: an IME action (`ImeAction.Send` / `Done` with
    `KeyboardActions`) that does the same as the button, or a `canProceed` based on the typed text
    itself.
  - Or lock a field that doesn't move when the keyboard opens.
  - Check on a device.
- **Place the message on the side away from the keyboard**, usually `Gravity.Top`.
- **Don't open the keyboard programmatically when the step starts.** Let the user tap the field.
  That's the action the step teaches.

## Snackbars, toasts and other feedback

- A `Snackbar` in the `Scaffold` inside the layout is dimmed by the overlay. Show
  "You can replay the tour from the ? menu" from `onFinish`, after the overlay has faded.
- Avoid navigation, permission prompts or system dialogs while a tour runs. Finish or `skip()`
  first.

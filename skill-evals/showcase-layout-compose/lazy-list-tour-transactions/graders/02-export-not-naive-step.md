---
type: llm
focus: trace
weight: 1
---

The Export CSV button (last item of the LazyColumn, off screen when the tour starts) is handled with an approach that works: it is scrolled into view and then shown with showcaseItem after the tour, OR the screen uses ShowcaseLayout with an instant scroll (scrollToItem) in step 2's onProceed and a FadeInOut exit, OR an always-composed export entry point is highlighted instead. Simply registering the off-screen lazy item as step 3 of the running tour (with TargetShowcaseLayout, or with no scrolling) fails this check.

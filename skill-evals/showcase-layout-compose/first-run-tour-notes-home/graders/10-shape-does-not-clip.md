---
type: llm
focus: trace
weight: 1
---

The highlight shape doesn't clip the new-note FloatingActionButton (a Material 3 rounded square with 16dp corners): the layout uses RECTANGLE (ShowcaseLayout's default), or ROUNDED_RECTANGLE with cornerRadius of 16.dp or less (TargetShowcaseLayout's default is 8.dp), and not TargetShape.CIRCLE.

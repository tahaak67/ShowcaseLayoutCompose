---
type: llm
focus: trace
weight: 1
---

The tour only starts when the existing SettingsStore says it hasn't been seen, the seen flag is saved through SettingsStore, and onFinish makes isShowcasing false (directly or through derived state).

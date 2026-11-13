---
tags: [showcase-layout-compose]
max_turns: 40
timeout_seconds: 900
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

Add a quick tour to TransactionsScreen that explains the monthly summary card, a transaction row, and the 'Export CSV' button at the bottom of the list. Show it the first time the screen opens.

Input files: gradle/libs.versions.toml, composeApp/build.gradle.kts, composeApp/src/commonMain/kotlin/com/example/app/App.kt, composeApp/src/commonMain/kotlin/com/example/app/data/SettingsStore.kt, composeApp/src/commonMain/kotlin/com/example/app/ui/TransactionsScreen.kt

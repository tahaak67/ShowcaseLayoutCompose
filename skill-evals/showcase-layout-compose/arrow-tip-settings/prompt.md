---
tags: [showcase-layout-compose]
max_turns: 40
timeout_seconds: 900
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

Show a one-time tip on the settings screen with an arrow pointing at the new Backup row the first time someone opens settings. Just a short text message, no multi-step tour.

Input files: gradle/libs.versions.toml, composeApp/build.gradle.kts, composeApp/src/commonMain/kotlin/com/example/app/App.kt, composeApp/src/commonMain/kotlin/com/example/app/data/SettingsStore.kt, composeApp/src/commonMain/kotlin/com/example/app/ui/SettingsScreen.kt

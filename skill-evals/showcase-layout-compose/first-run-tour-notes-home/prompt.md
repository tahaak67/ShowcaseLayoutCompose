---
tags: [showcase-layout-compose]
max_turns: 40
timeout_seconds: 900
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

Add a first-run onboarding tour to the notes home screen (composeApp/src/commonMain/kotlin/com/example/app/ui/HomeScreen.kt) using Showcase Layout Compose. It should introduce the search button, the filter chips and the new-note button, and only show the first time the app opens.

Input files: gradle/libs.versions.toml, composeApp/build.gradle.kts, composeApp/src/commonMain/kotlin/com/example/app/App.kt, composeApp/src/commonMain/kotlin/com/example/app/data/SettingsStore.kt, composeApp/src/commonMain/kotlin/com/example/app/ui/HomeScreen.kt, composeApp/src/commonMain/kotlin/com/example/app/ui/FilterChipsRow.kt

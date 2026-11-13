---
tags: [showcase-layout-compose]
max_turns: 40
timeout_seconds: 900
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

In SignUpScreen, add a short tutorial with showcase layout: first highlight the username field and don't let the user move on until they've actually typed a username, then point out the Sign up button. Each step should be a little card with a Skip button.

Input files: gradle/libs.versions.toml, composeApp/build.gradle.kts, composeApp/src/commonMain/kotlin/com/example/app/App.kt, composeApp/src/commonMain/kotlin/com/example/app/ui/WelcomeScreen.kt, composeApp/src/commonMain/kotlin/com/example/app/ui/SignUpScreen.kt

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "ly.com.tahaben.showcaselayoutcomposekmp.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        // Required for Compose Multiplatform resources on Android.
        androidResources { enable = true }
    }

    jvm("desktop")

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            implementation(projects.showcaseLayoutCompose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation("org.jetbrains.compose.ui:ui-backhandler:${libs.versions.compose.plugin.get()}")
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose.resources {
    // webApp reads the emoji font from Res at startup.
    publicResClass = true
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

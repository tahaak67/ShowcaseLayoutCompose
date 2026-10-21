plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.mavenPublish)
}


kotlin {
    jvmToolchain(17)

    android {
        namespace = "ly.com.tahaben.showcaselayoutcompose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        // Matches the minimum required by Compose 1.12.
        minSdk = 23
    }

    jvm("desktop")

    iosArm64()
    iosSimulatorArm64()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "showcase-layout-compose"
        browser()
    }


    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
        }
        getByName("desktopMain").dependencies {
            implementation(compose.desktop.common)
        }
    }
}

mavenPublishing {
    // Uploads to the Central Portal (central.sonatype.com). Credentials and the
    // signing key are read from Gradle properties / env vars (see CI workflow).
    publishToMavenCentral()
    signAllPublications()

    coordinates("ly.com.tahaben", "showcase-layout-compose", "1.1.1")

    pom {
        name.set("Showcase Layout Compose")
        description.set("Showcase Layout allows you to easily showcase and explain compose UI elements to users in a beautiful and attractive way.")
        inceptionYear.set("2024")
        url.set("https://github.com/tahaak67/ShowcaseLayoutCompose")

        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("tahaak67")
                name.set("Taha Ben Ashur")
                email.set("dev@tahaben.com.ly")
                url.set("https://github.com/tahaak67")
            }
        }
        scm {
            url.set("https://github.com/tahaak67/ShowcaseLayoutCompose")
            connection.set("scm:git:git://github.com/tahaak67/ShowcaseLayoutCompose.git")
            developerConnection.set("scm:git:ssh://git@github.com/tahaak67/ShowcaseLayoutCompose.git")
        }
    }
}

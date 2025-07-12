import java.util.*

plugins {
    `maven-publish`
    signing
}

// Stub secrets to let the project sync and build without the publication values set up

ext["signingKeyId"] = null
ext["signingPassword"] = null
ext["signingKey"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

val publishGroupId: String = "ly.com.tahaben"
val publishVersion: String = "1.0.8"
val publishArtifactId: String = "showcase-layout-compose"


// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signingKeyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signingPassword"] = System.getenv("SIGNING_PASSWORD")
    ext["signingKey"] = System.getenv("SIGNING_KEY")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

group = publishGroupId
version = publishVersion

println("group: $group ,version: $version")

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        groupId = publishGroupId
        artifactId = publishArtifactId
        version = publishVersion

        println("publish group id $groupId")
        println("publish artifact id $artifactId")
        println("publish version $version")

        // Provide artifacts information requited by Maven Central
        pom {
            name.set(getExtraString("signingKeyId"))
            description.set("Showcase Layout allows you to easily showcase and explain compose UI elements to users in a beautiful and attractive way.")
            url.set("https://github.com/tahaak67/ShowcaseLayoutCompose")

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("tahaak67")
                    name.set("Taha Ben Ashur")
                    email.set("dev@tahaben.com.ly")
                }
            }
            scm {
                url.set("https://github.com/tahaak67/ShowcaseLayoutCompose/tree/main")
                developerConnection.set("scm:git:ssh://github.com/tahaak67/ShowcaseLayoutCompose.git")
                connection.set("scm:git:github.com/tahaak67/ShowcaseLayoutCompose.git")
            }
        }
    }
}

// Signing artifacts. Signing extra properties values will be used

signing {
    useInMemoryPgpKeys(
        getExtraString("signingKeyId"),
        getExtraString("signingKey"),
        getExtraString("signingPassword"),
    )
    sign(publishing.publications)
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}

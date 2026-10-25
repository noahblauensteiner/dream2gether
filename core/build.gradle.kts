// Shared logic for the phone, the Pi box and the laptop CLI.
// Keep it free of Android and java.io APIs so it can move to Kotlin Multiplatform later.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

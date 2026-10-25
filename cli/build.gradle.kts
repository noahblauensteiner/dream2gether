// Laptop CLI: runs the same processing as the phone, on local whisper.cpp / llama.cpp binaries.
plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("app.dream2gether.cli.MainKt")
    applicationName = "d2g"
}

tasks.test { useJUnitPlatform() }

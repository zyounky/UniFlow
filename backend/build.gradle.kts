plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.example.uniflow"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

tasks.register<JavaExec>("runBot") {
    group = "application"
    description = "Runs the UniFlow Telegram bot"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.uniflow.bot.BotMainKt")
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.client.cio)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(libs.logback.classic)

    implementation("org.jetbrains.exposed:exposed-core:1.6.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.6.0")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")


    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}

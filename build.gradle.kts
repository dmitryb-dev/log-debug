plugins {
    kotlin("jvm") version "1.5.31"
    id("io.freefair.aspectj") version "6.4.1"
}

group = "com.github.dmitrybdev"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    api("com.github.dmitrybdev:log-debug-handler:1.0")
    implementation("org.aspectj:aspectjrt:1.9.9.1")
}
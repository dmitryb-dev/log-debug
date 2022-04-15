plugins {
    kotlin("jvm") version "1.5.31"
}

group = "com.github.dmitrybdev"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.aspectj:aspectjrt:1.9.9.1")
}
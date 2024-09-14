plugins {
    kotlin("jvm") version "2.0.10"
    id("me.champeau.jmh") version "0.7.2"
    kotlin("kapt") version "2.0.0"
    id("project-report")
//    id("com.google.devtools.ksp") version "2.0.0-1.0.24" // not working with ksp
}

group = "com.eventloopsoftware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

jmh {
    profilers = listOf("org.openjdk.jmh.profile.JavaFlightRecorderProfiler")
    resultFormat = "CSV"
}


dependencies {
    implementation(("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1"))
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.5.9")

    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    implementation("io.ktor:ktor-server-netty:2.3.12")

    kapt("org.openjdk.jmh:jmh-generator-annprocess:1.37")
//    ksp("org.openjdk.jmh:jmh-generator-annprocess:1.37") // not working with ksp

}

kotlin {
    jvmToolchain(21)
}




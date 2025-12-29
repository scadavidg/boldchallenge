
plugins {
    id("java-library")
    kotlin("jvm")
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    // ===== COROUTINES =====
    implementation(libs.coroutines.core)

    // ===== UNIT TESTING =====
    // JUnit 5 (Modern Testing Framework)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)
    
    // JUnit Platform Launcher (required for JUnit 5 test execution)
    testImplementation(libs.junit.platform.launcher)

    // JUnit 4 (Legacy Compatibility)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit5.vintage)

    // Testing Utilities
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}

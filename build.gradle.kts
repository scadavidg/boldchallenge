// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // ===== ANDROID PLUGINS =====
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // ===== KOTLIN PLUGINS =====
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false

    // ===== CODE PROCESSING =====
    alias(libs.plugins.ksp) apply false // Kotlin Symbol Processing

    // ===== DEPENDENCY INJECTION =====
    alias(libs.plugins.hilt.android) apply false

    // ===== CODE QUALITY =====
    alias(libs.plugins.ktlint) // Applied to root project
}
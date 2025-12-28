plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    kotlin("kapt")
}

android {
    namespace = "com.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Leer API key desde local.properties (no se sube a Git)
        // Gradle lee automáticamente desde local.properties si existe
        val weatherApiKey = project.findProperty("WEATHER_API_KEY") as String? ?: ""
        buildConfigField("String", "WEATHER_API_KEY", "\"$weatherApiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            // Enable JUnit 5 for unit tests
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}


dependencies {
// ===== PROJECT MODULES =====
    implementation(project(":domain"))

    // ===== DEPENDENCY INJECTION =====
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    // ===== NETWORKING =====
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.logging.interceptor)

    // ===== JSON SERIALIZATION =====
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    // Usando reflection en lugar de codegen para evitar problemas con KAPT

    // ===== DATABASE =====
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // ===== UNIT TESTING =====
    // JUnit 5 (Modern Testing Framework)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testRuntimeOnly(libs.junit5.engine)

    // JUnit 4 (Legacy Compatibility)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit5.vintage)

    // Testing Utilities
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.coroutines.test)

    // Network Testing
    testImplementation(libs.mockwebserver)
    testImplementation(libs.okhttp3)

    // ===== INSTRUMENTATION TESTING =====
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
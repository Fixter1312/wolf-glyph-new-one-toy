plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.wilczektoy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wilczektoy"
        minSdk = 34        // możesz zostawić 34 (Nothing Phone 3 = Android 14)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // KLUCZ: Java i Kotlin na JDK 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release { isMinifyEnabled = false }
        debug { isMinifyEnabled = false }
    }

    packaging { resources.excludes.add("META-INF/*") }
}

dependencies {
    // AAR z Nothing Glyph Matrix SDK – musi być w app/libs/
    implementation(files("libs/glyph-matrix-sdk-1.0.aar"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}

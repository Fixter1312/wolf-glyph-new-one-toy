plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.wilczektoy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.wilczektoy"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // WYMAGANE: umieść plik .aar SDK Nothing w app/libs i dopasuj nazwę poniżej
    implementation(files("libs/glyph-matrix-sdk-1.0.aar"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // <- ADD THIS LINE
}

android {
    namespace = "com.example.pixelpholio"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pixelpholio"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // REMOVE THIS ENTIRE composeOptions BLOCK
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "1.5.1"
    // }
}

dependencies {
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.activity:activity-compose:1.8.0") // You have two versions of activity-compose, keep the newer one

    // implementation("androidx.activity:activity-compose:1.7.2") // REMOVE THIS (older version)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material") // Consider using Material3 components more if you are using material3:1.2.0

    implementation("androidx.compose.ui:ui-tooling-preview") // BOM should manage version
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Optional for Canvas debugging:
    debugImplementation("androidx.compose.ui:ui-tooling") // BOM should manage version
}
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProperties.load(localPropsFile.inputStream())
}

android {
    namespace = "com.z.tdclub.tradulauncherredux"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.z.tdclub.tradulauncherredux"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-redux"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            val keyStore = localProperties.getProperty("key.store")
            if (keyStore != null) {
                storeFile = file(keyStore)
                storePassword = localProperties.getProperty("key.store.password")
                keyAlias = localProperties.getProperty("key.alias")
                keyPassword = localProperties.getProperty("key.alias.password")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            packaging {
                jniLibs {
                    useLegacyPackaging = true
                    excludes += listOf("**/x86/*.so")
                    keepDebugSymbols += listOf("**/*.so")
                }
            }
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            packaging {
                jniLibs {
                    useLegacyPackaging = true
                    excludes += listOf("**/x86/*.so")
                }
            }
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

    androidResources {
        noCompress += listOf(
            "ogv", "avi", "mpg", "webm", "mkv", "mp4",
            "mp3", "mp2", "ogg", "opus", "flac",
            "rpyc", "rpymc", "rpyb"
        )
    }
}

dependencies {
    // internal modules
    implementation(project(":core"))
    implementation(project(":runtime-renpy784"))

    // compose bom
    val composeBom = platform("androidx.compose:compose-bom:2025.04.00")
    implementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // activity compose integration
    implementation("androidx.activity:activity-compose:1.10.1")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // core
    implementation("androidx.core:core-ktx:1.15.0")
}

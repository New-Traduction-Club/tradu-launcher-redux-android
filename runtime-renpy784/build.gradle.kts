plugins {
    id("com.android.library")
}

android {
    namespace = "org.renpy.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
            isMinifyEnabled = false
            packaging {
                jniLibs {
                    useLegacyPackaging = true
                    excludes += listOf("**/x86/*.so")
                    keepDebugSymbols += listOf("**/*.so")
                }
            }
        }
    }
}

// pure java module, no kotlin or compose dependencies needed
dependencies {
}

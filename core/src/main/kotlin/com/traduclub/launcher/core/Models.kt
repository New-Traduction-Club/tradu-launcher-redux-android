package com.traduclub.launcher.core

// information about a detected renpy game on disk
data class GameInfo(
    val id: String,
    val name: String,
    val path: String,
    val detectedRenpyVersion: String? = null,
    val detectedPythonVersion: Int? = null,
    val runtimeFamilyId: String? = null,
    val coverImagePath: String? = null,
    val orientation: Int = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
)

// metadata describing an installed runtime bundle
data class RuntimeManifest(
    val familyId: String,
    val displayName: String,
    val renpyVersion: String,
    val pythonVersion: String,
    val supportedVersionRange: VersionRange,
    val nativeLibraries: List<String>,
    val runtimeScriptsUrl: String? = null,
    val runtimeScriptsInstalled: Boolean = false
)

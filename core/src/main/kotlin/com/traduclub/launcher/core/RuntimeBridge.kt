package com.traduclub.launcher.core

import android.content.Intent

// contract that each runtime module must implement to bridge the launcher with a specific
// renpy/rapt version family
interface RuntimeBridge {

    // unique identifier for this runtime family
    val familyId: String

    // human readable name shown in ui
    val displayName: String

    // renpy version range this runtime supports
    val supportedVersionRange: VersionRange

    // returns the activity class that launches the game
    fun getActivityClass(): Class<*>

    // prepares an intent with all necessary extras to launch a game
    fun prepareLaunchIntent(
        baseIntent: Intent,
        gamePath: String,
        orientation: Int
    ): Intent

    // list of native library names loaded by this runtime
    fun getRequiredLibraries(): List<String>
}

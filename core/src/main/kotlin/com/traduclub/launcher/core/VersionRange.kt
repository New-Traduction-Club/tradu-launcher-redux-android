package com.traduclub.launcher.core

// represents a range of renpy versions that a runtime can handle
data class VersionRange(
    val minMajor: Int,
    val minMinor: Int,
    val maxMajor: Int,
    val maxMinor: Int
) {
    fun contains(major: Int, minor: Int): Boolean {
        val version = major * 1000 + minor
        val min = minMajor * 1000 + minMinor
        val max = maxMajor * 1000 + maxMinor
        return version in min..max
    }

    override fun toString(): String = "$minMajor.$minMinor - $maxMajor.$maxMinor"
}

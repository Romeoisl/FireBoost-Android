package com.fireboost.launcher.mapper

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class RelativeMotionProcessor(private var profile: CameraProfile = CameraProfile()) {

    fun setProfile(newProfile: CameraProfile) {
        profile = newProfile
    }

    fun process(deltaX: Float, deltaY: Float): Pair<Float, Float> {
        if (abs(deltaX) < profile.deadzone && abs(deltaY) < profile.deadzone) {
            return 0f to 0f
        }

        var x = deltaX * profile.sensitivityX
        var y = deltaY * profile.sensitivityY

        if (profile.invertX) x = -x
        if (profile.invertY) y = -y

        val smoothing = min(1f, max(0f, profile.smoothing))
        val factor = 1f - smoothing
        return (x * factor) to (y * factor)
    }
}

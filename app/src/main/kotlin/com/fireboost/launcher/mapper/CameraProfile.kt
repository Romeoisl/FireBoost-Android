package com.fireboost.launcher.mapper

data class CameraProfile(
    val name: String = "Default",
    val sensitivityX: Float = 1.0f,
    val sensitivityY: Float = 1.0f,
    val deadzone: Float = 0.0f,
    val smoothing: Float = 0.0f,
    val invertX: Boolean = false,
    val invertY: Boolean = false
)

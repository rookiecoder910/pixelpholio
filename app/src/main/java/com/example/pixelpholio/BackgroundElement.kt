package com.example.pixelpholio

data class BackgroundElement(
    val imageRes: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val layerSpeed: Float = 1f,  // for parallax effect
    val isInteractive: Boolean = false,
    val isCollidable: Boolean = false
)

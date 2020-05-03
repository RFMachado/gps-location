package com.example.amorient.model

data class Route(
    val name: String,
    val checkpoints: List<CheckPoint>,
    var isChecked: Boolean = false
)
package ru.dikoresearch.aridewarehouse.domain.entities

data class OrderImage(
    val imageName: String,
    val imageUri: String = "",
    val loaded: Boolean = false,
    val newImageActionHolder: Boolean = false
)

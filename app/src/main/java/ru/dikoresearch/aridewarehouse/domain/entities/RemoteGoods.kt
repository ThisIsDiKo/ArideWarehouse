package ru.dikoresearch.aridewarehouse.domain.entities

import com.google.gson.annotations.SerializedName

data class RemoteGoods(
    @SerializedName("art")
    val art: String,
    val name: String,
    val count: String,
    val price: Int
)

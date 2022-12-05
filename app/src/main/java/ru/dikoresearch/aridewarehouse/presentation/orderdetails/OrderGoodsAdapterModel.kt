package ru.dikoresearch.aridewarehouse.presentation.orderdetails

import ru.dikoresearch.aridewarehouse.domain.entities.RemoteGoods


data class OrderGoodsAdapterModel(
    val goods: RemoteGoods,
    val isChecked: Boolean,
    val isLoaded: Boolean
)


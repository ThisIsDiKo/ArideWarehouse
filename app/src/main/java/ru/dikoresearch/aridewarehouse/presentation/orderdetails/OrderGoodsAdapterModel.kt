package ru.dikoresearch.aridewarehouse.presentation.orderdetails

import ru.dikoresearch.aridewarehouse.domain.entities.ArideGoods

data class OrderGoodsAdapterModel(
    val goods: ArideGoods,
    val isChecked: Boolean,
    val isLoaded: Boolean
)


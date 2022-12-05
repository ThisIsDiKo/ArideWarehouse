package ru.dikoresearch.aridewarehouse.presentation.utils

import ru.dikoresearch.aridewarehouse.BuildConfig

const val SHARED_PREFERENCES_NAME = "WarehouseData"
const val WAREHOUSE_TOKEN = "WAREHOUSE_TOKEN"
const val WAREHOUSE_USERNAME = "WAREHOUSE_USERNAME"
const val ORDER_NAME = "ORDER_NAME"
const val IMAGE_URL = "IMAGE_URL"

const val ALLOWED_NUMBER_OF_IMAGES = "ALLOWED_NUMBER_OF_IMAGES"

const val IMAGES_URLS_ARRAY = "IMAGES_URLS_ARRAY"

const val BASE_URL = BuildConfig.SERVER_URL
const val BASE_URL_IMAGES = "$BASE_URL/orders/image/"
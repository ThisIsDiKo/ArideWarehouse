package ru.dikoresearch.aridewarehouse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

//    1. @Inject
//    lateinit var warehouseRepository: WarehouseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        appComponent.inject(this)
//        trackOnStart()
    }

//    @Inject //инжект в функцию
//    fun trackOnStart(warehouseRepository: WarehouseRepository){
//        // 1. warehouseRepository.login("11","22")
//    }
}
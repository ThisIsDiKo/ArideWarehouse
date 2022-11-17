package ru.dikoresearch.aridewarehouse.domain.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository

class ImageUploadWorkerFactory(
    private val warehouseRepository: WarehouseRepository
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName){
            ImageUploadWorker::class.java.name -> {
                ImageUploadWorker(appContext, workerParameters, warehouseRepository)
            }
            else -> {
                null
            }
        }
    }
}
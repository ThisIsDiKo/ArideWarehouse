package ru.dikoresearch.aridewarehouse.domain.workers

import androidx.work.DelegatingWorkerFactory
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkersFactory @Inject constructor(
    warehouseRepository: WarehouseRepository
): DelegatingWorkerFactory() {
    init {
        addFactory(ImageUploadWorkerFactory(warehouseRepository))
    }
}
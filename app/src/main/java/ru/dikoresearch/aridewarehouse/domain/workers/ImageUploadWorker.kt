package ru.dikoresearch.aridewarehouse.domain.workers


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.work.*
import ru.dikoresearch.aridewarehouse.R
import ru.dikoresearch.aridewarehouse.domain.repository.WarehouseRepository
import ru.dikoresearch.aridewarehouse.presentation.utils.rotateImage
import java.io.InputStream

class ImageUploadWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val warehouseRepository: WarehouseRepository
): CoroutineWorker(appContext, workerParameters) {

    private val notificationId = 1

    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    private fun createNotification(): Notification{
        val channelId = "1"
        val title = "Upload to server"
        //val cancel = "Cancel"
        val name = "UploadingToServer"

        // This PendingIntent can be used to cancel the Worker.
        //val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val notificationBuilder = Notification.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_icon_airbag)
            .setOngoing(true)
            .setProgress(0, 0, true)

        createNotificationChannel(channelId, name).also { channel ->
            notificationBuilder.setChannelId(channel.id)
        }

        return notificationBuilder.build()
    }

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(
        channelId: String,
        name: String
    ): NotificationChannel {
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationId, createNotification()
        )
    }


    override suspend fun doWork(): Result {
        return try {
            val orderId = inputData.getInt(WORKER_ORDER_ID_KEY, -1)
            val images = inputData.getStringArray(WORKER_IMAGES_ARRAY_KEY) ?: throw IllegalStateException("Images Array must not be null")

            images.forEach { imageUri ->
                Log.d(TAG, "Uploading image $imageUri")

//                val path = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/"+ Uri.parse(imageUri).lastPathSegment
//                val inputStream = inputStreamFor(applicationContext, imageUri)
//                val bitmap = BitmapFactory.decodeStream(inputStream)
//
//                val exif = ExifInterface(path)

                val bitmap = BitmapFactory.decodeFile(imageUri)
                val exif = ExifInterface(imageUri)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)

                val angle = when(orientation){
                    ExifInterface.ORIENTATION_ROTATE_90 -> {
                        90
                    }
                    ExifInterface.ORIENTATION_ROTATE_180 -> {
                        180
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> {
                        270
                    }
                    else -> {
                        0
                    }
                }

                val rotatedBitmap = rotateImage(bitmap, angle.toFloat())
                warehouseRepository.uploadImage(
                    orderId = orderId,
                    imageName = imageUri.split("/").last(),
                    image = rotatedBitmap
                )
            }

            val outputData = workDataOf(WORKER_OUTPUT_DATA_KEY to WORKER_OUTPUT_DATA_SUCCESS)
            Result.success(outputData)
        }
        catch (e: Exception){
            val outputData = workDataOf(WORKER_OUTPUT_DATA_KEY to e.toString())
            Result.failure(outputData)
        }
    }

    companion object {
        const val TAG = "Image Upload Worker"

        const val WORKER_ORDER_ID_KEY = "WORKER_ORDER_ID_KEY"
        const val WORKER_IMAGE_NAME_KEY = "WORKER_IMAGE_NAME_KEY"
        const val WORKER_IMAGES_ARRAY_KEY = "WORKER_IMAGES_ARRAY_KEY"
        const val WORKER_OUTPUT_DATA_KEY = "WORKER_OUTPUT_DATA_KEY"

        const val WORKER_OUTPUT_DATA_SUCCESS = "SUCCESS"

        private const val ASSET_PREFIX = "file:///android_asset/"
        private fun inputStreamFor(
            context: Context,
            resourceUri: String
        ): InputStream? {

            // If the resourceUri is an Android asset URI, then use AssetManager to get a handle to
            // the input stream. (Stock Images are Asset URIs).
            return if (resourceUri.startsWith(ASSET_PREFIX)) {
                val assetManager = context.resources.assets
                assetManager.open(resourceUri.substring(ASSET_PREFIX.length))
            } else {
                // Not an Android asset Uri. Use a ContentResolver to get a handle to the input stream.
                val resolver = context.contentResolver
                resolver.openInputStream(Uri.parse(resourceUri))
            }
        }
    }
}
package ru.dikoresearch.aridewarehouse.domain.camera

import android.app.Activity
import android.net.Uri
import android.util.DisplayMetrics
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

class CameraXCaptureHelper(
    private val caller: Any,
    private val previewView: PreviewView,
    private val filesDirectory: File,
    private val orderName: String,
    private val onPictureTaken: ((File, Uri?) -> Unit)? = null,
    private val builderPreview: Preview.Builder? = null,
    private val builderImageCapture: ImageCapture.Builder? = null,
    private val onError: ((Throwable) -> Unit)? = null,
) {

    private val context by lazy {
        when (caller) {
            is Activity -> caller
            is Fragment -> caller.activity ?: throw Exception("Fragment not attached to activity")
            else -> throw Exception("Can't get a context from caller")
        }
    }

    private lateinit var imagePreview: Preview
    private lateinit var imageCapture: ImageCapture

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val executor = Executors.newSingleThreadExecutor()

    fun start() {
        if (caller !is LifecycleOwner) throw Exception("Caller is not lifecycle owner")
        previewView.post {
            startCamera()
        }
    }

    fun stop(){
        ProcessCameraProvider.getInstance(context).get().unbindAll()
    }

    private fun createImagePreview() =
        (builderPreview ?: Preview.Builder()
            .setTargetAspectRatio(aspectRatio()))
            .setTargetRotation(previewView.display.rotation)
            .build()
            .apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

    private fun createImageCapture() =
        (builderImageCapture ?: ImageCapture.Builder()
            .setTargetAspectRatio(aspectRatio()))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

    private fun startCamera(){
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val cameraProvideFuture = ProcessCameraProvider.getInstance(context)
        cameraProvideFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProvideFuture.get()
                imagePreview = createImagePreview()
                imagePreview.setSurfaceProvider(previewView.surfaceProvider)

                imageCapture = createImageCapture()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    caller as LifecycleOwner,
                    cameraSelector,
                    imagePreview,
                    imageCapture
                )
            }
            catch (e: Exception){
                onError?.invoke(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun aspectRatio(): Int {
        @Suppress("DEPRECATION")
        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun takePicture(){
        val dir = filesDirectory
        if (!dir.exists()) dir.mkdirs()

        val fileName = "IMG_" + orderName + "_${System.currentTimeMillis()}"+".jpeg"

        val file = File(dir, fileName)

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file)
            .setMetadata(metadata)
            .build()

        imageCapture.takePicture(
            outputFileOptions,
            executor,
            object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onPictureTaken?.invoke(
                        file,
                        outputFileResults.savedUri
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    onError?.invoke(exception)
                }
            }
        )
    }
}
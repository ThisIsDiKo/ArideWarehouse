package ru.dikoresearch.aridewarehouse.domain.camera

import android.app.Activity
import android.util.DisplayMetrics
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import kotlin.math.abs

class CameraXHelper(
    private val caller: Any,
    private val previewView: PreviewView,
    private val builderPreview: Preview.Builder? = null,
    private val imageAnalyzer: ImageAnalysis.Analyzer? = null,
    private val onError: ((Throwable) -> Unit)? = null,
) {

    private val context by lazy {
        when(caller){
            is Activity -> caller
            is Fragment -> caller.activity ?: throw IllegalStateException("Fragment not attached to activity")
            else -> throw IllegalStateException("Can't get context from caller")
        }
    }

    private lateinit var imagePreview: Preview
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null

    private val lensFacing = CameraSelector.LENS_FACING_BACK
    private val executor = Executors.newSingleThreadExecutor()

    fun start(){
        if (caller !is LifecycleOwner) throw IllegalStateException("caller is not lifecycle owner")
        previewView.post {
            startCamera()
        }
    }


    private fun createImagePreview() =
        (builderPreview ?: Preview.Builder().setTargetAspectRatio(aspectRatio()))
            .setTargetRotation(previewView.display.rotation)
            .build()
            .apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

    private fun createImageAnalysis(): ImageAnalysis?{
        if (imageAnalyzer == null) return null

        val analysisUseCase = ImageAnalysis.Builder()
            .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(aspectRatio())
            .build()

        analysisUseCase.setAnalyzer(
            executor,
            imageAnalyzer
        )

        return analysisUseCase
    }

    private fun createImageCapture(): ImageCapture?{
         return null
    }

    private fun startCamera(){
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val cameraProvideFeature = ProcessCameraProvider.getInstance(context)

        cameraProvideFeature.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProvideFeature.get()
                imagePreview = createImagePreview()
                //imagePreview.setSurfaceProvider(previewView.surfaceProvider)

                imageCapture = createImageCapture()
                imageAnalysis = createImageAnalysis()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    caller as LifecycleOwner,
                    cameraSelector,
                    imagePreview,
                    //imageCapture,
                    imageAnalysis
                )

            }
            catch (e: Exception){
                onError?.invoke(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @Suppress("DEPRECATION")
    private fun aspectRatio(): Int {
        val metrics = DisplayMetrics().also {
            previewView.display.getRealMetrics(it)
        }
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)){
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
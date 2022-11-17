package ru.dikoresearch.aridewarehouse.presentation.scanner

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import ru.dikoresearch.aridewarehouse.presentation.utils.rotate
import ru.dikoresearch.aridewarehouse.presentation.utils.toBitmap

class QrCodeAnalyzer(
    private val qrCodeDetectionListener: (barcode: String) -> Unit
): ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null){
            val height = mediaImage.height
            val width = mediaImage.width

            //Top    : (far) -value > 0 > +value (closer)
            val c1x = (width * 0.125).toInt() + 150
            //Right  : (far) -value > 0 > +value (closer)
            val c1y = (height * 0.25).toInt() - 25
            //Bottom : (closer) -value > 0 > +value (far)
            val c2x = (width * 0.875).toInt() - 150
            //Left   : (closer) -value > 0 > +value (far)
            val c2y = (height * 0.75).toInt() + 25

            val rect = Rect(c1x, c1y, c2x, c2y)

            val ori: Bitmap = imageProxy.toBitmap()!!
            val crop = Bitmap.createBitmap(ori, rect.left, rect.top, rect.width(), rect.height())
            val rImage = crop.rotate(90F)

            val image: InputImage =
                InputImage.fromBitmap(rImage, imageProxy.imageInfo.rotationDegrees)

            // Pass image to the scanner and have it do its thing
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Task completed successfully
                    for (barcode in barcodes) {
                        qrCodeDetectionListener(barcode.rawValue ?: "")
                        imageProxy.close()
                    }
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
                .addOnCompleteListener {
                    // It's important to close the imageProxy
                    imageProxy.close()
                }
        }
    }
}
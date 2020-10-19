/**
 * +-+-+-+-+-+-+
 * |y|o|o|n|i|t|
 * +-+-+-+-+-+-+
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Yoonit Camera lib for Android applications                      |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2020             |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

package ai.cyberlabs.yoonit.camera

import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Custom camera image analyzer based on barcode detection bounded on [CameraController].
 */
class BarcodeAnalyzer(
    private val cameraEventListener: CameraEventListener?,
    private val graphicView: CameraGraphicView
) : ImageAnalysis.Analyzer {

    /**
     * Analyzes camera image...
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        this.graphicView.clear()

        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val barCodeOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(barCodeOptions)

        scanner
            .process(image)
            .addOnSuccessListener { barcodes ->

                if (barcodes.isEmpty() || this.cameraEventListener == null) return@addOnSuccessListener

                // Get closest bounding box.
                var boundingBox = Rect()
                var rawValue = String()
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    bounds?.let {
                        if (it.width() > boundingBox.width()) {
                            boundingBox = it
                            rawValue = barcode.rawValue!!
                        }
                    }
                }

                this.cameraEventListener.onBarcodeScanned(rawValue)
            }
            .addOnFailureListener { e ->
                if (this.cameraEventListener != null) {
                    this.cameraEventListener.onError(e.toString())
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
                scanner.close()
            }
    }
}

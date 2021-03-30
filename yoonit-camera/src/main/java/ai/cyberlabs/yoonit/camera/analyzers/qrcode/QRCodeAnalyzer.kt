/**
 * +-+-+-+-+-+-+
 * |y|o|o|n|i|t|
 * +-+-+-+-+-+-+
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Yoonit Camera lib for Android applications                      |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2020-2021        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */

package ai.cyberlabs.yoonit.camera.analyzers.qrcode

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.analyzers.CoordinatesController
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Custom camera image analyzer based on barcode detection bounded on [CameraController].
 */
class QRCodeAnalyzer(
    private val cameraEventListener: CameraEventListener?,
    private val graphicView: CameraGraphicView
) : ImageAnalysis.Analyzer {
    private var isValid: Boolean = true
    private val coordinatesController = CoordinatesController(this.graphicView)
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions
            .Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    /**
     * Receive image from CameraX API.
     *
     * @param imageProxy image from CameraX API.
     */
    override fun analyze(imageProxy: ImageProxy) {
        this.detect(
            imageProxy,
            { detectionBox, value ->
                this.cameraEventListener?.onQRCodeScanned(value)
                this.graphicView.handleDraw(detectionBox)
            },
            { message ->
                this.cameraEventListener?.onMessage(message)
            },
            { error ->
                this.cameraEventListener?.onError(error)
            },
            {
                imageProxy.close()
            }
        )
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun detect(
        imageProxy: ImageProxy,
        onSuccess: (RectF, String) -> Unit,
        onMessage: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        if (imageProxy.image == null) {
            onComplete()
            return
        }

        val image: InputImage = InputImage.fromMediaImage(
            imageProxy.image,
            imageProxy.imageInfo.rotationDegrees
        )

        this.scanner
            .process(image)
            .addOnSuccessListener { barcodes ->

                if (barcodes.isEmpty()) {
                    onComplete()
                    return@addOnSuccessListener
                }

                // Get closest detection box.
                var boundingBox = Rect()
                var value = ""
                for (barcode in barcodes) {
                    val bounds = barcode.boundingBox
                    bounds?.let {
                        if (it.width() > boundingBox.width()) {
                            boundingBox = it
                            value = barcode.rawValue!!
                        }
                    }
                }

                // Get from barcode the UI detection box.
                val detectionBox: RectF = this.coordinatesController.getDetectionBox(
                    boundingBox,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat(),
                    imageProxy.imageInfo.rotationDegrees.toFloat()
                )

                // Get error if exist in the detectionBox.
                val error: String? = this.coordinatesController.getError(
                    detectionBox
                )


                // Handle error.
                error?.let {
                    if (this.isValid) {
                        this.isValid = false
                        this.graphicView.clear()
                        if (error != "") {
                            onMessage(error)
                        }
                    }
                    onComplete()
                    return@addOnSuccessListener
                }
                this.isValid = true

                onSuccess(detectionBox, value)
                onComplete()
            }
            .addOnFailureListener { e ->
                onError(e.toString())
                onComplete()
            }
            .addOnCompleteListener {
                onComplete()
            }
    }
}

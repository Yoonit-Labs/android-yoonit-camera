/**
 * ██╗   ██╗ ██████╗  ██████╗ ███╗   ██╗██╗████████╗
 * ╚██╗ ██╔╝██╔═══██╗██╔═══██╗████╗  ██║██║╚══██╔══╝
 *  ╚████╔╝ ██║   ██║██║   ██║██╔██╗ ██║██║   ██║
 *   ╚██╔╝  ██║   ██║██║   ██║██║╚██╗██║██║   ██║
 *    ██║   ╚██████╔╝╚██████╔╝██║ ╚████║██║   ██║
 *    ╚═╝    ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═╝   ╚═╝
 *
 * https://yoonit.dev - about@yoonit.dev
 *
 * Yoonit Camera
 * The most advanced and modern Camera module for Android with a lot of awesome features
 *
 * Haroldo Teruya, Victor Goulart, Thúlio Noslen & Luigui Delyer @ 2020-2021
 */

package ai.cyberlabs.yoonit.camera.analyzers.qrcode

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.analyzers.CoordinatesController
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.utils.mirror
import ai.cyberlabs.yoonit.camera.utils.rotate
import ai.cyberlabs.yoonit.camera.utils.toRGBBitmap
import android.annotation.SuppressLint
import android.content.Context
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
    private val context: Context,
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
        onDetected: (RectF, String) -> Unit,
        onMessage: (String) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        imageProxy.image?.let { image ->
            val imageBitmap = image
                .toRGBBitmap(this.context)
                .rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                .mirror()

            val image: InputImage = InputImage.fromBitmap(
                imageBitmap,
                0
            )

            this.scanner
                .process(image)
                .addOnSuccessListener { barcodes ->

                    if (barcodes.isEmpty()) {
                        this.graphicView.clear()
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
                        imageProxy.height.toFloat()
                    )

                    // Get error if exist in the detectionBox.
                    val error: String? = this.coordinatesController.getError(
                        detectionBox
                    )

                    // Handle error.
                    error?.let {
                        if (this.isValid) {
                            this.isValid = false
                            if (error != "") {
                                onMessage(error)
                            }
                        }
                        this.graphicView.clear()
                        onComplete()
                        return@addOnSuccessListener
                    }
                    this.isValid = true

                    onDetected(detectionBox, value)
                    onComplete()
                }
                .addOnFailureListener { e ->
                    this.graphicView.clear()
                    onError(e.toString())
                    onComplete()
                }
                .addOnCompleteListener {
                    onComplete()
                }

            return
        }

        this.graphicView.clear()
        onComplete()
        return
    }
}

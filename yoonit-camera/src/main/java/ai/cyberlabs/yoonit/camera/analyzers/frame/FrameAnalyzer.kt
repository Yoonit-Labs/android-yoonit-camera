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

package ai.cyberlabs.yoonit.camera.analyzers.frame

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.utils.toBitmap
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream

/**
 * Custom camera image analyzer based on frame bounded on [CameraController].
 */
class FrameAnalyzer(
    private val context: Context,
    private val cameraEventListener: CameraEventListener?,
    private val captureOptions: CaptureOptions,
    private val graphicView: CameraGraphicView,
    private val cameraCallback: CameraCallback
) : ImageAnalysis.Analyzer {

    private var analyzerTimeStamp: Long = 0
    private var numberOfImages = 0

    /**
     * Receive image from CameraX API.
     *
     * @param imageProxy image from CameraX API.
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        this.graphicView.clear()

        if (this.shouldAnalyze(imageProxy)) {
            val mediaImage = imageProxy.image

            val imagePath = this.saveImage(
                mediaImage!!.toBitmap(),
                imageProxy.imageInfo.rotationDegrees.toFloat()
            )

            Handler(Looper.getMainLooper()).post {
                this.handleEmitFrameImageCreated(imagePath)
            }
        }

        imageProxy.close()
    }

    /**
     * Check if image is to analyze.
     *
     * @param imageProxy image from CameraX API.
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    private fun shouldAnalyze(imageProxy: ImageProxy): Boolean {

        if (!this.captureOptions.saveImageCaptured) {
            return false
        }

        if (this.cameraEventListener == null) {
            return false
        }

        if (imageProxy.image == null) {
            return false
        }

        // Process image only within interval equal ANALYZE_TIMER.
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - this.analyzerTimeStamp < this.captureOptions.timeBetweenImages) {
            return false
        }
        this.analyzerTimeStamp = currentTimestamp

        return true
    }

    /**
     * Handle emit frame image file created.
     *
     * @param imagePath image file path.
     */
    private fun handleEmitFrameImageCreated(imagePath: String) {

        // process face number of images.
        if (this.captureOptions.numberOfImages > 0) {
            if (this.numberOfImages < captureOptions.numberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onImageCaptured(
                    "frame",
                    this.numberOfImages,
                    this.captureOptions.numberOfImages,
                    imagePath
                )
                return
            }

            this.cameraCallback.onStopAnalyzer()
            this.cameraEventListener?.onEndCapture()
            return
        }

        // process face unlimited.
        this.numberOfImages = (this.numberOfImages + 1) % NUMBER_OF_IMAGES_LIMIT
        this.cameraEventListener?.onImageCaptured(
            "frame",
            this.numberOfImages,
            this.captureOptions.numberOfImages,
            imagePath
        )
    }

    /**
     *
     *
     * @param mediaBitmap the original image bitmap.
     * @param rotationDegrees the rotation degrees to turn the image to portrait.
     * @return the image file path created.
     */
    private fun saveImage(mediaBitmap: Bitmap, rotationDegrees: Float): String {
        val path = this.context.externalCacheDir.toString()
        val file = File(path, "yoonit-frame-".plus(this.numberOfImages).plus(".jpg"))
        val fileOutputStream = FileOutputStream(file)

        var matrix = Matrix()
        matrix.postRotate(rotationDegrees)

        if (rotationDegrees == 270.0f) {
            matrix.preScale(1.0f, -1.0f)
        }

        val rotateBitmap =
            Bitmap.createBitmap(
                mediaBitmap,
                0,
                0,
                mediaBitmap.width,
                mediaBitmap.height,
                matrix,
                false
            )

        rotateBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

        fileOutputStream.close()

        return file.absolutePath
    }

    companion object {
        private const val TAG = "FrameAnalyzer"
        private const val NUMBER_OF_IMAGES_LIMIT = 25
    }
}

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

package ai.cyberlabs.yoonit.camera.analyzers.frame

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.controllers.ComputerVisionController
import ai.cyberlabs.yoonit.camera.controllers.ImageQualityController
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.mirror
import ai.cyberlabs.yoonit.camera.utils.rotate
import ai.cyberlabs.yoonit.camera.utils.toRGBBitmap
import ai.cyberlabs.yoonit.camera.utils.toYUVBitmap
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraSelector
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

            mediaImage?.let {

                var frameBitmap: Bitmap = when (CaptureOptions.colorEncoding) {
                    "YUV" -> mediaImage.toYUVBitmap()
                    else -> mediaImage.toRGBBitmap(context)
                }

                if (CaptureOptions.cameraLens == CameraSelector.LENS_FACING_BACK) {
                    frameBitmap = frameBitmap
                        .rotate(180f)
                        .mirror()
                }

                // Computer Vision Inference.
                var inferences: ArrayList<android.util.Pair<String, FloatArray>> = arrayListOf()
                if (CaptureOptions.ComputerVision.enable) {
                    inferences = ComputerVisionController.getInferences(
                        CaptureOptions.ComputerVision.modelMap,
                        frameBitmap
                    )
                }

                // Save image captured.
                var imagePath = ""
                if (CaptureOptions.saveImageCaptured) {
                    imagePath = this.handleSaveImage(
                        frameBitmap,
                        imageProxy.imageInfo.rotationDegrees.toFloat()
                    )
                }

                val imageQuality: Triple<Double, Double, Double> =
                        ImageQualityController.processImage(frameBitmap, false)

                // Handle to emit image path and the inference.
                Handler(Looper.getMainLooper()).post {
                    this.handleEmitImageCaptured(imagePath, inferences, imageQuality)
                }
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

        if (
            !CaptureOptions.saveImageCaptured &&
            !CaptureOptions.ComputerVision.enable
        ) {
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
        if (currentTimestamp - this.analyzerTimeStamp < CaptureOptions.timeBetweenImages) {
            return false
        }
        this.analyzerTimeStamp = currentTimestamp

        return true
    }

    /**
     * Handle emit frame image file created.
     *
     * @param imagePath image file path.
     * @param inferences The computer vision inferences based in the models.
     */
    private fun handleEmitImageCaptured(
        imagePath: String,
        inferences: ArrayList<android.util.Pair<String, FloatArray>>,
        imageQuality: Triple<Double, Double, Double>
    ) {

        // process face number of images.
        if (CaptureOptions.numberOfImages > 0) {
            if (this.numberOfImages < CaptureOptions.numberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onImageCaptured(
                    "frame",
                    this.numberOfImages,
                    CaptureOptions.numberOfImages,
                    imagePath,
                    inferences,
                    imageQuality.first,
                    imageQuality.second,
                    imageQuality.third
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
            CaptureOptions.numberOfImages,
            imagePath,
            inferences,
            imageQuality.first,
            imageQuality.second,
            imageQuality.third
        )
    }

    /**
     * Handle save file image.
     *
     * @param mediaBitmap the original image bitmap.
     * @param rotationDegrees the rotation degrees to turn the image to portrait.
     * @return the image file path created.
     */
    private fun handleSaveImage(
        mediaBitmap: Bitmap,
        rotationDegrees: Float
    ): String {

        val path = this.context.externalCacheDir.toString()
        val file = File(path, "yoonit-frame-".plus(this.numberOfImages).plus(".jpg"))
        val fileOutputStream = FileOutputStream(file)

        mediaBitmap
            .rotate(rotationDegrees)
            .mirror()
            .compress(
                Bitmap.CompressFormat.JPEG,
                100,
                fileOutputStream
            )

        fileOutputStream.close()

        return file.absolutePath
    }

    companion object {
        private const val TAG = "FrameAnalyzer"
        private const val NUMBER_OF_IMAGES_LIMIT = 25
    }
}

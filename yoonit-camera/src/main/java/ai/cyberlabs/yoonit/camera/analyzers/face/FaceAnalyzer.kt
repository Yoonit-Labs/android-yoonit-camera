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

package ai.cyberlabs.yoonit.camera.analyzers.face

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.analyzers.CoordinatesController
import ai.cyberlabs.yoonit.camera.controllers.ComputerVisionController
import ai.cyberlabs.yoonit.camera.controllers.ImageQualityController
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.*
import ai.cyberlabs.yoonit.facefy.Facefy
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream

/**
 * Custom camera image analyzer based on face detection bounded on [CameraController].
 */
class FaceAnalyzer(
    private val context: Context,
    private val cameraEventListener: CameraEventListener?,
    private val graphicView: CameraGraphicView,
    private val cameraCallback: CameraCallback
) : ImageAnalysis.Analyzer {
    private var facefy: Facefy = Facefy()
    private var analyzerTimeStamp: Long = 0
    private var isValid: Boolean = true
    private var numberOfImages = 0
    private val coordinatesController = CoordinatesController(this.graphicView)

    /**
     * Receive image from CameraX API.
     *
     * @param imageProxy image from CameraX API.
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return

        val bitmap = mediaImage
            .toRGBBitmap(context)
            .rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            .mirror()

        this.facefy.detect(
            bitmap,
            { faceDetected ->

                // Get from faceDetected the graphic face bounding box.
                val detectionBox = if (faceDetected != null) {
                    this.coordinatesController.getDetectionBox(
                        faceDetected.boundingBox,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                } else RectF()

                // Verify if has error on detection box.
                if (this.hasError(detectionBox)) {
                    return@detect
                }

                faceDetected?.let { faceDetected ->

                    // Transform the camera face contour points to UI graphic coordinates.
                    // Used to draw the face contours.
                    val faceContours = this.coordinatesController.getFaceContours(
                        faceDetected.contours,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )

                    // Get face bitmap.
                    val faceBitmap: Bitmap = this.getFaceBitmap(
                        mediaImage,
                        faceDetected.boundingBox,
                        imageProxy.imageInfo.rotationDegrees.toFloat()
                    )

                    // Draw or clean the face detection box, face blur and face contours.
                    this.graphicView.handleDraw(
                        detectionBox,
                        faceBitmap,
                        faceContours
                    )

                    // Stop here if camera event listener is not set.
                    if (this.cameraEventListener == null) {
                        return@detect
                    }

                    // Emit face analysis.
                    this.cameraEventListener.onFaceDetected(
                        detectionBox.left.pxToDPI(this.context),
                        detectionBox.top.pxToDPI(this.context),
                        detectionBox.width().pxToDPI(this.context),
                        detectionBox.height().pxToDPI(this.context),
                        faceDetected.leftEyeOpenProbability,
                        faceDetected.rightEyeOpenProbability,
                        faceDetected.smilingProbability,
                        faceDetected.headEulerAngleX,
                        faceDetected.headEulerAngleY,
                        faceDetected.headEulerAngleZ
                    )

                    // Continue only if current time stamp is within the interval.
                    val currentTimestamp = System.currentTimeMillis()
                    if (currentTimestamp - this.analyzerTimeStamp < CaptureOptions.timeBetweenImages) {
                        return@detect
                    }
                    this.analyzerTimeStamp = currentTimestamp

                    // Computer Vision Inference.
                    val inferences: ArrayList<android.util.Pair<String, FloatArray>> =
                        if (CaptureOptions.ComputerVision.enable)
                            ComputerVisionController.getInferences(
                                CaptureOptions.ComputerVision.modelMap,
                                faceBitmap
                            )
                        else arrayListOf()

                    // Save image captured.
                    val imagePath =
                        if (CaptureOptions.saveImageCaptured) this.handleSaveImage(faceBitmap)
                        else ""

                    val imageQuality: Triple<Double, Double, Double> =
                            ImageQualityController.processImage(faceBitmap, true)

                    // Handle to emit image path and the inferences.
                    this.handleEmitImageCaptured(imagePath, inferences, imageQuality)
                }
            },
            { errorMessage ->
                this.cameraEventListener?.let { this.cameraEventListener.onError(errorMessage) }
            },
            { imageProxy.close() }
        )
    }

    private fun hasError(detectionBox: RectF): Boolean {

        // Get error if exist in the detectionBox.
        val error = this.coordinatesController.getError(
            detectionBox
        )

        // Emit once if exist error in the closestFace or detectionBox.
        return this.handleError(error)
    }

    private fun handleError(error: String?): Boolean {
        error?.let {
            if (this.isValid) {
                this.isValid = false
                this.graphicView.clear()
                this.cameraEventListener?.let {
                    if (error != "") {
                        this.cameraEventListener.onMessage(error)
                    }
                    this.cameraEventListener.onFaceUndetected()
                }
            }
            return true
        }

        this.isValid = true

        return false
    }

    /**
     * Get face bitmap:
     *
     * 1. Color encoding if necessary;
     * 2. Rotate image if necessary;
     * 3. Mirror image if necessary;
     * 4. Crop image if necessary;
     * 5. Scale image if necessary;
     *
     * @param mediaImage The camera frame image;
     * @param boundingBox The face detected bounding box;
     * @param cameraRotation The camera rotation;
     *
     * @return the face bitmap.
     */
    private fun getFaceBitmap(
        mediaImage: Image,
        boundingBox: Rect,
        cameraRotation: Float
    ): Bitmap {

        val colorEncodedBitmap: Bitmap = when (CaptureOptions.colorEncoding) {
            "YUV" -> mediaImage.toYUVBitmap()
            else -> mediaImage.toRGBBitmap(context)
        }

        var faceBitmap: Bitmap = colorEncodedBitmap
            .rotate(cameraRotation)
            .mirror()

        faceBitmap = faceBitmap.crop(boundingBox.scale(
            CaptureOptions.detectionTopSize,
            CaptureOptions.detectionRightSize,
            CaptureOptions.detectionBottomSize,
            CaptureOptions.detectionLeftSize
        ))

        if (CaptureOptions.cameraLens == CameraSelector.LENS_FACING_BACK) {
            faceBitmap = faceBitmap.mirror()
        }

        return Bitmap.createScaledBitmap(
            faceBitmap,
            CaptureOptions.imageOutputWidth,
            CaptureOptions.imageOutputHeight,
            false
        )
    }

    /**
     * Handle emit face image file created.
     *
     * @param imagePath The image file path.
     * @param inferences The computer vision inferences based in the models.
     */
    private fun handleEmitImageCaptured(
        imagePath: String,
        inferences: ArrayList<android.util.Pair<String, FloatArray>>,
        imageQuality: Triple<Double, Double, Double>
    ) {
        if (imagePath == "") return

        // process face number of images.
        if (CaptureOptions.numberOfImages > 0) {
            if (this.numberOfImages < CaptureOptions.numberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onImageCaptured(
                    "face",
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
            "face",
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
     * @param faceBitmap the face bitmap.
     *
     * @return the image file path created.
     */
    private fun handleSaveImage(faceBitmap: Bitmap): String {
        val path = this.context.externalCacheDir.toString()
        val file = File(path, "yoonit-face-".plus(this.numberOfImages).plus(".jpg"))
        val fileOutputStream = FileOutputStream(file)

        faceBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            fileOutputStream
        )

        fileOutputStream.close()

        return file.absolutePath
    }

    companion object {
        private const val TAG = "FaceAnalyzer"
        private const val NUMBER_OF_IMAGES_LIMIT = 25
    }
}

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

package ai.cyberlabs.yoonit.camera.analyzers.face

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.vision.face.FaceDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream

/**
 * Custom camera image analyzer based on face detection bounded on [CameraController].
 */
class FaceAnalyzer(
    private val context: Context,
    private val cameraEventListener: CameraEventListener?,
    private val graphicView: CameraGraphicView,
    private val captureOptions: CaptureOptions,
    private val cameraCallback: CameraCallback
) : ImageAnalysis.Analyzer {

    private var analyzerTimeStamp: Long = 0
    private var isValid: Boolean = true
    private var numberOfImages = 0

    private val faceBoundingBoxController = FaceBoundingBoxController(
        this.graphicView,
        this.captureOptions
    )

    /**
     * Receive image from CameraX API.
     *
     * @param imageProxy image from CameraX API.
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return

        val cameraRotation: Int =
            if (this.captureOptions.isScreenFlipped) 270
            else imageProxy.imageInfo.rotationDegrees

        val image = InputImage.fromMediaImage(
            mediaImage,
            cameraRotation
        )

        val faceDetectorOptions = FaceDetectorOptions
            .Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetector.NO_LANDMARKS)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(faceDetectorOptions)

        detector
            .process(image)
            .addOnSuccessListener { faces ->

                // Get closest face.
                // Can be null if no face found.
                val closestFace: Face? = this.faceBoundingBoxController.getClosestFace(faces)

                // Transform the camera face coordinates to UI graphic coordinates.
                val detectionBox = this.faceBoundingBoxController.getDetectionBox(
                    closestFace,
                    image
                )

                // Get status if exist.
                val error = this
                    .faceBoundingBoxController
                    .getError(closestFace, detectionBox)

                // Emit once if has error.
                if (error != null) {
                    if (this.isValid) {
                        this.isValid = false
                        this.graphicView.clear()
                        if (this.cameraEventListener != null) {
                            if (error != "") {
                                this.cameraEventListener.onMessage(error)
                            }
                            this.cameraEventListener.onFaceUndetected()
                        }
                    }
                    return@addOnSuccessListener
                }
                this.isValid = true

                //Transform media image in bitmap and crop bitmap in the face
                var faceBoundingBox = Rect(0,0,0,0)
                closestFace?.let {
                    faceBoundingBox = it.boundingBox
                }
                val faceBitmap = cropFaceBitmap(mediaImage.toRGBBitmap(context), cameraRotation.toFloat(), faceBoundingBox)

                // Draw or clean the bounding box based on the "faceDetectionBox".
                if (this.captureOptions.faceDetectionBox) {
                    detectionBox?.let {
                        if (this.captureOptions.blurFaceDetectionBox) {
                            this.graphicView.drawFaceDetectionBox(it, faceBitmap)
                        } else this.graphicView.drawBoundingBox(it)
                    }
                } else {
                    this.graphicView.clear()
                }

                // Emit face detected.
                if (this.cameraEventListener != null && detectionBox != null) {
                    this.cameraEventListener.onFaceDetected(
                        detectionBox.left.pxToDPI(this.context),
                        detectionBox.top.pxToDPI(this.context),
                        detectionBox.width().pxToDPI(this.context),
                        detectionBox.height().pxToDPI(this.context)
                    )
                }

                if (!this.captureOptions.saveImageCaptured) {
                    return@addOnSuccessListener
                }

                // Process image only within interval equal ANALYZE_TIMER.
                val currentTimestamp = System.currentTimeMillis()
                if (currentTimestamp - this.analyzerTimeStamp < this.captureOptions.timeBetweenImages) {
                    return@addOnSuccessListener
                }
                this.analyzerTimeStamp = currentTimestamp

                val imagePath = this.saveImage(
                    faceBitmap
                )

                if (this.cameraEventListener != null) {
                    this.handleEmitFaceImageCreated(imagePath)
                }
            }
            .addOnFailureListener { e ->
                if (this.cameraEventListener != null) {
                    this.cameraEventListener.onError(e.toString())
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
                detector.close()
            }
    }

    /**
     * Handle emit face image file created.
     *
     * @param imagePath image file path.
     */
    private fun handleEmitFaceImageCreated(imagePath: String) {

        // process face number of images.
        if (this.captureOptions.numberOfImages > 0) {
            if (this.numberOfImages < captureOptions.numberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onImageCaptured(
                    "face",
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
            "face",
            this.numberOfImages,
            this.captureOptions.numberOfImages,
            imagePath
        )
    }

    /**
     * Crop an image bitmap with a face based on the detected face rect coordinates.
     *
     * @param croppedBitmap the cropped face bitmap.
     *
     * @return the image file path created.
     */
    private fun saveImage(croppedBitmap: Bitmap): String {
        val path = this.context.externalCacheDir.toString()
        val file = File(path, "yoonit-face-".plus(this.numberOfImages).plus(".jpg"))
        val fileOutputStream = FileOutputStream(file)

        val  scaledBitmap = Bitmap.createScaledBitmap(
            croppedBitmap,
            this.captureOptions.imageOutputWidth,
            this.captureOptions.imageOutputHeight,
            false
        )

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

        fileOutputStream.close()

        return file.absolutePath
    }

    private fun cropFaceBitmap(imageBitmap: Bitmap, rotationDegrees: Float, faceRect: Rect): Bitmap {

        val rotateBitmap = imageBitmap.rotate(rotationDegrees)

        val mirroredBitmap = rotateBitmap.mirror(rotationDegrees)

        return mirroredBitmap.crop(faceRect)
    }

    companion object {
        private const val TAG = "FaceAnalyzer"
        private const val NUMBER_OF_IMAGES_LIMIT = 25
    }
}

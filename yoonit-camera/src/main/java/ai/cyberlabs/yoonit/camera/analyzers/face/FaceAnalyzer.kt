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
import ai.cyberlabs.yoonit.camera.Message
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.pxToDPI
import ai.cyberlabs.yoonit.camera.utils.toBitmap
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
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
    private var isValid: Boolean = false
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

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
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

                // Draw or clean the bounding box based on the "faceDetectionBox".
                if (this.captureOptions.faceDetectionBox) {
                    this.graphicView.drawBoundingBox(detectionBox!!)
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

                if (!this.captureOptions.faceSaveImages) {
                    return@addOnSuccessListener
                }

                // Process image only within interval equal ANALYZE_TIMER.
                val currentTimestamp = System.currentTimeMillis()
                if (currentTimestamp - this.analyzerTimeStamp < this.captureOptions.faceTimeBetweenImages) {
                    return@addOnSuccessListener
                }
                this.analyzerTimeStamp = currentTimestamp

                val imagePath = this.saveImage(
                    mediaImage.toBitmap(),
                    closestFace!!.boundingBox,
                    imageProxy.imageInfo.rotationDegrees.toFloat()
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
        if (this.captureOptions.faceNumberOfImages > 0) {
            if (this.numberOfImages < captureOptions.faceNumberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onFaceImageCreated(
                    this.numberOfImages,
                    this.captureOptions.faceNumberOfImages,
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
        this.cameraEventListener?.onFaceImageCreated(
            this.numberOfImages,
            this.captureOptions.faceNumberOfImages,
            imagePath
        )
    }

    /**
     * Crop an image bitmap with a face based on the detected face rect coordinates.
     *
     * @param mediaBitmap the original image bitmap.
     * @param boundingBox the face coordinates detected.
     * @param rotationDegrees the rotation degrees to turn the image to portrait.
     *
     * @return the image file path created.
     */
    private fun saveImage(mediaBitmap: Bitmap, boundingBox: Rect, rotationDegrees: Float): String {
        val path = this.context.externalCacheDir.toString()
        val file = File(path, "yoonit-face-".plus(this.numberOfImages).plus(".jpg"))
        val fileOutputStream = FileOutputStream(file)

        var matrix = Matrix()
        matrix.postRotate(rotationDegrees)

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

        matrix = Matrix()
        if (rotationDegrees == 270.0f) {
            matrix.preScale(-1.0f, 1.0f)
        }

        var croppedBitmap =
            Bitmap.createBitmap(
                rotateBitmap,
                boundingBox.left,
                boundingBox.top,
                boundingBox.width(),
                boundingBox.height(),
                matrix,
                false
            )

        croppedBitmap = Bitmap.createScaledBitmap(
            croppedBitmap,
            this.captureOptions.faceImageSize.width,
            this.captureOptions.faceImageSize.height,
            false
        )

        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

        fileOutputStream.close()

        return file.absolutePath
    }

    companion object {
        private const val TAG = "FaceAnalyzer"
        private const val NUMBER_OF_IMAGES_LIMIT = 25
    }
}

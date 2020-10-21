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

import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.utils.resize
import ai.cyberlabs.yoonit.camera.utils.scaledBy
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
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Custom camera image analyzer based on face detection bounded on [CameraController].
 */
class FaceAnalyzer(
    private val context: Context,
    private val cameraEventListener: CameraEventListener?,
    private val graphicView: CameraGraphicView,
    private val captureOptions: CaptureOptions,
    private val showDetectionBox: Boolean,
    private val cameraCallback: CameraCallback
) : ImageAnalysis.Analyzer {

    private var analyzerTimeStamp: Long = 0
    private var isFaceDetected: Boolean = false
    private var numberOfImages = 0

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
                //  Check if found face.
                if (faces.isEmpty()) {
                    this.graphicView.clear()
                    this.checkFaceUndetected()
                    return@addOnSuccessListener
                }

                // Get closest face.
                var boundingBox = Rect()
                faces.forEach {
                    if (it.boundingBox.width() > boundingBox.width()) {
                        boundingBox = it.boundingBox
                    }
                }

                val size = max(boundingBox.width(), boundingBox.height())
                boundingBox = boundingBox.resize(size, size)

                if (this.captureOptions.facePaddingPercent != 0f) {
                    // Scale bounding box.
                    boundingBox = boundingBox.scaledBy(this.captureOptions.facePaddingPercent)
                }

                // Scale the closest face.
                val faceBoundingBox = this.scaleBoundingBox(boundingBox, image)

                // Verify the face bounding box.
                if (faceBoundingBox == null) {
                    this.checkFaceUndetected()
                    return@addOnSuccessListener
                }
                this.isFaceDetected = true

                // Draw face bounding box.
                this.toggleDetectionBox(faceBoundingBox)

                if (this.cameraEventListener == null) return@addOnSuccessListener

                // Emit face detected.
                this.cameraEventListener.onFaceDetected(
                    faceBoundingBox.left.toInt(),
                    faceBoundingBox.top.toInt(),
                    faceBoundingBox.width().toInt(),
                    faceBoundingBox.height().toInt()
                )

                // Process image only within interval equal ANALYZE_TIMER.
                val currentTimestamp = System.currentTimeMillis()
                if (currentTimestamp - this.analyzerTimeStamp < this.captureOptions.faceTimeBetweenImages) {
                    return@addOnSuccessListener
                }
                this.analyzerTimeStamp = currentTimestamp

                // Save face image.
                this.saveFaceImage(
                    mediaImage.toBitmap(),
                    boundingBox,
                    imageProxy.imageInfo.rotationDegrees.toFloat())
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
     * Set to show face detection box when face detected..
     *
     * @param faceBoundingBox bounding box of the face.
     */
    private fun toggleDetectionBox(faceBoundingBox: RectF) {
        if (this.showDetectionBox) {
            this.graphicView.drawBoundingBox(faceBoundingBox)
            return
        }

        this.graphicView.clear()
    }

    /**
     * Verify if has any limit of faces saved then save the cropped image and emit onFaceImageCreated event.
     *
     * @param mediaBitmap the original image bitmap.
     * @param boundingBox the face coordinates detected.
     * @param rotationDegrees the rotation degrees to turn the image to portrait.
     */
    private fun saveFaceImage(mediaBitmap: Bitmap, boundingBox: Rect, rotationDegrees: Float) {

        // process face number of images.
        if (this.captureOptions.faceNumberOfImages > 0) {
            if (this.numberOfImages < captureOptions.faceNumberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onFaceImageCreated(
                    this.numberOfImages,
                    this.captureOptions.faceNumberOfImages,
                    this.saveCroppedImage(
                        mediaBitmap,
                        boundingBox,
                        rotationDegrees
                    )
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
            this.saveCroppedImage(
                mediaBitmap,
                boundingBox,
                rotationDegrees
            )
        )
    }

    /**
     * Clear [CameraGraphicView] and emit face not detected once.
     */
    private fun checkFaceUndetected() {
        if (this.isFaceDetected) {
            this.isFaceDetected = false
            this.graphicView.clear()
            if (this.cameraEventListener != null) {
                this.cameraEventListener.onFaceUndetected()
            }
        }
    }

    /**
     * Crop an image bitmap with a face based on the detected face rect coordinates.
     *
     * @param mediaBitmap the original image bitmap.
     * @param boundingBox the face coordinates detected.
     * @param rotationDegrees the rotation degrees to turn the image to portrait.
     * @return the image file path created.
     */
    private fun saveCroppedImage(mediaBitmap: Bitmap, boundingBox: Rect, rotationDegrees: Float): String {
        val path = this.context.externalCacheDir.toString()
        val file = File(path, "yoonit-".plus(this.numberOfImages).plus(".jpg"))
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

        val croppedBitmap =
            Bitmap.createBitmap(
                rotateBitmap,
                boundingBox.left,
                boundingBox.top,
                boundingBox.width(),
                boundingBox.height(),
                matrix,
                false
            )

        val scaledBitmap = Bitmap.createScaledBitmap(
            croppedBitmap,
            this.captureOptions.faceImageSize.width,
            this.captureOptions.faceImageSize.height,
            false
        )

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

        fileOutputStream.close()

        return file.absolutePath
    }

    /**
     * Scale the face bounding box coordinates inside the camera input image.
     *
     * @param boundingBox the detected face bounding box.
     * @param cameraInputImage the camera image input with the face detected.
     * @return the scaled rect of the detected face.
     */
    private fun scaleBoundingBox(boundingBox: Rect, cameraInputImage: InputImage): RectF? {
        val imageHeight = cameraInputImage.height.toFloat()
        val imageWidth = cameraInputImage.width.toFloat()

        if (imageHeight <= 0 || imageWidth <= 0) {
            return null
        }

        val viewAspectRatio: Float = this.graphicView.width.toFloat() / this.graphicView.height.toFloat()
        val imageAspectRatio: Float = imageHeight / imageWidth

        var postScaleWidthOffset = 0f
        var postScaleHeightOffset = 0f
        val scaleFactor: Float

        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = this.graphicView.width.toFloat() / imageHeight
            postScaleHeightOffset = (this.graphicView.width.toFloat() / imageAspectRatio - this.graphicView.height.toFloat()) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = this.graphicView.height.toFloat() / imageWidth
            postScaleWidthOffset = ((this.graphicView.height.toFloat() * imageAspectRatio) - this.graphicView.width.toFloat()) / 2
        }

        val x = if (cameraInputImage.rotationDegrees == 90) {
            this.scale(boundingBox.centerX().toFloat(), scaleFactor) - postScaleWidthOffset
        } else {
            this.graphicView.width - (this.scale(boundingBox.centerX().toFloat(), scaleFactor) - postScaleWidthOffset)
        }
        val y = this.scale(boundingBox.centerY().toFloat(), scaleFactor) - postScaleHeightOffset

        val left = x - this.scale(boundingBox.width() / 2.0f, scaleFactor)
        val top = y - this.scale(boundingBox.height() / 2.0f, scaleFactor)
        val right = x + this.scale(boundingBox.width() / 2.0f, scaleFactor)
        val bottom = y + this.scale(boundingBox.height() / 2.0f, scaleFactor)

        if (left < 0 || top < 0 || right > this.graphicView.width || bottom > this.graphicView.height) {
            return null
        }

        return RectF(left, top, right, bottom)
    }

    /**
     * Scale the image pixel dimension.
     *
     * @param dimension dimension in pixels.
     * @param scaleFactor scale factor to apply.
     * @return the scaled image pixel.
     */
    private fun scale(dimension: Float, scaleFactor: Float): Float = dimension * scaleFactor

    companion object {
        private const val TAG = "FaceAnalyzer"
        private const val NUMBER_OF_IMAGES_LIMIT = 25
    }
}

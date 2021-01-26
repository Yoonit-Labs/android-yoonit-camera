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
import ai.cyberlabs.yoonit.camera.controllers.ComputerVisionController
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
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

    /**
     * Responsible to manipulate everything related with the face bounding box.
     */
    private val faceBoundingBoxController = FaceCoordinatesController(
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
                // Used to crop the face image.
                val closestFace: Face? = this.faceBoundingBoxController.getClosestFace(faces)

                // Transform the camera face coordinates to UI graphic coordinates.
                // Used to draw the face detection box.
                val detectionBox = this.faceBoundingBoxController.getDetectionBox(
                    closestFace,
                    image
                )

                // Verify if has error on the closestFace and detectionBox.
                if (this.hasError(closestFace, detectionBox)) {
                    return@addOnSuccessListener
                }

                // Transform the camera face contour points to UI graphic coordinates.
                // Used to draw the face countours.
                val faceContours = this.faceBoundingBoxController.getFaceContours(closestFace, image)

                // Get face bitmap.
                val faceBitmap: Bitmap = this.getFaceBitmap(
                    mediaImage,
                    closestFace!!,
                    cameraRotation.toFloat()
                )

                // Draw or clean the face detection box and face blur.
                this.handleDraw(faceBitmap, detectionBox!!, faceContours!!)

                // Stop here if camera event listener is not set.
                if (this.cameraEventListener == null) {
                    return@addOnSuccessListener
                }

                // Emit face bounding box.
                this.cameraEventListener.onFaceDetected(
                    detectionBox.left.pxToDPI(this.context),
                    detectionBox.top.pxToDPI(this.context),
                    detectionBox.width().pxToDPI(this.context),
                    detectionBox.height().pxToDPI(this.context)
                )

                // Continue only if current time stamp is within the interval.
                val currentTimestamp = System.currentTimeMillis()
                if (currentTimestamp - this.analyzerTimeStamp < this.captureOptions.timeBetweenImages) {
                    return@addOnSuccessListener
                }
                this.analyzerTimeStamp = currentTimestamp

                // Computer Vision Inference.
                val inferences: ArrayList<android.util.Pair<String, FloatArray>> =
                    if (this.captureOptions.computerVision.enable)
                        ComputerVisionController.getInferences(this.captureOptions.computerVision.modelMap, faceBitmap)
                    else arrayListOf()

                // Save image captured.
                val imagePath =
                    if (this.captureOptions.saveImageCaptured) this.handleSaveImage(faceBitmap)
                    else ""

                // Handle to emit image path and the inferences.
                this.handleEmitImageCaptured(imagePath, inferences)
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

    private fun hasError(closestFace: Face?, detectionBox: RectF?): Boolean {

        // Get error if exist in the closestFace or detectionBox.
        val error = this.faceBoundingBoxController.getError(
            closestFace,
            detectionBox
        )

        // Emit once if exist error in the closestFace or detectionBox.
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
     * 3. Crop image if necessary;
     * 4. Mirror image if necessary;
     * 5. Scale image if necessary;
     *
     * @param mediaImage The camera frame image;
     * @param closestFace The face detected bounding box;
     * @param cameraRotation The camera rotation;
     *
     * @return the face bitmap.
     */
    private fun getFaceBitmap(
        mediaImage: Image,
        closestFace: Face,
        cameraRotation: Float
    ): Bitmap {

        val colorEncodedBitmap: Bitmap = when(this.captureOptions.colorEncoding) {
            "YUV" -> mediaImage.toYUVBitmap()
            else -> mediaImage.toRGBBitmap(context)
        }

        val faceBitmap: Bitmap = colorEncodedBitmap
            .rotate(cameraRotation)
            .crop(closestFace.boundingBox)
            .mirror(cameraRotation)

        return Bitmap.createScaledBitmap(
            faceBitmap,
            this.captureOptions.imageOutputWidth,
            this.captureOptions.imageOutputHeight,
            false
        )
    }

    /**
     * Handle draw face detection box and/or the face blur;
     *
     * @param faceBitmap The face bitmap;
     * @param faceDetectionBox The face bounding box within the camera frame image;
     */
    private fun handleDraw(
            faceBitmap: Bitmap,
            faceDetectionBox: RectF,
            faceContours: MutableList<PointF>
    ) {
        if (
            !this.captureOptions.faceDetectionBox &&
            !this.captureOptions.blurFaceDetectionBox &&
            !this.captureOptions.faceContours
        ) {
            this.graphicView.clear()
        }

        if (this.captureOptions.faceDetectionBox) {
            this.graphicView.drawFaceDetectionBox(faceDetectionBox)
        }

        if (this.captureOptions.blurFaceDetectionBox) {
            this.graphicView.drawFaceBlur(
                faceDetectionBox,
                faceBitmap
            )
        }

        if (this.captureOptions.faceContours) {
            this.graphicView.drawFaceLandmarks(faceContours)
        }

        this.graphicView.postInvalidate()
    }

    /**
     * Handle emit face image file created.
     *
     * @param imagePath The image file path.
     * @param inferences The computer vision inferences based in the models.
     */
    private fun handleEmitImageCaptured(
        imagePath: String,
        inferences: ArrayList<android.util.Pair<String, FloatArray>>
    ) {

        // process face number of images.
        if (this.captureOptions.numberOfImages > 0) {
            if (this.numberOfImages < captureOptions.numberOfImages) {
                this.numberOfImages++
                this.cameraEventListener?.onImageCaptured(
                    "face",
                    this.numberOfImages,
                    this.captureOptions.numberOfImages,
                    imagePath,
                    inferences
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
            imagePath,
            inferences
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

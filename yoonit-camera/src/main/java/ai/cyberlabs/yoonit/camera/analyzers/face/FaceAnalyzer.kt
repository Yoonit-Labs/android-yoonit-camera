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
    private val cameraCallback: CameraCallback
) : ImageAnalysis.Analyzer {

    private var analyzerTimeStamp: Long = 0
    private var isValid: Boolean = true
    private var numberOfImages = 0

    /**
     * Responsible to manipulate everything related with the face bounding box.
     */
    private val faceCoordinatesController = FaceCoordinatesController(this.graphicView)

    /**
     * Receive image from CameraX API.
     *
     * @param imageProxy image from CameraX API.
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: return

        val cameraRotation: Int =
            if (CaptureOptions.isScreenFlipped) 270
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
                val closestFace: Face? = this.faceCoordinatesController.getClosestFace(faces)

                closestFace?.let {
                    // Transform the camera face coordinates to UI graphic coordinates.
                    // Used to draw the face detection box.
                    val detectionBox = this.faceCoordinatesController.getDetectionBox(
                            closestFace,
                            image
                    )

                    // Verify if has error on the closestFace and detectionBox.
                    if (this.hasError(detectionBox)) {
                        return@addOnSuccessListener
                    }

                    // Transform the camera face contour points to UI graphic coordinates.
                    // Used to draw the face contours.
                    val faceContours = this.faceCoordinatesController.getFaceContours(closestFace, image)

                    // Get face bitmap.
                    val faceBitmap: Bitmap = this.getFaceBitmap(
                            mediaImage,
                            closestFace,
                            cameraRotation.toFloat()
                    )

                    // Draw or clean the face detection box and face blur.
                    this.handleDraw(faceBitmap, detectionBox, faceContours)

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
                    if (currentTimestamp - this.analyzerTimeStamp < CaptureOptions.timeBetweenImages) {
                        return@addOnSuccessListener
                    }
                    this.analyzerTimeStamp = currentTimestamp

                    // Computer Vision Inference.
                    val inferences: ArrayList<android.util.Pair<String, FloatArray>> =
                            if (CaptureOptions.computerVision.enable)
                                ComputerVisionController.getInferences(CaptureOptions.computerVision.modelMap, faceBitmap)
                            else arrayListOf()

                    // Save image captured.
                    val imagePath =
                            if (CaptureOptions.saveImageCaptured) this.handleSaveImage(faceBitmap)
                            else ""

                    // Handle to emit image path and the inferences.
                    this.handleEmitImageCaptured(imagePath, inferences)
                }
            }
            .addOnFailureListener { e ->
                this.cameraEventListener?.let {
                    this.cameraEventListener.onError(e.toString())
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
                detector.close()
            }
    }

    private fun hasError(detectionBox: RectF): Boolean {

        // Get error if exist in the closestFace or detectionBox.
        val error = this.faceCoordinatesController.getError(
            detectionBox
        )

        // Emit once if exist error in the closestFace or detectionBox.
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

        val colorEncodedBitmap: Bitmap = when(CaptureOptions.colorEncoding) {
            "YUV" -> mediaImage.toYUVBitmap()
            else -> mediaImage.toRGBBitmap(context)
        }

        val faceBitmap: Bitmap = colorEncodedBitmap
            .rotate(cameraRotation)
            .crop(closestFace.boundingBox)
            .mirror(cameraRotation)

        return Bitmap.createScaledBitmap(
            faceBitmap,
            CaptureOptions.imageOutputWidth,
            CaptureOptions.imageOutputHeight,
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
            !CaptureOptions.faceDetectionBox &&
            !CaptureOptions.blurFaceDetectionBox &&
            !CaptureOptions.faceContours
        ) {
            this.graphicView.clear()
        }

        if (CaptureOptions.faceDetectionBox) {
            this.graphicView.drawFaceDetectionBox(faceDetectionBox)
        }

        if (CaptureOptions.blurFaceDetectionBox) {
            this.graphicView.drawFaceBlur(
                faceDetectionBox,
                faceBitmap
            )
        }

        if (CaptureOptions.faceContours) {
            this.graphicView.drawFaceContours(faceContours)
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
            CaptureOptions.numberOfImages,
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

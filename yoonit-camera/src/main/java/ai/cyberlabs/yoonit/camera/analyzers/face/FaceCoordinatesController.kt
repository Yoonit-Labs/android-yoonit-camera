package ai.cyberlabs.yoonit.camera.analyzers.face

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.Message
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.resize
import ai.cyberlabs.yoonit.camera.utils.scaledBy
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import kotlin.math.max

/**
 * Responsible to manipulate everything related with the face bounding box.
 */
class FaceCoordinatesController(
    private val graphicView: CameraGraphicView
) {

    /**
     * Get closest face.
     * Can be null if no face found.
     *
     * @param faces the face list camera detected;
     * @return the closest face. null if no face found.
     */
    fun getClosestFace(faces: List<Face>): Face? {

        //  Check if found face.
        if (faces.isEmpty()) {
            return null
        }

        // Get closest face.
        var closestFace: Face? = null
        faces.forEach {
            if (closestFace == null ||
                it.boundingBox.width() > closestFace!!.boundingBox.width()
            ) {
                closestFace = it
            }
        }

        // Transform bounding box rectangle in square.
        val size = max(
            closestFace!!.boundingBox.width(),
            closestFace!!.boundingBox.height()
        )
        closestFace!!.boundingBox.set(closestFace!!.boundingBox.resize(size, size))

        return closestFace
    }

    /**
     * Transform the detected face bounding box coordinates in the UI graphic
     * coordinates, based in the [CameraGraphicView] and [InputImage] dimensions.
     *
     * @param face the detected face bounding box.
     * @param cameraInputImage the camera image input with the face detected.
     * @return the detection box rect of the detected face. null if face is null or detection box is
     * out of the screen.
     */
    fun getDetectionBox(face: Face, cameraInputImage: InputImage): RectF {
        var boundingBox: Rect = face.boundingBox

        // Scale bounding box.
        if (CaptureOptions.facePaddingPercent != 0f) {
            boundingBox = boundingBox.scaledBy(CaptureOptions.facePaddingPercent)
        }

        val imageHeight = cameraInputImage.height.toFloat()
        val imageWidth = cameraInputImage.width.toFloat()

        if (imageHeight <= 0 || imageWidth <= 0) {
            return RectF()
        }

        val viewAspectRatio: Float =
            this.graphicView.width.toFloat() / this.graphicView.height.toFloat()
        val imageAspectRatio: Float = imageHeight / imageWidth

        var postScaleWidthOffset = 0f
        var postScaleHeightOffset = 0f
        val scaleFactor: Float

        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = this.graphicView.width.toFloat() / imageHeight
            postScaleHeightOffset =
                (this.graphicView.width.toFloat() / imageAspectRatio - this.graphicView.height.toFloat()) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = this.graphicView.height.toFloat() / imageWidth
            postScaleWidthOffset =
                ((this.graphicView.height.toFloat() * imageAspectRatio) - this.graphicView.width.toFloat()) / 2
        }

        var x = if (cameraInputImage.rotationDegrees == 90) {
            this.scale(boundingBox.centerX().toFloat(), scaleFactor) - postScaleWidthOffset
        } else {
            this.graphicView.width - (this.scale(boundingBox.centerX().toFloat(), scaleFactor) - postScaleWidthOffset)
        }

        var y = this.scale(boundingBox.centerY().toFloat(), scaleFactor) - postScaleHeightOffset

        // Adjust the "x" and "y" coordinates when screen flipped. - - - - - - - - - - - - - - - -
        x =
            if (CaptureOptions.isScreenFlipped) this.graphicView.width - x
            else x
        y =
            if (CaptureOptions.isScreenFlipped) this.graphicView.height - y
            else y
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -


        val left = x - this.scale(boundingBox.width() / 2.0f, scaleFactor)
        val top = y - this.scale(boundingBox.height() / 2.0f, scaleFactor)
        val right = x + this.scale(boundingBox.width() / 2.0f, scaleFactor)
        val bottom = y + this.scale(boundingBox.height() / 2.0f, scaleFactor)

        return RectF(left, top, right, bottom)
    }

    fun getFaceContours(face: Face, cameraInputImage: InputImage): MutableList<PointF> {

        val imageHeight = cameraInputImage.height.toFloat()
        val imageWidth = cameraInputImage.width.toFloat()

        if (imageHeight <= 0 || imageWidth <= 0) {
            return mutableListOf()
        }

        val viewAspectRatio: Float =
                this.graphicView.width.toFloat() / this.graphicView.height.toFloat()
        val imageAspectRatio: Float = imageHeight / imageWidth

        var postScaleWidthOffset = 0f
        var postScaleHeightOffset = 0f
        val scaleFactor: Float

        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = this.graphicView.width.toFloat() / imageHeight
            postScaleHeightOffset =
                    (this.graphicView.width.toFloat() / imageAspectRatio - this.graphicView.height.toFloat()) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = this.graphicView.height.toFloat() / imageWidth
            postScaleWidthOffset =
                    ((this.graphicView.height.toFloat() * imageAspectRatio) - this.graphicView.width.toFloat()) / 2
        }

        val faceContours = mutableListOf<PointF>()

        face.allContours.forEach {faceContour ->
            faceContour.points.forEach {point ->
                val x = if (cameraInputImage.rotationDegrees == 90) {
                    this.scale(point.x, scaleFactor) - postScaleWidthOffset
                } else {
                    this.graphicView.width - (this.scale(point.x, scaleFactor) - postScaleWidthOffset)
                }

                val y = this.scale(point.y, scaleFactor) - postScaleHeightOffset

                faceContours.add(PointF(x,y))
            }
        }

        return faceContours
    }

    /**
     *  Get the error message if exist based in the capture options rules,
     *  detection box.
     *
     *  @param detectionBox The closest face detected bounding box normalized coordinates.
     *
     *  @return null for no error:
     *  - INVALID_CAPTURE_FACE_MIN_SIZE
     *  - INVALID_CAPTURE_FACE_MAX_SIZE
     *  - INVALID_CAPTURE_FACE_OUT_OF_ROI
     *  - INVALID_CAPTURE_FACE_ROI_MIN_SIZE
     */
    fun getError(detectionBox: RectF): String? {
        
        val screenWidth = this.graphicView.width
        val screenHeight = this.graphicView.height

        if (detectionBox.isEmpty) {
            return ""
        }

        val isOutOfTheScreen =
            detectionBox.left < 0 ||
                    detectionBox.top < 0 ||
                    detectionBox.right > screenWidth ||
                    detectionBox.bottom > screenHeight

        if (isOutOfTheScreen) {
            return ""
        }

        // This variable is the face detection box percentage in relation with the
        // UI graphic view. The value must be between 0 and 1.
        val detectionBoxRelatedWithScreen: Float = detectionBox.width() / screenWidth

        if (detectionBoxRelatedWithScreen < CaptureOptions.faceCaptureMinSize) {
            return Message.INVALID_CAPTURE_FACE_MIN_SIZE
        }

        if (detectionBoxRelatedWithScreen > CaptureOptions.faceCaptureMaxSize) {
            return Message.INVALID_CAPTURE_FACE_MAX_SIZE
        }

        if (CaptureOptions.faceROI.enable) {

            // Detection box offsets.
            val topOffset: Float = detectionBox.top / screenHeight
            val rightOffset: Float = (screenWidth - detectionBox.right) / screenWidth
            val bottomOffset: Float = (screenHeight - detectionBox.bottom) / screenHeight
            val leftOffset: Float = detectionBox.left / screenWidth

            if (CaptureOptions.faceROI.isOutOf(
                    topOffset,
                    rightOffset,
                    bottomOffset,
                    leftOffset
                )
            ) {
                return Message.INVALID_CAPTURE_FACE_OUT_OF_ROI
            }

            if (CaptureOptions.faceROI.hasChanges) {

                // Face is inside the region of interest and faceROI is setted.
                // Face is smaller than the defined "minimumSize".
                val roiWidth: Float =
                    screenWidth -
                            ((CaptureOptions.faceROI.rightOffset + CaptureOptions.faceROI.leftOffset) * screenWidth)
                val faceRelatedWithROI: Float = detectionBox.width() / roiWidth

                if (CaptureOptions.faceROI.minimumSize > faceRelatedWithROI) {
                    return Message.INVALID_CAPTURE_FACE_ROI_MIN_SIZE
                }
            }
        }

        return null
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
        private const val TAG = "FaceBoundingBoxController"
    }
}

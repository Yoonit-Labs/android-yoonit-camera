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

package ai.cyberlabs.yoonit.camera.analyzers

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import ai.cyberlabs.yoonit.camera.Message
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.utils.scale
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import com.google.mlkit.vision.common.InputImage

/**
 * Responsible to manipulate everything related with the UI coordinates.
 */
class CoordinatesController(
    private val graphicView: CameraGraphicView
) {
    /**
     * Transform the detected bounding box coordinates in the UI graphic
     * coordinates, based in the [CameraGraphicView] and [InputImage] dimensions.
     *
     * @param boundingBox the bounding box.
     * @param imageWidth the camera image input width.
     * @param imageHeight the camera image input height.
     * @return the detection box rect. Return empty rect if null or detection box is out of the screen.
     */
    fun getDetectionBox(
        boundingBox: Rect,
        imageWidth: Float,
        imageHeight: Float
    ): RectF {
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

        var x = this.scale(boundingBox.centerX().toFloat(), scaleFactor) - postScaleWidthOffset
        if (CaptureOptions.cameraLens == CameraSelector.LENS_FACING_BACK) {
            x = this.graphicView.width - x
        }
        val y = this.scale(boundingBox.centerY().toFloat(), scaleFactor) - postScaleHeightOffset

        val left = x - this.scale(boundingBox.width() / 2.0f, scaleFactor)
        val top = y - this.scale(boundingBox.height() / 2.0f, scaleFactor)
        val right = x + this.scale(boundingBox.width() / 2.0f, scaleFactor)
        val bottom = y + this.scale(boundingBox.height() / 2.0f, scaleFactor)

        return RectF(
            left,
            top,
            right,
            bottom
        ).scale(
            CaptureOptions.detectionTopSize,
            CaptureOptions.detectionRightSize,
            CaptureOptions.detectionBottomSize,
            CaptureOptions.detectionLeftSize
        )
    }

    fun getFaceContours(
        contours: MutableList<PointF>,
        imageWidth: Float,
        imageHeight: Float
    ): MutableList<PointF> {
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

        contours.forEach { point ->
            var x = this.scale(point.x, scaleFactor) - postScaleWidthOffset
            if (CaptureOptions.cameraLens == CameraSelector.LENS_FACING_BACK) {
                x = this.graphicView.width - x
            }
            val y = this.scale(point.y, scaleFactor) - postScaleHeightOffset
            faceContours.add(PointF(x, y))
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

        if (CaptureOptions.ROI.enable) {

            // Detection box offsets.
            val topOffset: Float = detectionBox.top / screenHeight
            val rightOffset: Float = (screenWidth - detectionBox.right) / screenWidth
            val bottomOffset: Float = (screenHeight - detectionBox.bottom) / screenHeight
            val leftOffset: Float = detectionBox.left / screenWidth

            if (CaptureOptions.ROI.isOutOf(
                    topOffset,
                    rightOffset,
                    bottomOffset,
                    leftOffset
                )
            ) {
                return Message.INVALID_OUT_OF_ROI
            }

            if (CaptureOptions.ROI.hasChanges) {

                // Face is inside the region of interest and faceROI is setted.
                // Face is smaller than the defined "minimumSize".
                val roiWidth: Float =
                    screenWidth -
                        ((CaptureOptions.ROI.rightOffset + CaptureOptions.ROI.leftOffset) * screenWidth)
                val faceRelatedWithROI: Float = detectionBox.width() / roiWidth

                if (CaptureOptions.minimumSize > faceRelatedWithROI) {
                    return Message.INVALID_MINIMUM_SIZE
                }
            }
            return null
        }

        // This variable is the face detection box percentage in relation with the
        // UI graphic view. The value must be between 0 and 1.
        val detectionBoxRelatedWithScreen: Float = detectionBox.width() / screenWidth

        if (detectionBoxRelatedWithScreen < CaptureOptions.minimumSize) {
            return Message.INVALID_MINIMUM_SIZE
        }

        if (detectionBoxRelatedWithScreen > CaptureOptions.maximumSize) {
            return Message.INVALID_MAXIMUM_SIZE
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

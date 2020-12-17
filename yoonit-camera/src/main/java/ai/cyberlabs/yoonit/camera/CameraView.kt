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

import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import ai.cyberlabs.yoonit.camera.models.FaceROI
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.cameraview_layout.view.*

/**
 * This class represents the camera layout and your functions.
 */
open class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    // Model to set CameraView features options.
    private var captureOptions: CaptureOptions = CaptureOptions()

    // Camera controller object.
    private var cameraController: CameraController

    // Camera interface event listeners object.
    private var cameraEventListener: CameraEventListener? = null

    /**
     * Inflate CameraView layout and instantiate [CameraController].
     */
    init {
        LayoutInflater.from(context).inflate(
            R.layout.cameraview_layout,
            this,
            true
        )

        this.cameraController = CameraController(
            context,
            previewView,
            graphicView,
            this.captureOptions
        )
    }

    /**
     * Start camera preview if has permission.
     */
    fun startPreview() {
        this.cameraController.startPreview()
    }

    /**
     * Start capture type: none, face or qrcode.
     * Must have started preview, see [startPreview].
     *
     * @param captureType The capture type: "none" | "face" | "qrcode" | "frame".
     */
    fun startCaptureType(captureType: String) {
        if (!this.cameraController.isPreviewStarted) {
            Log.w(TAG, KeyError.NOT_STARTED_PREVIEW)
            return
        }

        when (captureType) {
            "none" -> this.cameraController.startCaptureType(CaptureType.NONE)

            "face" -> this.cameraController.startCaptureType(CaptureType.FACE)

            "qrcode" -> this.cameraController.startCaptureType(CaptureType.QRCODE)

            "frame" -> this.cameraController.startCaptureType(CaptureType.FRAME)

            else -> throw IllegalArgumentException(KeyError.INVALID_CAPTURE_TYPE)
        }
    }

    /**
     * Stop camera image capture.
     */
    fun stopCapture() {
        this.cameraController.stopAnalyzer()
    }

    /**
     * Toggle between Front and Back Camera.
     */
    fun toggleCameraLens() {
        this.cameraController.toggleCameraLens()
    }

    /**
     * Get current camera lens.
     *
     * @return value 0 is front camera. Value 1 is back camera.
     */
    fun getCameraLens(): Int {
        return this.cameraController.getCameraLens()
    }

    /**
     * Expose explicit set for [CameraEventListener] instance.
     */
    fun setCameraEventListener(cameraEventListener: CameraEventListener) {
        this.cameraEventListener = cameraEventListener
        this.cameraController.cameraEventListener = cameraEventListener
    }

    /**
     * Set number of face/frame file images to create.
     *
     * @param numberOfImages The number of images to create.
     */
    fun setNumberOfImages(numberOfImages: Int) {
        if (numberOfImages < 0) {
            throw IllegalArgumentException(KeyError.INVALID_NUMBER_OF_IMAGES)
        }

        this.captureOptions.numberOfImages = numberOfImages
    }

    /**
     * Set saving face/frame images time interval in milli seconds.
     *
     * @param timeBetweenImages The time in milli seconds.
     * Default value is 1000.
     */
    fun setTimeBetweenImages(timeBetweenImages: Long) {
        if (timeBetweenImages < 0) {
            throw IllegalArgumentException(KeyError.INVALID_TIME_BETWEEN_IMAGES)
        }

        this.captureOptions.timeBetweenImages = timeBetweenImages
    }

    /**
     * Set face image width to be created.
     *
     * @param width The file image width in pixels.
     * Default value is 200.
     */
    fun setOutputImageWidth(width: Int) {
        if (width <= 0) {
            throw IllegalArgumentException(KeyError.INVALID_OUTPUT_IMAGE_WIDTH)
        }

        this.captureOptions.imageOutputWidth = width
    }

    /**
     * Set face image height to be created.
     *
     * @param height The file image height in pixels.
     * Default value is 200.
     */
    fun setOutputImageHeight(height: Int) {
        if (height <= 0) {
            throw IllegalArgumentException(KeyError.INVALID_OUTPUT_IMAGE_HEIGHT)
        }

        this.captureOptions.imageOutputHeight = height
    }

    /**
     * Set to enable/disable save images when capturing face/frame.
     *
     * @param enable The indicator to enable or disable the face/frame save images.
     * Default value is false.
     */
    fun setSaveImageCaptured(enable: Boolean) {
        this.captureOptions.saveImageCaptured = enable
    }

    /**
     * Set to show/hide face detection box when face detected.
     *
     * @param enable The indicator to show or hide the face detection box.
     * Default value is true.
     */
    fun setFaceDetectionBox(enable: Boolean) {
        this.captureOptions.faceDetectionBox = enable
    }

    /**
     * Set saving face images time interval in milli seconds.
     *
     * @param facePaddingPercent The percent to enlarge the bounding box.
     * Default value is 0.0.
     */
    fun setFacePaddingPercent(facePaddingPercent: Float) {
        if (facePaddingPercent < 0.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_PADDING_PERCENT)
        }

        this.captureOptions.facePaddingPercent = facePaddingPercent
    }

    /**
     * Limit the minimum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     *
     * For example, if set 0.5, will capture face with the detection box width occupying
     * at least 50% of the screen width.
     *
     * @param faceCaptureMinSize The face capture min size value.
     * Default value is 0.0f.
     */
    fun setFaceCaptureMinSize(faceCaptureMinSize: Float) {
        if (faceCaptureMinSize < 0.0f || faceCaptureMinSize > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_CAPTURE_MIN_SIZE)
        }

        this.captureOptions.faceCaptureMinSize = faceCaptureMinSize
    }

    /**
     * Limit the maximum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     *
     * For example, if set 0.7, will capture face with the detection box width occupying
     * at least 70% of the screen width.
     *
     * @param faceCaptureMaxSize The face capture max size value.
     * Default value is 1.0f.
     */
    fun setFaceCaptureMaxSize(faceCaptureMaxSize: Float) {
        if (faceCaptureMaxSize < 0.0f || faceCaptureMaxSize > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_CAPTURE_MAX_SIZE)
        }

        this.captureOptions.faceCaptureMaxSize = faceCaptureMaxSize
    }

    /**
     * Set to apply enable/disable face region of interest.
     *
     * @param enable The indicator to enable/disable face region of interest.
     * Default value is `false`.
     */
    fun setFaceROIEnable(enable: Boolean) {
        this.captureOptions.faceROI.enable = enable
    }

    /**
     * Tried to input invalid face region of interest top offset.
     *
     * @param percentage The "above" area of the face bounding box in percentage.
     * Default value is 0.0f.
     */
    fun setFaceROITopOffset(topOffset: Float) {
        if (topOffset < 0.0f || topOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_ROI_TOP_OFFSET)
        }

        this.captureOptions.faceROI.topOffset = topOffset
    }

    /**
     * Tried to input invalid face region of interest right offset.
     *
     * @param percentage The "right" area of the face bounding box in percentage.
     * Default value is 0.0f.
     */
    fun setFaceROIRightOffset(rightOffset: Float) {
        if (rightOffset < 0.0f || rightOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_ROI_RIGHT_OFFSET)
        }

        this.captureOptions.faceROI.rightOffset = rightOffset
    }

    /**
     * Tried to input invalid face region of interest bottom offset.
     *
     * @param percentage The "bottom" area of the face bounding box in percentage.
     * Default value is 0.0f.
     */
    fun setFaceROIBottomOffset(bottomOffset: Float) {
        if (bottomOffset < 0.0f || bottomOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_ROI_BOTTOM_OFFSET)
        }

        this.captureOptions.faceROI.bottomOffset = bottomOffset
    }

    /**
     * Tried to input invalid face region of interest left offset.
     *
     * @param percentage The "left" area of the face bounding box in percentage.
     * Default value is 0.0f.
     */
    fun setFaceROILeftOffset(leftOffset: Float) {
        if (leftOffset < 0.0f || leftOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_ROI_LEFT_OFFSET)
        }

        this.captureOptions.faceROI.leftOffset = leftOffset
    }

    /**
     * Set face minimum size in relation of the region of interest.
     *
     * @param minimumSize: Represents in percentage [0, 1].
     * Default value is `0`.
     */
    fun setFaceROIMinSize(minimumSize: Float) {
        if (minimumSize < 0.0 || minimumSize > 1.0) {
            throw IllegalArgumentException(KeyError.INVALID_FACE_ROI_MIN_SIZE)
        }

        this.captureOptions.faceROI.minimumSize = minimumSize
    }

    fun flipScreen() {
        this.captureOptions.isScreenFlipped = !this.captureOptions.isScreenFlipped

        this.rotation =
            if (this.captureOptions.isScreenFlipped) 180f
            else 0f
    }

    companion object {
        private const val TAG = "CameraView"
    }
}

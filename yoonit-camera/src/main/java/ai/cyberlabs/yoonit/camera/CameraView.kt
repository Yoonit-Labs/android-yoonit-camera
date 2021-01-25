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

import ai.cyberlabs.yoonit.camera.controllers.CameraController
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import kotlinx.android.synthetic.main.cameraview_layout.view.*
import java.io.File

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

        graphicView.captureOptions = this.captureOptions

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
     *
     * @param captureType The capture type: "none" | "face" | "qrcode" | "frame".
     */
    fun startCaptureType(captureType: String) {
        when (captureType) {
            "none" -> {
                this.captureOptions.type = CaptureType.NONE
                this.cameraController.startCaptureType()
            }

            "face" -> {
                this.captureOptions.type = CaptureType.FACE
                this.cameraController.startCaptureType()
            }

            "qrcode" -> {
                this.captureOptions.type = CaptureType.QRCODE
                this.cameraController.startCaptureType()
            }

            "frame" -> {
                this.captureOptions.type = CaptureType.FRAME
                this.cameraController.startCaptureType()
            }

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
     * Destroy camera.
     */
    fun destroy() {
        this.cameraController.destroy()
    }

    /**
     * Set camera lens: "front" or "back".
     *
     * @param cameraLens "back" || "front"
     */
    fun setCameraLens(cameraLens: String) {
        if (cameraLens != "front" && cameraLens != "back") {
            throw IllegalArgumentException(KeyError.INVALID_CAMERA_LENS)
        }

        val cameraSelector: Int =
            if (cameraLens == "front") CameraSelector.LENS_FACING_FRONT
            else CameraSelector.LENS_FACING_BACK


        if (this.captureOptions.cameraLens != cameraSelector) {
            this.cameraController.toggleCameraLens()
        }
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
     * @return "front" || "back".
     * Default value is "front".
     */
    fun getCameraLens(): String {
        return if (this.captureOptions.cameraLens == CameraSelector.LENS_FACING_FRONT) "front" else "back"
    }

    /**
     * Expose explicit set for [CameraEventListener] instance.
     */
    fun setCameraEventListener(cameraEventListener: CameraEventListener?) {
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
     * Set to show/hide face landmarks when face detected.
     *
     * @param enable The indicator to show or hide the face landmarks.
     * Default value is true.
     */
    fun setLandmarksDetection(enable: Boolean) {
        this.captureOptions.faceLandmarks = enable
    }

    /**
     * Set face landmarks color.
     *
     * @param red Integer that represent red color.
     * @param green Integer that represent green color.
     * @param blue Integer that represent blue color.
     * Default value is 255, 255, 255 (white color).
     */
    fun setFaceLandmarksColor(red: Int, green: Int, blue: Int) {
        this.captureOptions.faceLandmarksColor = Color.argb(100, red, green, blue)
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
     * @param topOffset The "above" area of the face bounding box in percentage.
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
     * @param rightOffset The "right" area of the face bounding box in percentage.
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
     * @param bottomOffset The "bottom" area of the face bounding box in percentage.
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
     * @param leftOffset The "left" area of the face bounding box in percentage.
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

    /**
     * Enable/disable blur in face detection box.
     *
     * @param enable The indicator to enable/disable face detection box blur
     * Default value is `false`.
     */
    fun setBlurFaceDetectionBox(enable: Boolean) {
        this.captureOptions.blurFaceDetectionBox = enable
    }

    /**
     * Set the color encoding for the saved images.
     *
     * @param colorEncoding The color encoding type: "RGB" | "YUV".
     * Default value is `RGB`.
     */
    fun setColorEncodingCapture(colorEncoding: String) {
        if (colorEncoding != "RGB" && colorEncoding != "YUV") {
            throw IllegalArgumentException(KeyError.INVALID_IMAGE_CAPTURE_COLOR_ENCODING)
        }

        this.captureOptions.colorEncoding = colorEncoding
    }

    /**
     * Enable/disable computer vision usage.
     *
     * @param enable The indicator to enable/disable computer vision usage.
     * Default value is `false`.
     */
    fun setComputerVision(enable: Boolean) {
        this.captureOptions.computerVision.enable = enable
    }

    /**
     * Set the computer vision model paths to load.
     *
     * @param modelPaths The computer vision absolute model file path array list.
     * Default value is an empty array.
     */
    fun setComputerVisionLoadModels(modelPaths: ArrayList<String>) {
        modelPaths.forEach {
            modelPath ->
            if (!File(modelPath).exists()) {
                throw IllegalArgumentException("${KeyError.INVALID_COMPUTER_VISION_MODEL_PATHS}: $modelPath")
            }
        }

        this.captureOptions.computerVision.paths = modelPaths
    }

    /**
     * Clear loaded computer vision models.
     */
    fun computerVisionClearModels() {
        this.captureOptions.computerVision.clear()
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

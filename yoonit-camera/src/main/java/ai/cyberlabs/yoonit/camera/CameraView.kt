/**
 * +-+-+-+-+-+-+
 * |y|o|o|n|i|t|
 * +-+-+-+-+-+-+
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Yoonit Camera lib for Android applications                      |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2020-2021        |
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
 * This class represents the camera layout.
 */
open class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

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
            graphicView
        )
    }

    /**
     * Start camera preview if has permission.
     */
    fun startPreview() {
        this.cameraController.startPreview()
    }

    /**
     * Start capture type: none, face, qrcode or frame.
     *
     * @param captureType The capture type: "none" | "face" | "qrcode" | "frame".
     */
    fun startCaptureType(captureType: String) {

        when (captureType) {
            "none" -> {
                CaptureOptions.type = CaptureType.NONE
                this.cameraController.startCaptureType()
            }

            "face" -> {
                CaptureOptions.type = CaptureType.FACE
                this.cameraController.startCaptureType()
            }

            "qrcode" -> {
                CaptureOptions.type = CaptureType.QRCODE
                this.cameraController.startCaptureType()
            }

            "frame" -> {
                CaptureOptions.type = CaptureType.FRAME
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
     * Toggle between Front and Back Camera.
     */
    fun toggleCameraLens() {
        this.cameraController.toggleCameraLens()
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

        if (CaptureOptions.cameraLens != cameraSelector) {
            this.cameraController.toggleCameraLens()
        }
    }

    /**
     * Get current camera lens.
     *
     * @return "front" || "back".
     * Default value is "front".
     */
    fun getCameraLens(): String {
        return if (CaptureOptions.cameraLens == CameraSelector.LENS_FACING_FRONT) "front" else "back"
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

        CaptureOptions.numberOfImages = numberOfImages
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

        CaptureOptions.timeBetweenImages = timeBetweenImages
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

        CaptureOptions.imageOutputWidth = width
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

        CaptureOptions.imageOutputHeight = height
    }

    /**
     * Set to enable/disable save images when capturing face/frame.
     *
     * @param enable The indicator to enable or disable the face/frame save images.
     * Default value is false.
     */
    fun setSaveImageCaptured(enable: Boolean) {
        CaptureOptions.saveImageCaptured = enable
    }

    /**
     * Set to enable/disable face detection box when face detected.
     *
     * @param enable The indicator to show or hide the face detection box.
     * Default value is true.
     */
    fun setFaceDetectionBox(enable: Boolean) {
        CaptureOptions.faceDetectionBox = enable
    }

    /**
     * Set to enable/disable face contours when face detected.
     *
     * @param enable The indicator to show or hide the face contours.
     * Default value is true.
     */
    fun setFaceContours(enable: Boolean) {
        CaptureOptions.faceContours = enable
    }

    /**
     * Set face contours ARGB color.
     *
     * @param alpha The alpha value.
     * @param red The red value.
     * @param green The green value.
     * @param blue The blue value.
     * Default value is `(100, 255, 255, 255)`.
     */
    fun setFaceContoursColor(alpha: Int, red: Int, green: Int, blue: Int) {
        if (
            alpha < 0 || alpha > 255 ||
            red < 0 || red > 255 ||
            green < 0 || green > 255 ||
            blue < 0 || blue > 255
        ) {
            throw java.lang.IllegalArgumentException(KeyError.INVALID_FACE_CONTOURS_COLOR)
        }

        CaptureOptions.faceContoursColor = Color.argb(alpha, red, green, blue)
    }

    /**
     * Set to enable/disable the device torch. Available only to camera lens "back".
     *
     * @param enable The indicator to set enable/disable the device torch.
     * Default value is false.
     */
    fun setTorch(enable: Boolean) {
        cameraController.setTorch(enable)
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

        CaptureOptions.facePaddingPercent = facePaddingPercent
    }

    /**
     * Limit the minimum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     *
     * For example, if set 0.5, will capture face with the detection box width occupying
     * at least 50% of the screen width.
     *
     * @param minimumSize The face capture min size value.
     * Default value is 0.0f.
     */
    fun setDetectionMinSize(minimumSize: Float) {
        if (minimumSize < 0.0f || minimumSize > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_MINIMUM_SIZE)
        }

        CaptureOptions.minimumSize = minimumSize
    }

    /**
     * Limit the maximum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     *
     * For example, if set 0.7, will capture face with the detection box width occupying
     * at least 70% of the screen width.
     *
     * @param maximumSize The face capture max size value.
     * Default value is 1.0f.
     */
    fun setDetectionMaxSize(maximumSize: Float) {
        if (maximumSize < 0.0f || maximumSize > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_MAXIMUM_SIZE)
        }

        CaptureOptions.maximumSize = maximumSize
    }

    /**
    Set to apply enable/disable region of interest.

    * @param enable: The indicator to enable/disable region of interest.
    Default value is `false`.
     */
    fun setROI(enable: Boolean) {
        CaptureOptions.roi.enable = enable
    }

    /**
    Camera preview top distance in percentage.

    * @param percentage: Value between `0` and `1`. Represents the percentage.
    Default value is `0.0`.
     */
    fun setROITopOffset(topOffset: Float) {
        if (topOffset < 0.0f || topOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_ROI_TOP_OFFSET)
        }

        CaptureOptions.roi.topOffset = topOffset
    }

    /**
    Camera preview right distance in percentage.

    * @param percentage: Value between `0` and `1`. Represents the percentage.
    Default value is `0.0`.
     */
    fun setROIRightOffset(rightOffset: Float) {
        if (rightOffset < 0.0f || rightOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_ROI_RIGHT_OFFSET)
        }

        CaptureOptions.roi.rightOffset = rightOffset
    }

    /**
    Camera preview bottom distance in percentage.

    * @param percentage: Value between `0` and `1`. Represents the percentage.
    Default value is `0.0`.
     */
    fun setROIBottomOffset(bottomOffset: Float) {
        if (bottomOffset < 0.0f || bottomOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_ROI_BOTTOM_OFFSET)
        }

        CaptureOptions.roi.bottomOffset = bottomOffset
    }

    /**
    Camera preview left distance in percentage.

    * @param percentage: Value between `0` and `1`. Represents the percentage.
    Default value is `0.0`.
     */
    fun setROILeftOffset(leftOffset: Float) {
        if (leftOffset < 0.0f || leftOffset > 1.0f) {
            throw IllegalArgumentException(KeyError.INVALID_ROI_LEFT_OFFSET)
        }

        CaptureOptions.roi.leftOffset = leftOffset
    }

    /**
    Set to enable/disable region of interest offset visibility.

    * @param enable: The indicator to enable/disable region of interest visibility.
    Default value is `false`.
     */
    fun setROIAreaOffset(enable: Boolean) {
        CaptureOptions.roi.areaOffsetEnable = enable
    }

    /**
     * Set face region of interest area offset color.
     *
     * @param alpha Integer that represents the alpha.
     * @param red Integer that represent red color.
     * @param green Integer that represent green color.
     * @param blue Integer that represent blue color.
     * Default value is 100, 255, 255, 255 (white color).
     */
    fun setROIAreaOffsetColor(alpha: Int, red: Int, green: Int, blue: Int) {
        if (
            alpha < 0 || alpha > 255 ||
            red < 0 || red > 255 ||
            green < 0 || green > 255 ||
            blue < 0 || blue > 255
        ) {
            throw java.lang.IllegalArgumentException(KeyError.INVALID_ROI_COLOR)
        }

        CaptureOptions.roi.areaOffsetColor = Color.argb(alpha, red, green, blue)
    }

    /**
     * Enable/disable blur in face detection box.
     *
     * @param enable The indicator to enable/disable face detection box blur
     * Default value is `false`.
     */
    fun setBlurFaceDetectionBox(enable: Boolean) {
        CaptureOptions.blurFaceDetectionBox = enable
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

        CaptureOptions.colorEncoding = colorEncoding
    }

    /**
     * Enable/disable computer vision usage.
     *
     * @param enable The indicator to enable/disable computer vision usage.
     * Default value is `false`.
     */
    fun setComputerVision(enable: Boolean) {
        CaptureOptions.computerVision.enable = enable
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

        CaptureOptions.computerVision.paths = modelPaths
    }

    /**
     * Clear loaded computer vision models.
     */
    fun computerVisionClearModels() {
        CaptureOptions.computerVision.clear()
    }

    companion object {
        private const val TAG = "CameraView"
    }
}

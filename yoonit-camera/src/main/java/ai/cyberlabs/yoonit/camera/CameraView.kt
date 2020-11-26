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
import android.content.Context
import android.util.AttributeSet
import android.util.Size
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
     * Start capture type: none, face or barcode.
     * Must have started preview, see [startPreview].
     *
     * @param captureType The capture type: "none" | "face" | "barcode" | "frame".
     */
    fun startCaptureType(captureType: String) {
        when (captureType) {
            "none" -> this.cameraController.startCaptureType(CaptureType.NONE)

            "face" -> this.cameraController.startCaptureType(CaptureType.FACE)

            "barcode" -> this.cameraController.startCaptureType(CaptureType.QRCODE)

            "frame" -> this.cameraController.startCaptureType(CaptureType.FRAME)

            else -> {
                if (this.cameraEventListener != null) {
                    this.cameraEventListener!!.onError(KeyError.INVALID_CAPTURE_TYPE)
                }
            }
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
     * @return value 0 is front camera. value 1 is back camera.
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
     * Set number of face file images to create.
     * The time interval to create the image is 1000 milli second.
     * See [setFaceTimeBetweenImages] to change the time interval.
     *
     * @param faceNumberOfImages The number of images to create.
     */
    fun setFaceNumberOfImages(faceNumberOfImages: Int) {
        if (faceNumberOfImages < 0) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_NUMBER_OF_IMAGES)
            }
            return
        }

        this.captureOptions.faceNumberOfImages = faceNumberOfImages
    }

    /**
     * Set to show/hide face detection box when face detected.
     *
     * @param faceDetectionBox The indicator to show or hide the face detection box. Default value is true.
     */
    fun setFaceDetectionBox(faceDetectionBox: Boolean) {
        this.captureOptions.faceDetectionBox = faceDetectionBox
        this.cameraController.startCaptureType(this.captureOptions.type)
    }

    /**
     * Set to enable/disable face save images when capturing faces.
     *
     * @param faceSaveImages The indicator to enable or disable the face save images. Default value is false.
     */
    fun setFaceSaveImages(faceSaveImages: Boolean) {
        this.captureOptions.faceSaveImages = faceSaveImages
    }

    /**
     * Set saving face images time interval in milli seconds.
     *
     * @param faceTimeBetweenImages The time in milli seconds. Default value is 1000.
     */
    fun setFaceTimeBetweenImages(faceTimeBetweenImages: Long) {
        if (faceTimeBetweenImages < 0) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_TIME_BETWEEN_IMAGES)
            }
            return
        }

        this.captureOptions.faceTimeBetweenImages = faceTimeBetweenImages
    }

    /**
     * Enlarge the face bounding box by percent.
     *
     * @param facePaddingPercent The percent to enlarge the bounding box. Default value is 0.0.
     */
    fun setFacePaddingPercent(facePaddingPercent: Float) {
        if (facePaddingPercent < 0.0f) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_PADDING_PERCENT)
            }
            return
        }

        this.captureOptions.facePaddingPercent = facePaddingPercent
    }

    /**
     * Set face image width and height to be saved.
     *
     * @param width The file image width in pixels. Default value is 200.
     * @param height The file image height in pixels. Default value is 200.
     */
    fun setFaceImageSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_IMAGE_SIZE)
            }
            return
        }

        this.captureOptions.faceImageSize = Size(width, height)
    }

    /**
     * Limit the minimum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     *
     * For example, if set 0.5, will capture face with the detection box width occupying
     * at least 50% of the screen width.
     *
     * @param faceCaptureMinSize The face capture min size value. Default value is 0.0f.
     */
    fun setFaceCaptureMinSize(faceCaptureMinSize: Float) {
        if (faceCaptureMinSize < 0.0f || faceCaptureMinSize > 1.0f) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_CAPTURE_MIN_SIZE)
            }
            return
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
     * @param faceCaptureMaxSize The face capture max size value. Default value is 1.0f.
     */
    fun setFaceCaptureMaxSize(faceCaptureMaxSize: Float) {
        if (faceCaptureMaxSize < 0.0f || faceCaptureMaxSize > 1.0f) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_CAPTURE_MAX_SIZE)
            }
            return
        }

        this.captureOptions.faceCaptureMaxSize = faceCaptureMaxSize
    }

    /**
     * Set number of frame file images to create.
     * The time interval to create the image is 1000 milli second.
     * See [setFrameTimeBetweenImages] to change the time interval.
     *
     * @param faceNumberOfImages The number of images to create.
     */
    fun setFrameNumberOfImages(frameNumberOfImages: Int) {
        if (frameNumberOfImages < 0) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FRAME_NUMBER_OF_IMAGES)
            }
            return
        }

        this.captureOptions.frameNumberOfImages = frameNumberOfImages
    }

    /**
     * Set saving frame images time interval in milli seconds.
     *
     * @param frameTimeBetweenImages The time in milli seconds. Default value is 1000.
     */
    fun setFrameTimeBetweenImages(frameTimeBetweenImages: Long) {
        if (frameTimeBetweenImages < 0) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FRAME_TIME_BETWEEN_IMAGES)
            }
            return
        }

        this.captureOptions.frameTimeBetweenImages = frameTimeBetweenImages
    }

    /**
     * Set to apply enable/disable face region of interest.
     *
     * @param faceROIEnable The indicator to enable/disable face region of interest. Default value is `false`.
     */
    fun setFaceROIEnable(faceROIEnable: Boolean) {
        this.captureOptions.faceROI.enable = faceROIEnable
    }

    /**
     * Set face region of interest offset.
     *
     * @param topOffset Represents in percentage [0, 1]. Default value is `0`.
     * @param rightOffset Represents in percentage [0, 1]. Default value is `0`.
     * @param bottomOffset Represents in percentage [0, 1]. Default value is `0`.
     * @param leftOffset Represents in percentage [0, 1]. Default value is `0`.
     */
    fun setFaceROIOffset(
        topOffset: Float,
        rightOffset: Float,
        bottomOffset: Float,
        leftOffset: Float
    ) {

        val isInvalid =
            topOffset < 0.0 || topOffset > 1.0 ||
                rightOffset < 0.0 || rightOffset > 1.0 ||
                bottomOffset < 0.0 || bottomOffset > 1.0 ||
                leftOffset < 0.0 || leftOffset > 1.0

        if (isInvalid) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_ROI_OFFSET)
            }
            return
        }

        this.captureOptions.faceROI.topOffset = topOffset
        this.captureOptions.faceROI.rightOffset = rightOffset
        this.captureOptions.faceROI.bottomOffset = bottomOffset
        this.captureOptions.faceROI.leftOffset = leftOffset
    }

    /**
     * Set face minimum size in relation of the region of interest.
     *
     * @param minimumSize: Represents in percentage [0, 1]. Default value is `0`.
     */
    fun setFaceROIMinSize(minimumSize: Float) {
        if (minimumSize < 0.0 || minimumSize > 1.0) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onError(KeyError.INVALID_FACE_ROI_MIN_SIZE)
            }
            return
        }

        this.captureOptions.faceROI.minimumSize = minimumSize
    }

    companion object {
        private const val TAG = "CameraView"
    }
}

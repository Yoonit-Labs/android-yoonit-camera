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
import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.cameraview_layout.view.*

/**
 * This class represents the camera layout and your functions.
 */
class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    // Custom model to set CameraView features options.
    private var captureOptions: CaptureOptions = CaptureOptions()

    // Camera interface event listeners object.
    private var cameraEventListener: CameraEventListener? = null

    // Camera controller object.
    private var cameraController: CameraController

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
     * @param captureType The capture type: "none" | "face" | "barcode".
     */
    fun startCaptureType(captureType: String) {

        when (captureType) {
            "none" -> {
                this.captureOptions.type = CaptureType.NONE
                this.cameraController.startCaptureType(CaptureType.NONE)
            }
            "face" -> {
                this.captureOptions.type = CaptureType.FACE
                this.cameraController.startCaptureType(CaptureType.FACE)
            }
            "barcode" -> {
                this.captureOptions.type = CaptureType.QRCODE
                this.cameraController.startCaptureType(CaptureType.QRCODE)
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
     * @return: value 0 is front camera; value 1 is back camera.
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
     * Set number of face file images to create;
     * The time interval to create the image is 1000 milli second.
     * See [setFaceTimeBetweenImages] to change the time interval.
     *
     * @param faceNumberOfImages The number of images to create;
     */
    fun setFaceNumberOfImages(faceNumberOfImages: Int) {
        this.captureOptions.faceNumberOfImages = faceNumberOfImages
    }

    /**
     * Set to show/hide face detection box when face detected.
     *
     * @param faceDetectionBox The indicator to show or hide the face detection box. Default value is true;
     */
    fun setFaceDetectionBox(faceDetectionBox: Boolean) {
        this.cameraController.showDetectionBox = faceDetectionBox
        this.cameraController.startCaptureType(this.captureOptions.type)
    }

    /**
     * Set saving face images time interval in milli seconds.
     *
     * @param faceTimeBetweenImages The time in milli seconds. Default value is 1000;
     */
    fun setFaceTimeBetweenImages(faceTimeBetweenImages: Long) {
        this.captureOptions.faceTimeBetweenImages = faceTimeBetweenImages
    }

    /**
     * Enlarge the face bounding box by percent.
     *
     * @param facePaddingPercent The percent to enlarge the bounding box. Default value is 0.0;
     */
    fun setFacePaddingPercent(facePaddingPercent: Float) {
        this.captureOptions.facePaddingPercent = facePaddingPercent
    }

    /**
     * Set face image width and height to be saved.
     *
     * @param width The file image width in pixels. Default value is 200;
     * @param height The file image height in pixels. Default value is 200;
     */
    fun setFaceImageSize(width: Int, height: Int) {
        this.captureOptions.faceImageSize = Size(width, height)
    }

    companion object {
        private const val TAG = "CameraView"
    }
}

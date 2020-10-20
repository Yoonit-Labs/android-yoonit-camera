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

    private var captureType: String = "none"

    /**
     * Inflate CameraView layout and instantiate [CameraController].
     */
    init {
        LayoutInflater.from(context).inflate(
            R.layout.cameraview_layout,
            this,
            true
        )

        this.cameraController = CameraController(context, previewView, graphicView, this.captureOptions)
    }

    /**
     * Start camera preview if has permission.
     */
    fun startPreview() {
        this.cameraController.startPreview()
    }

    /**
     * Set different types os captures (none, face, barcode).
     */
    fun startCaptureType(captureType: String) {
        this.captureType = captureType
        when (captureType) {
            "none" -> this.cameraController.startCaptureType(CaptureType.NONE)
            "face" -> this.cameraController.startCaptureType(CaptureType.FACE)
            "barcode" -> this.cameraController.startCaptureType(CaptureType.QRCODE)
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
     * Return Integer that represents lens face state (0 for Front Camera, 1 for Back Camera).
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
     * Set number of images to save when detected.
     */
    fun setFaceNumberOfImages(faceNumberOfImages: Int) {
        this.captureOptions.faceNumberOfImages = faceNumberOfImages
    }

    /**
     * Set to show face detection box when face detected.
     */
    fun setFaceDetectionBox(faceDetectionBox: Boolean) {
        this.cameraController.showDetectionBox = faceDetectionBox
        this.startCaptureType(this.captureType)
    }

    /**
     * Set saving face images time interval in milli seconds.
     */
    fun setFaceTimeBetweenImages(faceTimeBetweenImages: Long) {
        this.captureOptions.faceTimeBetweenImages = faceTimeBetweenImages
    }

    /**
     * Set face image and bounding box padding in percent.
     */
    fun setFacePaddingPercent(facePaddingPercent: Float) {
        this.captureOptions.facePaddingPercent = facePaddingPercent
    }

    /**
     * Set face image size to be saved.
     */
    fun setFaceImageSize(faceImageSize: Int) {
        this.captureOptions.faceImageSize = faceImageSize
    }

    companion object {
        private const val TAG = "CameraView"
    }
}

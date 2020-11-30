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

package ai.cyberlabs.yoonit.camera.models

import ai.cyberlabs.yoonit.camera.CaptureType
import android.util.Size
import androidx.camera.core.CameraSelector

/**
 * Model to set [CameraView] capture features options.
 */
data class CaptureOptions(

    // Face region of interesting. Default is all the screen area.
    var faceROI: FaceROI = FaceROI(),

    // Camera image capture type: NONE, FACE, BARCODE and FRAME.
    var type: CaptureType = CaptureType.NONE,

    // Camera lens facing: CameraSelector.LENS_FACING_FRONT and CameraSelector.LENS_FACING_BACK.
    var cameraLens: Int = CameraSelector.LENS_FACING_FRONT,

    // Draw or not the face detection box.
    var faceDetectionBox: Boolean = true,

    // Face save cropped images.
    var faceSaveImages: Boolean = false,

    // Face capture number of images. 0 capture unlimited.
    var faceNumberOfImages: Int = 0,

    // Face capture time between images in milliseconds.
    var faceTimeBetweenImages: Long = 1000,

    // Face capture padding percent.
    var facePaddingPercent: Float = 0f,

    // Face capture image size to save.
    var faceImageSize: Size = Size(200, 200),

    /**
     * Limit the minimum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     */
    var faceCaptureMinSize: Float = 0.0f,

    /**
     * Limit the maximum face capture size.
     * This variable is the face detection box percentage in relation with the UI graphic view.
     * The value must be between 0 and 1.
     */
    var faceCaptureMaxSize: Float = 1.0f,

    // Frame capture number of images. 0 capture unlimited.
    var frameNumberOfImages: Int = 0,

    // Frame capture time between images in milliseconds.
    var frameTimeBetweenImages: Long = 1000,

    var isScreenFlipped: Boolean = false
)

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
import androidx.camera.core.CameraSelector

/**
 * Model to set [CameraView] capture features options.
 */
data class CaptureOptions(

    // Face region of interesting. Default is all the screen area.
    var faceROI: FaceROI = FaceROI(),

    // Camera image capture type: NONE, FACE, QRCODE and FRAME.
    var type: CaptureType = CaptureType.NONE,

    // Camera lens facing: CameraSelector.LENS_FACING_FRONT and CameraSelector.LENS_FACING_BACK.
    var cameraLens: Int = CameraSelector.LENS_FACING_FRONT,

    // Face/Frame capture number of images. 0 capture unlimited.
    var numberOfImages: Int = 0,

    // Face/Frame capture time between images in milliseconds.
    var timeBetweenImages: Long = 1000,

    // Face save cropped images.
    var faceSaveImages: Boolean = false,

    // Face/Frame capture image width to create.
    var imageOutputWidth: Int = 200,

    // Face/Frame capture image height to create.
    var imageOutputHeight: Int = 200,

    // Draw the face detection box.
    var faceDetectionBox: Boolean = true,

    // Face capture padding percent.
    var facePaddingPercent: Float = 0f,

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

    var isScreenFlipped: Boolean = false
)

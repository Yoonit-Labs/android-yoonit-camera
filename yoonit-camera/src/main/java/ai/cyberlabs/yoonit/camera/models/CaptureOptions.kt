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

package ai.cyberlabs.yoonit.camera.models

import ai.cyberlabs.yoonit.camera.CaptureType
import android.graphics.Color
import androidx.camera.core.CameraSelector

/**
 * Model to set [CameraView] capture features options.
 */
object CaptureOptions {

    // Region of interesting.
    var ROI: ROI = ROI()

    // Computer vision models.
    var ComputerVision: ComputerVision = ComputerVision()

    // Camera image capture type: NONE, FACE, QRCODE and FRAME.
    var type: CaptureType = CaptureType.NONE

    // Camera lens facing: CameraSelector.LENS_FACING_FRONT and CameraSelector.LENS_FACING_BACK.
    var cameraLens: Int = CameraSelector.LENS_FACING_FRONT

    // Face/Frame capture number of images. 0 capture unlimited.
    var numberOfImages: Int = 0

    // Face/Frame capture time between images in milliseconds.
    var timeBetweenImages: Long = 1000

    // Face/Frame capture image width to create.
    var imageOutputWidth: Int = 200

    // Face/Frame capture image height to create.
    var imageOutputHeight: Int = 200

    // Face/Frame save images captured.
    var saveImageCaptured: Boolean = false

    // Draw or not the face/qrcode detection box.
    var detectionBox: Boolean = false

    /**
     * Face/qrcode minimum size to detect in percentage related with the camera preview.
     * This variable is the detection box percentage in relation with the UI graphic view.
     * The value must be between `0` and `1`.
     */
    var minimumSize: Float = 0.0f

    /**
     * Face/qrcode maximum size to detect in percentage related with the camera preview.
     * This variable is the detection box percentage in relation with the UI graphic view.
     * The value must be between `0` and `1`.
     */
    var maximumSize: Float = 1.0f

    var detectionTopSize: Float = 0.0f
    var detectionRightSize: Float = 0.0f
    var detectionBottomSize: Float = 0.0f
    var detectionLeftSize: Float = 0.0f

    // Detection box color.
    var detectionBoxColor: Int = Color.WHITE

    // Face contours.
    var faceContours: Boolean = false

    // Face contours color.
    var faceContoursColor: Int = Color.WHITE

    // Blur face detection box
    var blurFaceDetectionBox: Boolean = false

    // Color encoding of the saved image.
    var colorEncoding: String = "RGB"
}

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

import android.util.Size
import androidx.camera.core.CameraSelector

/**
 * Model to set [CameraView] features options.
 */
data class CaptureOptions(
    var type: CaptureType = CaptureType.NONE,

    var cameraLens: Int = CameraSelector.LENS_FACING_FRONT,

    var faceDetectionBox: Boolean = true,
    var faceNumberOfImages: Int = 0,
    var faceTimeBetweenImages: Long = 1000,
    var facePaddingPercent: Float = 0f,
    var faceImageSize: Size = Size(200, 200),

    var frameNumberOfImages: Int = 0,
    var frameTimeBetweenImages: Long = 1000
)

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

/**
 * Custom model to set [CameraView] features options.
 */
data class CaptureOptions(
    var type: CaptureType = CaptureType.NONE,

    var faceNumberOfImages: Int = 0,
    var faceTimeBetweenImages: Long = 1000,
    var facePaddingPercent: Float = 0f,
    var faceImageSize: Size = Size(200, 200)
)

/**
 *
 * CaptureOptions.kt
 * CaptureOptions
 *
 * Created by Victor Goulart on 26/08/2020.
 *
 */

package ai.cyberlabs.yoonit.camera

/**
 * Custom model to set [CameraView] features options.
 */
data class CaptureOptions(
    var faceNumberOfImages: Int = 0,
    var faceTimeBetweenImages: Long = 300,
    var facePaddingPercent: Float = 0f,
    var faceImageSize: Int = 200
)

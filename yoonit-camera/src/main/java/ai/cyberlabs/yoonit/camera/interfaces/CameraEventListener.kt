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

package ai.cyberlabs.yoonit.camera.interfaces

interface CameraEventListener {

    fun onImageCaptured(
        type: String,
        count: Int,
        total: Int,
        imagePath: String,
        inferences: ArrayList<Pair<String, FloatArray>>
    )

    fun onFaceDetected(x: Int, y: Int, width: Int, height: Int)

    fun onFaceUndetected()

    fun onEndCapture()

    fun onError(error: String)

    fun onMessage(message: String)

    fun onPermissionDenied()

    fun onQRCodeScanned(content: String)
}

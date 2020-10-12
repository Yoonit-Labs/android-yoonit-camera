/**
 *
 * CameraEventListener.kt
 * CameraEventListener
 *
 * Created by Victor Goulart on 01/09/2020.
 *
 */

package ai.cyberlabs.yoonit.camera.interfaces

interface CameraEventListener {

    fun onFaceImageCreated(count: Int, total: Int, imagePath: String)

    fun onFaceDetected(faceDetected: Boolean)

    fun onEndCapture()

    fun onError(error: String)

    fun onMessage(message: String)

    fun onPermissionDenied()

    fun onBarcodeScanned(content: String)
}

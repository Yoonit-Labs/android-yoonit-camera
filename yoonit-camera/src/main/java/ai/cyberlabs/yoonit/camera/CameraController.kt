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

import ai.cyberlabs.yoonit.camera.analyzers.qrcode.QRCodeAnalyzer
import ai.cyberlabs.yoonit.camera.analyzers.face.FaceAnalyzer
import ai.cyberlabs.yoonit.camera.analyzers.frame.FrameAnalyzer
import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * Class responsible to handle the camera operations.
 */
class CameraController(
    private val context: Context,
    private val previewView: PreviewView,
    private val graphicView: CameraGraphicView,
    private val captureOptions: CaptureOptions
) : CameraCallback {

    // Camera interface event listeners object.
    var cameraEventListener: CameraEventListener? = null

    // Image analyzer handle build, start and stop.
    private var imageAnalyzerController = ImageAnalyzerController(this.graphicView)

    // Preview and ProcessCameraProvider both used to resume camera setup after toggle lens.
    private lateinit var preview: Preview
    private var cameraProviderFuture = ProcessCameraProvider.getInstance(this.context)
    private var cameraProviderProcess: ProcessCameraProvider? = null


    // Called when number of images reached.
    override fun onStopAnalyzer() {
        this.stopAnalyzer()
    }

    init {
        this.imageAnalyzerController.build()
    }

    /**
     * Start camera preview if has permission.
     */
    fun startPreview() {

        // Emit permission denied if do not has permission.
        if (!this.isAllPermissionsGranted()) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener!!.onPermissionDenied()
            }
            return
        }

        this.cameraProviderFuture.addListener(
            Runnable {
                try {
                    this.cameraProviderProcess = cameraProviderFuture.get()
                    this.cameraProviderProcess?.unbindAll()

                    this.preview = Preview
                        .Builder()
                        .build()

                    this.buildCameraPreview()
                } catch (e: Exception) {
                    if (this.cameraEventListener != null) {
                        this.cameraEventListener!!.onError(e.toString())
                    }
                }
            },
            ContextCompat.getMainExecutor(this.context)
        )
    }

    /**
     * Stop camera image analyzer and clear drawings.
     */
    fun stopAnalyzer() {
        this.captureOptions.type = CaptureType.NONE

        this.imageAnalyzerController.stop()
    }

    /**
     * Start image analyzer based on the capture type.
     */
    fun startCaptureType() {
        // If camera preview already is running, re-build camera preview.
        if (this.cameraProviderProcess != null) {
            this.imageAnalyzerController.stop()

            when(this.captureOptions.type) {

                CaptureType.FACE -> {
                    this.imageAnalyzerController.start(
                        FaceAnalyzer(
                            this.context,
                            this.cameraEventListener,
                            this.graphicView,
                            this.captureOptions,
                            this as CameraCallback
                        )
                    )
                }

                CaptureType.QRCODE -> this.imageAnalyzerController.start(
                    QRCodeAnalyzer(
                        this.cameraEventListener,
                        this.graphicView
                    )
                )

                CaptureType.FRAME -> this.imageAnalyzerController.start(
                    FrameAnalyzer(
                        this.context,
                        this.cameraEventListener,
                        this.captureOptions,
                        this.graphicView,
                        this as CameraCallback
                    )
                )
            }
        }
    }

    /**
     * Toggle between Front and Back Camera.
     */
    fun toggleCameraLens() {

        // Set camera lens.
        this.captureOptions.cameraLens =
            if (this.captureOptions.cameraLens == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK
            else
                CameraSelector.LENS_FACING_FRONT

        // If camera preview already is running, re-build camera preview.
        if (this.cameraProviderProcess != null) {
            this.buildCameraPreview()
        }
    }

    private fun buildCameraPreview() {
        this.cameraProviderProcess?.unbindAll()

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(this.captureOptions.cameraLens)
            .build()

        this.cameraProviderProcess?.bindToLifecycle(
            this.context as LifecycleOwner,
            cameraSelector,
            this.imageAnalyzerController.analysis,
            this.preview
        )

        this.preview.setSurfaceProvider(this.previewView.createSurfaceProvider())

        this.startCaptureType()
    }

    /**
     * Return Integer that represents lens face state (0 for Front Camera, 1 for Back Camera).
     */
    fun getCameraLens(): Int {
        return this.captureOptions.cameraLens
    }

    /**
     * Return if has permission or not to use the camera.
     */
    private fun isAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this.context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraController"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

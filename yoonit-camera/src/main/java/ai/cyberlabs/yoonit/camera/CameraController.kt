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

import ai.cyberlabs.yoonit.camera.interfaces.CameraCallback
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
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
    var cameraEventListener: CameraEventListener? = null
    var showDetectionBox: Boolean = true

    private var imageAnalyzerController = ImageAnalyzerController(this.graphicView)
    private lateinit var preview: Preview
    private var cameraProviderProcess: ProcessCameraProvider? = null
    private var captureType: CaptureType = CaptureType.NONE
    private var cameraLensFacing: Int = CameraSelector.LENS_FACING_FRONT

    override fun onStopAnalyzer() {
        this.stopAnalyzer()
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

        // Initialize camera.
        val cameraProviderFuture = ProcessCameraProvider
            .getInstance(this.context)

        this.imageAnalyzerController.build()

        cameraProviderFuture.addListener(
            Runnable {
                try {
                    this.cameraProviderProcess = cameraProviderFuture.get()

                    this.preview = Preview
                        .Builder()
                        .build()

                    val cameraSelector = CameraSelector
                        .Builder()
                        .requireLensFacing(this.cameraLensFacing)
                        .build()

                    this.cameraProviderProcess?.unbindAll()

                    this.cameraProviderProcess?.bindToLifecycle(
                        this.context as LifecycleOwner,
                        cameraSelector,
                        this.imageAnalyzerController.analysis,
                        this.preview
                    )

                    this.preview.setSurfaceProvider(this.previewView.createSurfaceProvider())
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
     * Stop camera face image analyzer and clear drawings.
     */
    fun stopAnalyzer() {
        this.captureType = CaptureType.NONE

        this.imageAnalyzerController.stop()
    }

    /**
     * Start capture type of Image Analyzer.
     * - Starts again if already has a capture process running.
     */
    fun startCaptureType(captureType: CaptureType) {
        this.captureType = captureType

        this.imageAnalyzerController.stop()
        this.buildCameraImageAnalyzer()
    }

    /**
     * Toggle between Front and Back Camera.
     */
    fun toggleCameraLens() {
        this.cameraLensFacing =
            if (this.cameraLensFacing == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK
            else
                CameraSelector.LENS_FACING_FRONT

        if (this.cameraProviderProcess != null) {
            val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(this.cameraLensFacing)
                .build()

            this.cameraProviderProcess?.unbindAll()

            this.cameraProviderProcess?.bindToLifecycle(
                this.context as LifecycleOwner,
                cameraSelector,
                this.imageAnalyzerController.analysis,
                this.preview
            )

            this.preview.setSurfaceProvider(this.previewView.createSurfaceProvider())

            this.startCaptureType(this.captureType)
        }
    }

    /**
     * Return Integer that represents lens face state (0 for Front Camera, 1 for Back Camera).
     */
    fun getCameraLens(): Int {
        return this.cameraLensFacing
    }

    /**
     * Start image analyzer based on the capture type.
     */
    private fun buildCameraImageAnalyzer() {

        when (this.captureType) {

            CaptureType.NONE -> this.imageAnalyzerController.stop()

            CaptureType.FACE -> this.imageAnalyzerController.start(FaceAnalyzer(
                this.context,
                this.cameraEventListener,
                this.graphicView,
                this.captureOptions,
                this.showDetectionBox,
                this as CameraCallback
            ))

            CaptureType.QRCODE -> this.imageAnalyzerController.start(BarcodeAnalyzer(
                this.cameraEventListener,
                this.graphicView))
        }
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

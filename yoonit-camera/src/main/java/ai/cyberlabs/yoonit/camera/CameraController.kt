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

import ai.cyberlabs.yoonit.camera.analyzers.barcode.BarcodeAnalyzer
import ai.cyberlabs.yoonit.camera.analyzers.face.FaceAnalyzer
import ai.cyberlabs.yoonit.camera.analyzers.frame.FrameAnalyzer
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

    // Camera interface event listeners object.
    var cameraEventListener: CameraEventListener? = null

    // Image analyzer handle build, start and stop.
    private var imageAnalyzerController = ImageAnalyzerController(this.graphicView)

    // Preview and ProcessCameraProvider both used to resume camera setup after toggle lens.
    private lateinit var preview: Preview
    private var cameraProviderProcess: ProcessCameraProvider? = null

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
                    this.cameraProviderProcess?.unbindAll()

                    this.preview = Preview
                        .Builder()
                        .build()

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
     * Start capture type of Image Analyzer.
     * Must have started preview.
     */
    fun startCaptureType(captureType: CaptureType) {

        // Must have started preview.
        if (this.cameraProviderProcess == null) {
            if (this.cameraEventListener != null) {
                this.cameraEventListener?.onError(KeyError.NOT_STARTED_PREVIEW)
            }
            return
        }

        this.captureOptions.type = captureType

        this.imageAnalyzerController.stop()
        this.buildCameraImageAnalyzer()
    }

    /**
     * Toggle between Front and Back Camera.
     */
    fun toggleCameraLens() {
        this.captureOptions.cameraLens =
            if (this.captureOptions.cameraLens == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK
            else
                CameraSelector.LENS_FACING_FRONT

        if (this.cameraProviderProcess != null) {
            val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(this.captureOptions.cameraLens)
                .build()

            this.cameraProviderProcess?.unbindAll()

            this.cameraProviderProcess?.bindToLifecycle(
                this.context as LifecycleOwner,
                cameraSelector,
                this.imageAnalyzerController.analysis,
                this.preview
            )

            this.preview.setSurfaceProvider(this.previewView.createSurfaceProvider())

            this.startCaptureType(this.captureOptions.type)
        }
    }

    /**
     * Return Integer that represents lens face state (0 for Front Camera, 1 for Back Camera).
     */
    fun getCameraLens(): Int {
        return this.captureOptions.cameraLens
    }

    /**
     * Start image analyzer based on the capture type.
     */
    private fun buildCameraImageAnalyzer() {

        when (this.captureOptions.type) {

            CaptureType.NONE -> this.imageAnalyzerController.stop()

            CaptureType.FACE -> this.imageAnalyzerController.start(
                FaceAnalyzer(
                    this.context,
                    this.cameraEventListener,
                    this.graphicView,
                    this.captureOptions,
                    this as CameraCallback
                )
            )

            CaptureType.QRCODE -> this.imageAnalyzerController.start(
                BarcodeAnalyzer(
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

package ai.cyberlabs.yoonit.camerademo

import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import ai.cyberlabs.yoonit.camera.CameraView
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.cameraView = camera_view
        this.cameraView.setCameraEventListener(startListener())

        this.stop_analyzer.setOnClickListener {  this.cameraView.stopCapture() }
        this.face_analyzer.setOnClickListener { this.cameraView.startCaptureType("face") }
        this.barcode_analyzer.setOnClickListener { this.cameraView.startCaptureType("qrcode") }
        this.change_lensfacing.setOnClickListener { this.cameraView.toggleCameraLens() }

        if (allPermissionsGranted()) {
            this.cameraView.startPreview()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this.baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (allPermissionsGranted()) {
                this.cameraView.startPreview()
            } else {
                Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startListener(): CameraEventListener = object : CameraEventListener {
        override fun onFaceImageCreated(count: Int, total: Int, imagePath: String) {
            val imageFile = File(imagePath)

            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                Log.d(TAG, "onFaceImageCreated: $count/$total - (w: ${imageBitmap.width}, h: ${imageBitmap.height})")
                image_preview.setImageBitmap(imageBitmap)
            }
        }

        override fun onFaceDetected(faceDetected: Boolean) {
            Log.d(TAG, "onFaceDetected $faceDetected")
        }

        override fun onEndCapture() {
            Log.d(TAG, "onEndCapture")
        }

        override fun onError(error: String) {
            Log.d(TAG, "onError: $error")
        }

        override fun onMessage(message: String) {
            Log.d(TAG, "onMessage: $message")
        }

        override fun onPermissionDenied() {
            Log.d(TAG, "onPermissionDenied")
        }

        override fun onBarcodeScanned(content: String) {
            makeText(content)
            Log.d(TAG, "onBarcodeScanned")
        }
    }

    private fun makeText(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "YoonitCameraDemo"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
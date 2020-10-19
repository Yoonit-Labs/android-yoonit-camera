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

package ai.cyberlabs.yoonit.camerademo

import ai.cyberlabs.yoonit.camera.CameraView
import ai.cyberlabs.yoonit.camera.interfaces.CameraEventListener
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Switch
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
        this.cameraView.setCameraEventListener(this.buildCameraEventListener())
    }

    override fun onResume() {
        super.onResume()

        if (this.allPermissionsGranted()) {
            this.cameraView.setFaceNumberOfImages(10)
            this.cameraView.startPreview()
            return
        }

        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PackageManager.PERMISSION_GRANTED
        )
    }

    fun onFaceBoxSwitchClick(view: View) {
        if (view is Switch) {
            val checked = view.isChecked
            this.cameraView.setFaceDetectionBox(checked)
        }
    }

    fun onCameraLensRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.getId()) {
                R.id.front_radio_button ->
                    if (checked && this.cameraView.getCameraLens() == 1) {
                        this.cameraView.toggleCameraLens()
                    }
                R.id.back_radio_button ->
                    if (checked && this.cameraView.getCameraLens() == 0) {
                        this.cameraView.toggleCameraLens()
                    }
            }
        }
    }

    fun onStopCaptureClick(view: View) {
        this.cameraView.stopCapture()
        this.image_preview.visibility = View.INVISIBLE
        this.qrcode_textview.visibility = View.INVISIBLE
    }

    fun onCaptureTypeRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.getId()) {
                R.id.none_radio_button ->
                    if (checked) {
                        this.cameraView.startCaptureType("none")
                        this.image_preview.visibility = View.INVISIBLE
                        this.info_textview.visibility = View.INVISIBLE
                        this.face_number_edittext.visibility = View.INVISIBLE
                        this.face_numer_textview.visibility = View.INVISIBLE

                        this.qrcode_textview.visibility = View.INVISIBLE
                    }
                R.id.face_radio_button ->
                    if (checked) {
                        this.cameraView.setFaceNumberOfImages(Integer.valueOf(face_number_edittext.text.toString()))
                        this.cameraView.startCaptureType("face")
                        this.image_preview.visibility = View.VISIBLE
                        this.info_textview.visibility = View.VISIBLE
                        this.face_number_edittext.visibility = View.VISIBLE
                        this.face_numer_textview.visibility = View.VISIBLE

                        this.qrcode_textview.visibility = View.INVISIBLE
                    }
                R.id.qrcode_radio_button ->
                    if (checked) {
                        this.cameraView.startCaptureType("barcode")
                        this.image_preview.visibility = View.INVISIBLE
                        this.info_textview.visibility = View.INVISIBLE
                        this.face_number_edittext.visibility = View.INVISIBLE
                        this.face_numer_textview.visibility = View.INVISIBLE

                        this.qrcode_textview.visibility = View.VISIBLE
                    }
            }
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

    private fun buildCameraEventListener(): CameraEventListener = object : CameraEventListener {

        override fun onFaceImageCreated(count: Int, total: Int, imagePath: String) {
            val imageFile = File(imagePath)

            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

                Log.d(TAG, "onFaceImageCreated: $count/$total - (w: ${imageBitmap.width}, h: ${imageBitmap.height})")

                image_preview.setImageBitmap(imageBitmap)
                image_preview.visibility = View.VISIBLE
                info_textview.text = "$count/$total"
            }
        }

        override fun onFaceDetected(x: Int, y: Int, width: Int, height: Int) {
            Log.d(TAG, "onFaceDetected $x, $y, $width, $height.")
        }

        override fun onFaceUndetected() {
            Log.d(TAG, "onFaceUndetected")
            image_preview.visibility = View.INVISIBLE
        }

        override fun onEndCapture() {
            Log.d(TAG, "onEndCapture")
            makeText("On end capture!")
            image_preview.visibility = View.INVISIBLE
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
            qrcode_textview.text = content
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
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var cameraView: CameraView

    private var captureType: String = ""
        set(value) {
            field = value

            when (field) {
                "none" -> {
                    this.cameraView.startCaptureType("none")
                    this.image_preview.visibility = View.INVISIBLE
                    this.info_textview.visibility = View.INVISIBLE
                    this.face_number_edittext.visibility = View.INVISIBLE
                    this.face_numer_textview.visibility = View.INVISIBLE

                    this.qrcode_textview.visibility = View.INVISIBLE
                }

                "face" -> {
                    this.cameraView.setNumberOfImages(Integer.valueOf(face_number_edittext.text.toString()))
                    this.cameraView.startCaptureType("face")
                    this.image_preview.visibility = View.VISIBLE
                    this.info_textview.visibility = View.VISIBLE
                    this.face_number_edittext.visibility = View.VISIBLE
                    this.face_numer_textview.visibility = View.VISIBLE

                    this.qrcode_textview.visibility = View.INVISIBLE
                }

                "frame" -> {
                    this.cameraView.setNumberOfImages(Integer.valueOf(face_number_edittext.text.toString()))
                    this.cameraView.startCaptureType("frame")
                    this.image_preview.visibility = View.VISIBLE
                    this.info_textview.visibility = View.VISIBLE
                    this.face_number_edittext.visibility = View.VISIBLE
                    this.face_numer_textview.visibility = View.VISIBLE

                    this.qrcode_textview.visibility = View.INVISIBLE
                }

                "qrcode" -> {
                    this.cameraView.startCaptureType("qrcode")
                    this.image_preview.visibility = View.INVISIBLE
                    this.info_textview.visibility = View.INVISIBLE
                    this.face_number_edittext.visibility = View.INVISIBLE
                    this.face_numer_textview.visibility = View.INVISIBLE

                    this.qrcode_textview.visibility = View.VISIBLE
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.cameraView = camera_view
        this.cameraView.setCameraEventListener(this.buildCameraEventListener())

        if (this.allPermissionsGranted()) {
            this.cameraView.startPreview()
            return
        }

        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PackageManager.PERMISSION_GRANTED
        )
    }


    fun onFlipScreenSwitchClick(view: View) {
        if (view is SwitchCompat) {
            this.cameraView.flipScreen()
        }
    }

    fun onFaceBoxSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setFaceDetectionBox(checked)
        }
    }

    fun onImageSaveSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setSaveImageCaptured(checked)

            if (checked) {
                this.image_preview.visibility = View.VISIBLE
            } else {
                this.image_preview.visibility = View.INVISIBLE
            }
        }
    }

    fun onBlurFaceSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setBlurFaceDetectionBox(checked)
        }
    }

    fun onCameraLensRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            if (!view.isChecked) {
                return
            }

            when (view.getId()) {
                R.id.back_radio_button -> {
                    camera_view.setCameraLens("back")
                    Log.d(TAG, "camera lens: ${camera_view.getCameraLens()}")
                }

                R.id.front_radio_button -> {
                    camera_view.setCameraLens("front")
                    Log.d(TAG, "camera lens: ${camera_view.getCameraLens()}")
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
            if (!checked) {
                return
            }

            when (view.getId()) {
                R.id.none_radio_button -> this.captureType = "none"

                R.id.face_radio_button -> this.captureType = "face"

                R.id.frame_radio_button -> this.captureType = "frame"

                R.id.qrcode_radio_button -> this.captureType = "qrcode"
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

        override fun onImageCaptured(type: String, count: Int, total: Int, imagePath: String) {
            val imageFile = File(imagePath)

            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

                Log.d(TAG, "onImageCaptured: $count/$total - (w: ${imageBitmap.width}, h: ${imageBitmap.height})")

                image_preview.setImageBitmap(imageBitmap)
                info_textview.text = "$count/$total"
                image_preview.visibility = View.VISIBLE
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

        override fun onQRCodeScanned(content: String) {
            qrcode_textview.text = content
            Log.d(TAG, "onQRCodeScanned")
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
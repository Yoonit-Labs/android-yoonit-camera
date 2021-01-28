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
import java.io.FileOutputStream

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
                    this.qrcode_textview.visibility = View.INVISIBLE
                }

                "face" -> {

                    this.cameraView.startCaptureType("face")
                    this.image_preview.visibility = View.VISIBLE
                    this.info_textview.visibility = View.VISIBLE
                    this.qrcode_textview.visibility = View.INVISIBLE
                }

                "frame" -> {

                    this.cameraView.startCaptureType("frame")
                    this.image_preview.visibility = View.VISIBLE
                    this.info_textview.visibility = View.VISIBLE
                    this.qrcode_textview.visibility = View.INVISIBLE
                }

                "qrcode" -> {
                    this.cameraView.startCaptureType("qrcode")
                    this.image_preview.visibility = View.INVISIBLE
                    this.info_textview.visibility = View.INVISIBLE
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
            this.cameraView.setFaceROILeftOffset(0.1f)
            this.cameraView.setFaceROIRightOffset(0.1f)
            this.cameraView.setFaceROITopOffset(0.1f)
            this.cameraView.setFaceROIBottomOffset(0.1f)
            this.cameraView.startPreview()

            this.cameraView.setComputerVisionLoadModels(arrayListOf(
                this.getModelPath("mask_custom_model.pt")
            ))

            return
        }

        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PackageManager.PERMISSION_GRANTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        this.cameraView.computerVisionClearModels()
    }

    private fun getModelPath(assetName: String): String {
        val file = File(this.filesDir, assetName)

        this.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }

    fun onPanelSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.features_panel.visibility = if (checked) View.VISIBLE else View.INVISIBLE
        }
    }

    fun onFaceBoxSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setFaceDetectionBox(checked)
        }
    }

    fun onFaceContoursSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setFaceContours(checked)
        }
    }

    fun onImageCaptureSwitchClick(view: View) {
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

    fun onFaceBlurSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setBlurFaceDetectionBox(checked)
        }
    }

    fun cameraStateSwitchClick(view: View) {
        if (view is SwitchCompat) {
            if (view.isChecked) {
                this.cameraView.startPreview()
                return
            }
            this.cameraView.destroy()
        }
    }

    fun onFaceMinSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val faceCaptureMinSize = if (view.isChecked) 0.7f else 0.0f
            this.cameraView.setFaceCaptureMinSize(faceCaptureMinSize)
        }
    }

    fun onFaceMaxSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val faceCaptureMaxSize = if (view.isChecked) 0.9f else 1.0f
            this.cameraView.setFaceCaptureMaxSize(faceCaptureMaxSize)
        }
    }

    fun onFaceROISwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setFaceROIEnable(checked)
            this.cameraView.setFaceROIAreaOffset(checked)
        }
    }

    fun onFaceROIMinSizeSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val faceROIMinimumSize = if (view.isChecked) 0.7f else 0.0f
            this.cameraView.setFaceROIMinSize(faceROIMinimumSize)
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

    fun onColorEncodingRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            if (!view.isChecked) {
                return
            }

            when (view.getId()) {
                R.id.rgb_radio_button -> {
                    camera_view.setColorEncodingCapture("RGB")
                    Log.d(TAG, "camera lens: ${camera_view.getCameraLens()}")
                }

                R.id.yuv_radio_button -> {
                    camera_view.setColorEncodingCapture("YUV")
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

    fun onComputerSwitchSwitchClick(view: View) {
        if (view is SwitchCompat) {
            val checked = view.isChecked
            this.cameraView.setComputerVision(checked)
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

        override fun onImageCaptured(
            type: String,
            count: Int,
            total: Int,
            imagePath: String,
            inferences: ArrayList<android.util.Pair<String, FloatArray>>
        ) {
            Log.d(TAG, "onImageCaptured . . . . . . . . . . . . . . . . . . . . . . . . .")

            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                Log.d(TAG, "$count/$total - (w: ${imageBitmap.width}, h: ${imageBitmap.height})")
                image_preview.setImageBitmap(imageBitmap)
            }

            inferences.forEach {
                var results = ""
                for (result in it.second) {
                    results += "$result "
                }
                Log.d(TAG, "${it.first}: $results")
            }
            Log.d(TAG, " . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .")

            info_textview.text = "$count/$total"
            image_preview.visibility = View.VISIBLE
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
/**
 * +-+-+-+-+-+-+
 * |y|o|o|n|i|t|
 * +-+-+-+-+-+-+
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Yoonit Camera lib for Android applications                      |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2020-2021        |
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
            this.cameraView.setROILeftOffset(0.1f)
            this.cameraView.setROIRightOffset(0.1f)
            this.cameraView.setROITopOffset(0.1f)
            this.cameraView.setROIBottomOffset(0.1f)
            this.captureType = "face"
            this.cameraView.setSaveImageCaptured(true)
            this.cameraView.setComputerVision(true)
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

    fun onConfigurationRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            when (view.getId()) {
                R.id.configuration_radio_button -> {
                    this.configurations.visibility = View.VISIBLE
                    this.analysis.visibility = View.GONE
                }
                R.id.analysis_radio_button -> {
                    this.configurations.visibility = View.GONE
                    this.analysis.visibility = View.VISIBLE
                }
                R.id.hide_radio_button -> {
                    this.configurations.visibility = View.GONE
                    this.analysis.visibility = View.GONE
                }
            }
        }
    }

    fun onDetectionBoxSwitchClick(view: View) {
        if (view is SwitchCompat) this.cameraView.setDetectionBox(view.isChecked)
    }

    fun onFaceContoursSwitchClick(view: View) {
        if (view is SwitchCompat) this.cameraView.setFaceContours(view.isChecked)
    }

    fun onImageCaptureSwitchClick(view: View) {
        if (view is SwitchCompat) {
            this.cameraView.setSaveImageCaptured(view.isChecked)
            if (view.isChecked) {
                this.image_preview.visibility = View.VISIBLE
            } else {
                this.image_preview.visibility = View.INVISIBLE
            }
        }
    }

    fun onFaceBlurSwitchClick(view: View) {
        if (view is SwitchCompat) this.cameraView.setBlurFaceDetectionBox(view.isChecked)
    }

    fun onCameraSwitchClick(view: View) {
        if (view is SwitchCompat) {
            if (view.isChecked) {
                this.cameraView.startPreview()
                return
            }
            this.cameraView.destroy()
        }
    }

    fun onTorchSwitchClick(view: View) {
        if (view is SwitchCompat) this.cameraView.setTorch(view.isChecked)
    }

    fun onMinSwitchClick(view: View) {
        if (view is SwitchCompat) this.cameraView.setDetectionMinSize(if (view.isChecked) 0.7f else 0.0f)
    }

    fun onMaxSwitchClick(view: View) {
        if (view is SwitchCompat) this.cameraView.setDetectionMaxSize(if (view.isChecked) 0.9f else 1.0f)
    }

    fun onROISwitchClick(view: View) {
        if (view is SwitchCompat) {
            this.cameraView.setROI(view.isChecked)
            this.cameraView.setROIAreaOffset(view.isChecked)
        }
    }

    fun onDetectionBoxColorSwitchClick(view: View) {
        if (view is SwitchCompat) {
            if (view.isChecked) {
                this.cameraView.setDetectionBoxColor(255, 255, 0, 0)
            } else {
                this.cameraView.setDetectionBoxColor(255, 255, 255, 255)
            }
        }
    }

    fun onFaceContoursColorSwitchClick(view: View) {
        if (view is SwitchCompat) {
            if (view.isChecked) {
                this.cameraView.setFaceContoursColor(255, 255, 0, 0)
            } else {
                this.cameraView.setFaceContoursColor(255, 255, 255, 255)
            }
        }
    }

    fun onROIColorSwitchClick(view: View) {
        if (view is SwitchCompat) {
            if (view.isChecked) {
                this.cameraView.setROIAreaOffsetColor(255, 255, 0, 0)
            } else {
                this.cameraView.setROIAreaOffsetColor(255, 255, 255, 255)
            }
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
                    turn_torch_state.visibility = View.VISIBLE
                    Log.d(TAG, "camera lens: ${camera_view.getCameraLens()}")
                }

                R.id.front_radio_button -> {
                    camera_view.setCameraLens("front")
                    turn_torch_state.isChecked = false
                    turn_torch_state.visibility = View.INVISIBLE
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

            if (inferences.isNotEmpty() && inferences.first().second.isNotEmpty()) {
                val probability = inferences.first().second.first()
                maskTextView.text = if (probability < 0.3) "Masked" else "Not Masked"
                maskProbabilityTextView.text = probability.toString()
            }

            Log.d(TAG, " . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .")

            info_textview.text = "$count/$total"
            image_preview.visibility = View.VISIBLE
        }

        override fun onFaceDetected(
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            leftEyeOpenProbability: Float?,
            rightEyeOpenProbability: Float?,
            smilingProbability: Float?,
            headEulerAngleX: Float,
            headEulerAngleY: Float,
            headEulerAngleZ: Float,
            quality: Triple<Double, Double, Double>
        ) {
            Log.d(
                TAG,
                "onFaceDetected: \n" +
                "x: $x, y: $y, w: $width, h: $height. \n" +
                "leftEye: $leftEyeOpenProbability \n" +
                "rightEye: $rightEyeOpenProbability \n" +
                "smilling: $smilingProbability \n" +
                "head X angle: $headEulerAngleX \n" +
                "head Y angle: $headEulerAngleY \n" +
                "head Z angle: $headEulerAngleZ"
            )

            leftEyeTextView.text = if (leftEyeOpenProbability != null) {
                leftEyeProbabilityTextView.text = leftEyeOpenProbability.toString()
                if (leftEyeOpenProbability > 0.8) "Open" else "Close"
            } else "-"
            rightEyeTextView.text = if (rightEyeOpenProbability != null) {
                rightEyeProbabilityTextView.text = rightEyeOpenProbability.toString()
                if (rightEyeOpenProbability > 0.8) "Open" else "Close"
            } else "-"
            smlingTextView.text = if (smilingProbability != null) {
                smilingProbabilityTextView.text = smilingProbability.toString()
                if (smilingProbability > 0.8) "Smiling" else "Not Smiling"
            } else "-"

            headVerticalAngleTextView.text =  headEulerAngleX.toString()
            headVerticalTextView.text =
                if (headEulerAngleX < -36) "Super Down"
                else if (-36 < headEulerAngleX && headEulerAngleX < -12) "Down"
                else if (-12 < headEulerAngleX && headEulerAngleX < 12) "Frontal"
                else if (12 < headEulerAngleX && headEulerAngleX < 36) "Up"
                else if (36 < headEulerAngleX) "Super Up"
                else "-"

            headHorizontalAngleTextView.text =  headEulerAngleY.toString()
            headHorizontalTextView.text =
                if (headEulerAngleY < -36) "Super Left"
                else if (-36 < headEulerAngleY && headEulerAngleY < -12) "Left"
                else if (-12 < headEulerAngleY && headEulerAngleY < 12) "Frontal"
                else if (12 < headEulerAngleY && headEulerAngleY < 36) "Right"
                else if (36 < headEulerAngleY) "Super Right"
                else "-"

            headTiltAngleTextView.text =  headEulerAngleZ.toString()
            headTiltTextView.text =
                if (headEulerAngleZ < -36) "Super Right"
                else if (-36 < headEulerAngleZ && headEulerAngleZ < -12) "Right"
                else if (-12 < headEulerAngleZ && headEulerAngleZ < 12) "Frontal"
                else if (12 < headEulerAngleZ && headEulerAngleZ < 36) "Left"
                else if (36 < headEulerAngleZ) "Super Left"
                else "-"
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
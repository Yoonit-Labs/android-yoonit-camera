package ai.cyberlabs.yoonit.camera

import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import android.util.Size
import java.io.File

class ComputerVision {

    /**
     * Enable/disable computer vision usage.
     * Default value is `false`.
     */
    var enable: Boolean = CaptureOptions.computerVision.enable
        set(value) {
            CaptureOptions.computerVision.enable = value
            field = value
        }

    /**
     * Set the computer vision model paths to load.
     * Default value is an empty array.
     */
    var modelPaths: ArrayList<String> = CaptureOptions.computerVision.paths
        set(value) {
            value.forEach {
                modelPath ->
                if (!File(modelPath).exists()) {
                    throw IllegalArgumentException("${KeyError.INVALID_COMPUTER_VISION_MODEL_PATHS}: $modelPath")
                }
            }

            CaptureOptions.computerVision.paths = value
            field = value
        }

    var inputSize: Size = CaptureOptions.computerVision.inputSize
        set(value) {
            CaptureOptions.computerVision.inputSize = value
            field = value
        }

    /**
     * Clear loaded computer vision models.
     */
    fun clear() {
        CaptureOptions.computerVision.clear()
    }
}
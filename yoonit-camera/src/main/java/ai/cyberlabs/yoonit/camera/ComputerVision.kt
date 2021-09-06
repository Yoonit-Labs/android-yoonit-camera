package ai.cyberlabs.yoonit.camera

import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import java.io.File

class ComputerVision {
    /**
     * Enable/disable computer vision usage.
     *
     * @param enable The indicator to enable/disable computer vision usage.
     * Default value is `false`.
     */
    fun setComputerVision(enable: Boolean) {
        CaptureOptions.computerVision.enable = enable
    }
    var enable: Boolean = CaptureOptions.computerVision.enable
        set(value) {
            CaptureOptions.computerVision.enable = value
            field = value
        }

    /**
     * Set the computer vision model paths to load.
     *
     * @param modelPaths The computer vision absolute model file path array list.
     * Default value is an empty array.
     */
    fun setComputerVisionLoadModels(modelPaths: ArrayList<String>) {
        modelPaths.forEach {
                modelPath ->
            if (!File(modelPath).exists()) {
                throw IllegalArgumentException("${KeyError.INVALID_COMPUTER_VISION_MODEL_PATHS}: $modelPath")
            }
        }

        CaptureOptions.computerVision.paths = modelPaths
    }
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

    /**
     * Clear loaded computer vision models.
     */
    fun computerVisionClearModels() {
        CaptureOptions.computerVision.clear()
    }
    fun clear() {
        CaptureOptions.computerVision.clear()
    }
}
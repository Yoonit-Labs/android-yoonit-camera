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

    /**
     * Clear loaded computer vision models.
     */
    fun computerVisionClearModels() {
        CaptureOptions.computerVision.clear()
    }
}
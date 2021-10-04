/**
 * ██╗   ██╗ ██████╗  ██████╗ ███╗   ██╗██╗████████╗
 * ╚██╗ ██╔╝██╔═══██╗██╔═══██╗████╗  ██║██║╚══██╔══╝
 *  ╚████╔╝ ██║   ██║██║   ██║██╔██╗ ██║██║   ██║
 *   ╚██╔╝  ██║   ██║██║   ██║██║╚██╗██║██║   ██║
 *    ██║   ╚██████╔╝╚██████╔╝██║ ╚████║██║   ██║
 *    ╚═╝    ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═╝   ╚═╝
 *
 * https://yoonit.dev - about@yoonit.dev
 *
 * Yoonit Camera
 * The most advanced and modern Camera module for Android with a lot of awesome features
 *
 * Haroldo Teruya, Victor Goulart, Thúlio Noslen & Luigui Delyer @ 2020-2021
 */

package ai.cyberlabs.yoonit.camera

import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import android.util.Size
import java.io.File

class ComputerVision {

    /**
     * Enable/disable computer vision usage.
     * Default value is `false`.
     */
    var enable: Boolean = CaptureOptions.ComputerVision.enable
        set(value) {
            CaptureOptions.ComputerVision.enable = value
            field = value
        }

    /**
     * The computer vision model paths.
     * Default value is an empty array.
     */
    var modelPaths: ArrayList<String> = CaptureOptions.ComputerVision.paths
        set(value) {
            value.forEach {
                modelPath ->
                if (!File(modelPath).exists()) {
                    throw IllegalArgumentException("${KeyError.INVALID_COMPUTER_VISION_MODEL_PATHS}: $modelPath")
                }
            }

            CaptureOptions.ComputerVision.paths = value
            field = value
        }

    /**
     * Image input size to use the loaded model paths.
     * Default value is (0, 0).
     */
    var inputSize: Size = CaptureOptions.ComputerVision.inputSize
        set(value) {
            CaptureOptions.ComputerVision.inputSize = value
            field = value
        }

    /**
     * Clear computer vision path models.
     */
    fun clear() {
        CaptureOptions.ComputerVision.clear()
    }
}

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

package ai.cyberlabs.yoonit.camera.models

import org.pytorch.Module

/**
 * Model to structure the computer vision model paths by name.
 */
class ComputerVision {

    //  Enable or disable computer vision models usage when capture images.
    var enable: Boolean = false

    // Computer vision models map.
    // Key is the name of the file and the value is the model loaded.
    var modelMap: MutableMap<String, Module> = mutableMapOf()

    // Computer vision model file absolute paths.
    // When set, build the modelMap.
    var paths: ArrayList<String> = arrayListOf()
        set(value) {
            for (modelPath in value) {
                val name: String = modelPath.substringAfterLast('/')
                val model: Module = Module.load(modelPath)

                this.modelMap[name] = model
            }

            field = value
        }

    /**
     * Disable computer vision, clear the paths and modelMap.
     */
    fun clear() {
        this.enable = false
        this.paths.clear()
        this.modelMap.clear()
    }
}
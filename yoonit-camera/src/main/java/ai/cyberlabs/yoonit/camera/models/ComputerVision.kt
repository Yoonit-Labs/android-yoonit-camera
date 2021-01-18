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
 * Model to structure the computer vision module paths by name.
 */
class ComputerVision {

    //  Enable or disable computer vision modules usage when capture images.
    var enable: Boolean = false

    // Computer vision modules map.
    // Key is the name of the file and the value is the module loaded.
    var moduleMap: MutableMap<String, Module> = mutableMapOf()

    // Computer vision module file absolute paths.
    // When set, build the moduleMap.
    var paths: ArrayList<String> = arrayListOf()
        set(value) {
            for (modulePath in value) {
                val name: String = modulePath.substringAfterLast('/')
                val model: Module = Module.load(modulePath)

                this.moduleMap[name] = model
            }

            field = value
        }

    /**
     * Disable computer vision, clear the paths and moduleMap.
     */
    fun clear() {
        this.enable = false
        this.paths.clear()
        this.moduleMap.clear()
    }
}
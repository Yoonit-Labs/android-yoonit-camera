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
 * Haroldo Teruya & Victor Goulart @ 2020-2021
 */

package ai.cyberlabs.yoonit.camera.models

import android.util.Size
import org.pytorch.Module

/**
 * Model to structure the computer vision model paths by name.
 */
class ComputerVision {

    //  Enable or disable computer vision models usage when capture images.
    var enable: Boolean = false

    var inputSize: Size = Size(0, 0)

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

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
 * Model to structure the classifier paths by name.
 */
class Classifier {

    //  Enable or disable the use of the classifier.
    var enable: Boolean = false

    // Classifier map. Key is the name of the file and the value is the module loaded.
    var classifierMap: MutableMap<String, Module> = mutableMapOf()

    // Classifier file absolute paths. When set, build the classifierMap.
    var paths: ArrayList<String> = arrayListOf()
        set(value) {
            for (modelPath in value) {
                val name: String = modelPath.substringAfterLast('/')
                val model: Module = Module.load(modelPath)

                this.classifierMap[name] = model
            }

            field = value
        }

    /**
     * Clear the paths and classifierMap.
     */
    fun clear() {
        this.paths.clear()
        this.classifierMap.clear()
    }
}
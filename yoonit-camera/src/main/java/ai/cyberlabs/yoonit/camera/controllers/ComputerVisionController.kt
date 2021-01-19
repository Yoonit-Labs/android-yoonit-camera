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

package ai.cyberlabs.yoonit.camera.controllers

import android.graphics.*
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils

/**
 * Class responsible to manipulate the [ComputerVision] models.
 */
class ComputerVisionController {

    companion object {

        private fun getInference(
            module: Module,
            bitmap: Bitmap
        ): FloatArray? {

            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB)

            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()

            return outputTensor.dataAsFloatArray
        }

        fun getInferences(
            moduleMap: MutableMap<String, Module>,
            bitmap: Bitmap
        ): ArrayList<Pair<String, FloatArray>> {

            val inferences: ArrayList<Pair<String, FloatArray>> = arrayListOf()

            val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(
                bitmap,
                28,
                28,
                false
            )

            for (module in moduleMap) {
                val results: FloatArray? = this.getInference(module.value, scaledBitmap)

                val inference: Pair<String, FloatArray> = Pair(
                    module.key,
                    results ?: return arrayListOf()
                )

                inferences.add(inference)
            }

            return inferences
        }
    }
}
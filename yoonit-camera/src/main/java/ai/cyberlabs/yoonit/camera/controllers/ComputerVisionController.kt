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

package ai.cyberlabs.yoonit.camera.controllers

import ai.cyberlabs.yoonit.camera.models.CaptureOptions
import android.graphics.Bitmap
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
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()

            return outputTensor.dataAsFloatArray
        }

        fun getInferences(
            moduleMap: MutableMap<String, Module>,
            bitmap: Bitmap
        ): ArrayList<android.util.Pair<String, FloatArray>> {

            val inferences: ArrayList<android.util.Pair<String, FloatArray>> = arrayListOf()

            val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(
                bitmap,
                CaptureOptions.ComputerVision.inputSize.width,
                CaptureOptions.ComputerVision.inputSize.height,
                false
            )

            for (module in moduleMap) {
                val results: FloatArray? = this.getInference(module.value, scaledBitmap)

                val inference: android.util.Pair<String, FloatArray> = android.util.Pair(
                    module.key,
                    results ?: return arrayListOf()
                )

                inferences.add(inference)
            }

            return inferences
        }
    }
}

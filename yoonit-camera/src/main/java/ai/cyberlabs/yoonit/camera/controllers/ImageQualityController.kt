package ai.cyberlabs.yoonit.camera.controllers

import ai.cyberlabs.yoonit.camera.utils.coerce
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class ImageQualityController {
    companion object {
        // ellipsoidal mask parameters
        private const val fracEllipseCenterX: Double = 0.50
        private const val fracEllipseCenterY: Double = 0.50
        private const val fracEllipseRadiusX: Double = 0.35
        private const val fracEllipseRadiusY: Double = 0.50

        // kernel for the convolution (3x3 laplacian of gaussian)
        private val kernel: IntArray = intArrayOf(
            1,  1,  1,
            1, -8,  1,
            1,  1,  1
        )

        fun processImage(scaledFaceBitmap: Bitmap, withMask: Boolean) : Triple<Double, Double, Double> {

            val pixels = convertToGrayscale(scaledFaceBitmap)

            var mask: Ellipse? = null

            if (withMask) {
                mask = Ellipse(
                    (scaledFaceBitmap.width * fracEllipseCenterX).toInt(),
                    (scaledFaceBitmap.height * fracEllipseCenterY).toInt(),
                    (scaledFaceBitmap.width * fracEllipseRadiusX).toInt(),
                    (scaledFaceBitmap.height * fracEllipseRadiusY).toInt()
                )
            }

            val histPair = calcHistogramMetrics(scaledFaceBitmap, pixels, mask)
            val dark = histPair.first
            val light = histPair.second

            val sharpness = calcConvolutionMetrics(scaledFaceBitmap, pixels)

            return Triple(dark, light, sharpness)
        }

        private fun convertToGrayscale(bitmap: Bitmap) : IntArray {

            // create flat array with grayscale image
            val pixelsRGB = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixelsRGB, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return pixelsRGB.map { pixel ->
                (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel))
                    .toInt().coerceIn(0, 255)
            }.toIntArray()
        }

        private fun calcHistogramMetrics(bitmap: Bitmap, pixels: IntArray, mask: Ellipse?) : Pair<Double, Double> {

            // calculate histogram of pixels inside bit mask
            val hist = IntArray(256) {0}
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    if ((mask != null && mask.contains(x, y))
                        || mask == null) {
                        val pixel = pixels[y * bitmap.width + x]
                        hist[pixel] += 1
                    }
                }
            }

            // calculate percentage of bright and dark pixels based on histogram "tails"
            // one measure of image quality (or image balance) is to quantify how many pixels
            // lie in the tails of the histogram, indicating the image is unbalanced
            val darkTail = hist.slice(IntRange(0, 63)).sum().toDouble()
            val dark = darkTail / hist.sum().toDouble()
            val lightTail = hist.slice(IntRange(192, 255)).sum().toDouble()
            val light = lightTail / hist.sum().toDouble()

            return Pair(dark, light)
        }

        private fun calcConvolutionMetrics(bitmap: Bitmap, pixels: IntArray) : Double {

            // determine edges (high frequency signals) via convolution with 3x3 LoG kernel
            // conv is the resulting flattened image, the same size as the original
            val conv = IntArray(bitmap.width * bitmap.height) {0}

            // we iterate on every pixel of the image...
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {

                    // ...and on every coefficient of the 3x3 kernel...
                    var convPixel = 0
                    for (j in -1 until 2) {
                        for (i in -1 until 2) {

                            // ...and we compute the dot product (the sum of an element-wise multiplication)
                            // of the kernel (sliding window) with the current region of the image it is
                            // passing through, and store the result on the corresponding pixel of the convoluted image

                            // if the image pixel required is "outside" the image, the border pixels will be
                            // replicated. otherwise, the sum of indices will point to a valid pixel
                            val pixelY = (y + j).coerceIn(0, bitmap.height - 1)
                            val pixelX = (x + i).coerceIn(0, bitmap.width - 1)
                            val pixelIndex = pixelY * bitmap.width + pixelX
                            val kernelIndex = (j + 1) * 3 + (i + 1)

                            // then, one of the products is computed and accumulated
                            convPixel += (pixels[pixelIndex] * kernel[kernelIndex])
                        }
                    }

                    // finally, the sum of the products is stored as a pixel
                    conv[y * bitmap.width + x] = convPixel.coerceIn(0, 255)
                }
            }

            // compute the standard deviation of the pixels. it results in a measure of the amount
            // of high frequency signals on the image
            val mean = conv.average()
            val accVar = conv.fold(0.0, { acc, pixel -> acc + (pixel - mean).pow(2) })

            return sqrt(accVar / conv.size) / 128
        }
    }



    private class Ellipse(val centerX: Int, val centerY: Int, val radiusX: Int, val radiusY: Int) {
        fun contains(x0: Int, y0: Int) : Boolean {
            // the ellipse equation is
            //
            // (x - cx) ^ 2   (y - cy) ^ 2
            // ------------ + ------------ = 1
            //   rx ^ 2         ry ^ 2
            //
            // if an (x0, y0) point inserted in the equation gives < 1,
            // then the point (x0, y0) is inside the ellipse
            return (((x0 - centerX).toDouble()/radiusX).pow(2) +
                    ((y0 - centerY).toDouble()/radiusY).pow(2)) < 1.0
        }
    }
}
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
        fun faceQuality(bitmap: Bitmap, boundingBox: Rect = Rect()) : Triple<Double, Double, Double> {

            // upper limit for dimensions
            val maxWidth = 200
            val maxHeight = 200

            // crop the face
            val faceBitmap: Bitmap = if (boundingBox.isEmpty) {
                Bitmap.createBitmap(bitmap)
            } else {
                boundingBox.coerce(bitmap.width, bitmap.height)
                Bitmap.createBitmap(bitmap, boundingBox.left, boundingBox.right,
                        boundingBox.width(), boundingBox.height())
            }

            // scale the dimensions to at most maxWidth x maxHeight
            val scaledFaceBitmap = Bitmap.createScaledBitmap(
                    faceBitmap,
                    faceBitmap.width.coerceIn(0, maxWidth),
                    faceBitmap.height.coerceIn(0, maxHeight),
                    true
            )

            // define parameters and ellipsoidal mask
            val width = scaledFaceBitmap.width
            val height = scaledFaceBitmap.height
            val ellipseCenterX = width / 2
            val ellipseCenterY = height / 2
            val ellipseRadiusX = width * 35 / 100
            val ellipseRadiusY = height * 50 / 100
            val mask = Ellipse(ellipseCenterX, ellipseCenterY, ellipseRadiusX, ellipseRadiusY)

            // laplacian of gaussian (LoG) kernel
            val kernel = arrayOf(
                    arrayOf(1,  1,  1),
                    arrayOf(1, -8,  1),
                    arrayOf(1,  1,  1)
            )

            // create flat array with grayscale image
            val pixels = IntArray(width * height)
            scaledFaceBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            for (i in pixels.indices)
                pixels[i] = min(max((0.299 * Color.red(pixels[i])
                        + 0.587 * Color.green(pixels[i])
                        + 0.114 * Color.blue(pixels[i])).toInt(), 0), 255)

            // reflect inner pixels on border for convolution -> gfedcb|abcdefgh|gfedcba
            // val reflectedBitmap = Bitmap.createBitmap(width, height, scaledFaceBitmap.config)
            // reflectedBitmap.setPixels(pixels, 0, width, 1, 1, width+2, height+2)

            // calculate histogram of pixel values inside ellipsoidal mask
            val hist = IntArray(256) {0}
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (mask.contains(x, y)) {
                        val pixel = pixels[y * width + x]
                        hist[pixel] += 1
                    }
                }
            }

            // calculate percentage of bright and dark pixels based on histogram "tails"
            // one measure of image quality is if the image has more pixels in the intermediate
            // regions instead of the tails of the histogram
            val darkTail = hist.slice(IntRange(0, 63)).sum().toDouble()
            val dark = darkTail / hist.sum().toDouble()
            val lightTail = hist.slice(IntRange(192, 255)).sum().toDouble()
            val light = lightTail / hist.sum().toDouble()

            // determine edges (high frequency signals) via convolution with 3x3 LoG kernel
            // conv is the resulting flattened image, the same size as the original
            val conv = IntArray(width * height) {0}

            // we iterate on every pixel of the image...
            for (y in 0 until height) {
                for (x in 0 until width) {

                    // ...and on every coefficient of the 3x3 kernel...
                    for (j in -1 until 2) {
                        for (i in -1 until 2) {

                            // ...and we compute an element-wise multiplication
                            // of the kernel with the current region of the image
                            // it is passing through, and store the result on the
                            // corresponding pixel of the convoluted image

                            // if the needed pixel index is pointing to a pixel
                            // that is outside the image, inner pixels will be mirrored
                            // otherwise, the pair (x+i, y+j) is computed
                            val xi = when (x + i) {
                                -1 -> 1
                                width -> width - 2
                                else -> x + i
                            }
                            val yj = when (y + j) {
                                -1 -> 1
                                height -> height - 2
                                else -> y + j
                            }

                            // finally, the needed pixel is fetched and one of the
                            // element-wise products is computed and the result accumulated
                            conv[y * width + x] += kernel[i+1][j+1] * pixels[xi * width + yj]
                        }
                    }
                }
            }

            // compute the variance of the pixels. it results in
            // a measure of the high frequency signals on the image
            val mean = conv.average()
            val accVar = conv.fold(0.0, { accVariance, pixel ->
                accVariance + (pixel - mean).pow(2) })

            // get the standard deviation and normalize it
            val sharp = sqrt(accVar / (width * height)) / 128

            return Triple(dark, light, sharp)
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
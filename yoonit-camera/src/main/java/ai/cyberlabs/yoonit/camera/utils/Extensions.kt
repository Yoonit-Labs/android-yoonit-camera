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

package ai.cyberlabs.yoonit.camera.utils

import android.content.Context
import android.graphics.*
import android.media.Image
import java.io.ByteArrayOutputStream

/**
 * Convert pixel value to DPI.
 *
 * @param context application context to get device screen density.
 */
fun Float.pxToDPI(context: Context) = (this / context.resources.displayMetrics.density).toInt()

fun Bitmap.rotate(rotationDegrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees)

    return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            false
    )
}

fun Bitmap.mirror(rotationDegrees: Float): Bitmap {
    val matrix = Matrix()
    if (rotationDegrees == 270f) {
        matrix.preScale(-1.0f, 1.0f)
    }

    return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            false
    )
}

fun Bitmap.crop(rect: Rect): Bitmap {
    return Bitmap.createBitmap(
                    this,
                    rect.left,
                    rect.top,
                    rect.width(),
                    rect.height()
            )
}


fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val uBuffer = planes[1].buffer // U
    val vBuffer = planes[2].buffer // V

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

fun Rect.scaledBy(percent: Float): Rect {
    val factor = 1 + percent
    val newWidth = width() * factor
    val newHeight = height() * factor
    val deltaX = (width() - newWidth) / 2
    val deltaY = (height() - newHeight) / 2

    return Rect((left + deltaX).toInt(), (top + deltaY).toInt(), (right - deltaX).toInt(), (bottom - deltaY).toInt())
}

fun Rect.resize(width: Int, height: Int): Rect {
    val left = centerX() - (width / 2)
    val top = centerY() - (height / 2)

    return Rect(
        left,
        top,
        left + width,
        top + height
    )
}

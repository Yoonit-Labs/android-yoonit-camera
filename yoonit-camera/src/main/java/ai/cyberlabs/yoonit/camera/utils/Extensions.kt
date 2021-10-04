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

package ai.cyberlabs.yoonit.camera.utils

import android.content.Context
import android.graphics.*
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

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

fun Bitmap.mirror(): Bitmap {
    val matrix = Matrix()
    matrix.preScale(-1.0f, 1.0f)

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

fun Image.toRGBBitmap(context: Context): Bitmap {

    // Get the YUV data
    val yuvBytes: ByteBuffer = imageToByteBuffer(this)

    // Convert YUV to RGB
    val rs: RenderScript = RenderScript.create(context)

    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val allocationRgb: Allocation = Allocation.createFromBitmap(rs, bitmap)

    val allocationYuv: Allocation = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().size)
    allocationYuv.copyFrom(yuvBytes.array())

    val scriptYuvToRgb: ScriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
    scriptYuvToRgb.setInput(allocationYuv)
    scriptYuvToRgb.forEach(allocationRgb)

    allocationRgb.copyTo(bitmap)

    return bitmap
}

fun Image.toYUVBitmap(): Bitmap {
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

fun Rect.scale(
    top: Float,
    right: Float,
    bottom: Float,
    left: Float
): Rect {
    val newLeft = this.exactCenterX() - ((left + 1) * this.width() / 2)
    val newTop = this.exactCenterY() - ((top + 1) * this.height() / 2)
    val newRight = this.exactCenterX() + ((right + 1) * this.width() / 2)
    val newBottom = this.exactCenterY() + ((bottom + 1) * this.height() / 2)

    return Rect(
        newLeft.toInt(),
        newTop.toInt(),
        newRight.toInt(),
        newBottom.toInt()
    )
}

fun RectF.scale(
    top: Float,
    right: Float,
    bottom: Float,
    left: Float
): RectF {
    val newLeft = this.centerX() - ((left + 1) * this.width() / 2)
    val newTop = this.centerY() - ((top + 1) * this.height() / 2)
    val newRight = this.centerX() + ((right + 1) * this.width() / 2)
    val newBottom = this.centerY() + ((bottom + 1) * this.height() / 2)

    return RectF(
        newLeft,
        newTop,
        newRight,
        newBottom
    )
}

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

package ai.cyberlabs.yoonit.camera.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import java.nio.ByteBuffer
import kotlin.math.roundToInt

object BlurBuilder {
    private const val BITMAP_SCALE = 0.4f
    private const val BLUR_RADIUS = 7.5f
    fun blur(context: Context?, image: Bitmap): Bitmap {
        val width = (image.width * BITMAP_SCALE).roundToInt()
        val height = (image.height * BITMAP_SCALE).roundToInt()
        val inputBitmap: Bitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap: Bitmap = Bitmap.createBitmap(inputBitmap)
        val rs: RenderScript = RenderScript.create(context)
        val theIntrinsic: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn: Allocation = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut: Allocation = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }
}

fun imageToByteBuffer(image: Image): ByteBuffer {
    val crop: Rect = image.cropRect
    val width: Int = crop.width()
    val height: Int = crop.height()

    val planes = image.planes
    val rowData = ByteArray(planes[0].rowStride)
    val bufferSize: Int = width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
    val output = ByteBuffer.allocateDirect(bufferSize)

    var channelOffset = 0
    var outputStride = 0

    for (planeIndex in 0..2) {
        if (planeIndex == 0) {
            channelOffset = 0
            outputStride = 1
        } else if (planeIndex == 1) {
            channelOffset = width * height + 1
            outputStride = 2
        } else if (planeIndex == 2) {
            channelOffset = width * height
            outputStride = 2
        }
        val buffer = planes[planeIndex].buffer
        val rowStride = planes[planeIndex].rowStride
        val pixelStride = planes[planeIndex].pixelStride
        val shift = if (planeIndex == 0) 0 else 1
        val widthShifted = width shr shift
        val heightShifted = height shr shift
        buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
        for (row in 0 until heightShifted) {
            val length: Int
            if (pixelStride == 1 && outputStride == 1) {
                length = widthShifted
                buffer[output.array(), channelOffset, length]
                channelOffset += length
            } else {
                length = (widthShifted - 1) * pixelStride + 1
                buffer[rowData, 0, length]
                for (col in 0 until widthShifted) {
                    output.array()[channelOffset] = rowData[col * pixelStride]
                    channelOffset += outputStride
                }
            }
            if (row < heightShifted - 1) {
                buffer.position(buffer.position() + rowStride - length)
            }
        }
    }

    return output
}

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

package ai.cyberlabs.yoonit.camera

import ai.cyberlabs.yoonit.camera.utils.BlurBuilder
import ai.cyberlabs.yoonit.camera.utils.crop
import ai.cyberlabs.yoonit.camera.utils.mirror
import ai.cyberlabs.yoonit.camera.utils.rotate
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Custom view to bind face bounding box to the preview view.
 */
class CameraGraphicView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var boundingBox: RectF? = null

    private var imageBitmap: Bitmap? = null

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        boundingBox?.let {

            imageBitmap?.let {bitmap ->
                canvas.drawBitmap(BlurBuilder.blur(context, bitmap), null, it, null)
            }

            canvas.drawRect(it, FACE_BOUNDING_BOX_PAINT)

            val left: Float = it.left
            val top: Float = it.top
            val right: Float = left + it.width()
            val bottom: Float = top + it.height()
            val toBottom = top + (it.height() * 0.3).toFloat()
            val toTop = top + (it.height() * 0.7).toFloat()
            val toRight = left + (it.width() * 0.3).toFloat()
            val toLeft = left + (it.width() * 0.7).toFloat()

            // edge - top-left > bottom-left
            canvas.drawLine(left, top, left, toBottom, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - top-right > bottom-right
            canvas.drawLine(right, top, right, toBottom, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - bottom-left > top-left
            canvas.drawLine(left, bottom, left, toTop, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - bottom-right > top-right
            canvas.drawLine(right, bottom, right, toTop, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - top-left > top-right
            canvas.drawLine(left, top, toRight, top, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - top-right > left-right
            canvas.drawLine(right, top, toLeft, top, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - bottom-left > right-left
            canvas.drawLine(left, bottom, toRight, bottom, FACE_BOUNDING_BOX_LINE_PAINT)
            // edge - bottom-right > right-left
            canvas.drawLine(right, bottom, toLeft, bottom, FACE_BOUNDING_BOX_LINE_PAINT)
        }

    }

    /**
     * Draw white face bounding box.
     *
     * @param boundingBox the face coordinates detected.
     */
    fun drawBoundingBox(boundingBox: RectF) {
        this.boundingBox = boundingBox

        this.imageBitmap = null

        this.postInvalidate()
    }

    fun drawFaceDetectionBox(boundingBox: RectF, imageBitmap: Bitmap) {
        this.boundingBox = boundingBox

        this.imageBitmap = imageBitmap

        this.postInvalidate()
    }

    /**
     * Erase anything draw.
     */
    fun clear() {
        this.boundingBox = null

        this.postInvalidate()
    }

    companion object {
        const val TAG = "CameraGraphicView"

        // Face bounding box styles.
        val FACE_BOUNDING_BOX_PAINT: Paint = Paint().apply {
            this.color = Color.WHITE
            this.style = Paint.Style.STROKE
            this.strokeWidth = 5.0f
        }

        val FACE_BOUNDING_BOX_LINE_PAINT = Paint().apply {
            this.color = Color.WHITE
            this.style = Paint.Style.STROKE
            this.strokeWidth = 16.0f
            this.strokeCap = Paint.Cap.ROUND
        }
    }
}

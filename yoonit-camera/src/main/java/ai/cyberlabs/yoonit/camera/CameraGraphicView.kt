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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (this.boundingBox == null) {
            return
        }

        canvas.drawRect(this.boundingBox!!, FACE_BOUNDING_BOX_PAINT)

//        val xOffset = this.boundingBox!!.width() / 2.0f
//        val yOffset = this.boundingBox!!.height() / 2.0f
//
//        val left: Float = this.boundingBox!!.left
//        val top: Float = this.boundingBox!!.top
//        val right: Float = this.boundingBox!!.width()
//        val bottom: Float = this.boundingBox!!.height()
//
//        // edge - top-left > bottom-left
//        canvas?.drawLine(left, top, left, bottom - yOffset * 1.24.toFloat(), FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - top-right > bottom-right
//        canvas?.drawLine(right, top, right, bottom - yOffset * 1.24.toFloat(), FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - bottom-left > top-left
//        canvas?.drawLine(left, bottom, left, bottom - yOffset * 0.5.toFloat(), FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - bottom-right > top-right
//        canvas?.drawLine(right, bottom, right, bottom - yOffset * 0.5.toFloat(), FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - top-left > top-right
//        canvas?.drawLine(left, top, left + yOffset * 0.5.toFloat(), top, FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - top-right > left-right
//        canvas?.drawLine(right, top, right - yOffset * 0.5.toFloat(), top, FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - bottom-left > right-left
//        canvas?.drawLine(left, bottom, left + yOffset * 0.5.toFloat(), bottom, FACE_BOUNDING_BOX_LINE_PAINT)
//        // edge - bottom-right > right-left
//        canvas?.drawLine(right, bottom, right - yOffset * 0.5.toFloat(), bottom, FACE_BOUNDING_BOX_LINE_PAINT)
    }

    /**
     * Draw white face bounding box.
     *
     * @param boundingBox the face coordinates detected.
     */
    fun drawBoundingBox(boundingBox: RectF) {
        this.boundingBox = boundingBox

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

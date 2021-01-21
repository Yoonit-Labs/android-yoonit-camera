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
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * Custom view to draw face detection box and face blurred.
 */
class CameraGraphicView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // The face detection box within the graphic view.
    private var faceDetectionBox: RectF? = null
        set(value) {
            this.drawFaceDetectionBox = value != null
            field = value
        }
    private var drawFaceDetectionBox: Boolean = false

    // The face bitmap.
    private var faceBlurBitmap: Bitmap? = null
        set(value) {
            this.drawFaceBlurBitmap = value != null
            field = value
        }
    private var drawFaceBlurBitmap: Boolean = false

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (this.faceBlurBitmap != null && this.drawFaceBlurBitmap) {
            val faceBlurredBitmap: Bitmap = BlurBuilder.blur(
                this.context,
                this.faceBlurBitmap!!
            )

            canvas.drawBitmap(
                faceBlurredBitmap,
                null,
                this.faceDetectionBox!!,
                null
            )
        }

        if (this.faceDetectionBox != null && this.drawFaceDetectionBox) {
            canvas.drawRect(this.faceDetectionBox!!, FACE_BOUNDING_BOX_PAINT)

            val left: Float = this.faceDetectionBox!!.left
            val top: Float = this.faceDetectionBox!!.top
            val right: Float = left + this.faceDetectionBox!!.width()
            val bottom: Float = top + this.faceDetectionBox!!.height()
            val toBottom = top + (this.faceDetectionBox!!.height() * 0.3).toFloat()
            val toTop = top + (this.faceDetectionBox!!.height() * 0.7).toFloat()
            val toRight = left + (this.faceDetectionBox!!.width() * 0.3).toFloat()
            val toLeft = left + (this.faceDetectionBox!!.width() * 0.7).toFloat()

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
     * Draw white face detection box.
     *
     * @param faceDetectionBox the face coordinates within the graphic view.
     */
    fun drawFaceDetectionBox(faceDetectionBox: RectF) {
        this.faceDetectionBox = faceDetectionBox
    }

    /**
     * Draw face bitmap blurred above the face detection box.
     *
     * @param faceDetectionBox The face coordinates within the graphic view.
     * @param faceBlurBitmap The face bitmap.
     */
    fun drawFaceBlur(faceDetectionBox: RectF, faceBlurBitmap: Bitmap) {
        this.faceDetectionBox = faceDetectionBox
        this.faceBlurBitmap = faceBlurBitmap
    }

    /**
     * Erase anything draw.
     */
    fun clear() {
        this.faceDetectionBox = null
        this.faceBlurBitmap = null

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

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

import ai.cyberlabs.yoonit.camera.models.CaptureOptions
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

    // The face bitmap.
    private var faceBlurBitmap: Bitmap? = null

    // The face contours within the graphic view.
    private var faceContours: MutableList<PointF>? = null

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw face blur.
        this.faceBlurBitmap?.let {
            faceBlurBitmap ->

            if (CaptureOptions.blurFaceDetectionBox) {
                this.faceDetectionBox?.let { detectionBox->
                    canvas.drawBitmap(
                            faceBlurBitmap,
                            null,
                            detectionBox,
                            null
                    )
                }

            }
        }

        CaptureOptions.colorEncoding

        // Draw face contours.
        this.faceContours?.let {
            faceContours ->

            faceContours.forEach {
                point ->

                canvas.drawPoint(point.x, point.y, Paint().apply {
                    this.color = CaptureOptions.faceContoursColor
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = 5.0f
                })
            }
        }

        // Face detection box.
        this.faceDetectionBox?.let {
            faceDetectionBox ->

            if (CaptureOptions.faceDetectionBox) {
                canvas.drawRect(this.faceDetectionBox!!, FACE_BOUNDING_BOX_PAINT)

                val left: Float = faceDetectionBox.left
                val top: Float = faceDetectionBox.top
                val right: Float = left + faceDetectionBox.width()
                val bottom: Float = top + faceDetectionBox.height()
                val toBottom = top + (faceDetectionBox.height() * 0.3).toFloat()
                val toTop = top + (faceDetectionBox.height() * 0.7).toFloat()
                val toRight = left + (faceDetectionBox.width() * 0.3).toFloat()
                val toLeft = left + (faceDetectionBox.width() * 0.7).toFloat()

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
     * Draw white face detection points.
     *
     * @param faceContours the face coordinates within the graphic view.
     */
    fun drawFaceContours(faceContours: MutableList<PointF>) {
        this.faceContours = faceContours
    }

    /**
     * Draw face bitmap blurred above the face detection box.
     *
     * @param faceDetectionBox The face coordinates within the graphic view.
     * @param faceBitmap The face bitmap.
     */
    fun drawFaceBlur(faceDetectionBox: RectF, faceBitmap: Bitmap) {
        this.faceDetectionBox = faceDetectionBox
        this.faceBlurBitmap = BlurBuilder.blur(this.context, faceBitmap)
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

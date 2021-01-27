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

    // The face region of interest area offset bitmap.
    private var faceROIAreaOffsetBitmap: Bitmap? = null

    override fun draw(canvas: Canvas) {
        if (
            !CaptureOptions.faceDetectionBox &&
            !CaptureOptions.blurFaceDetectionBox &&
            !CaptureOptions.faceContours &&
            !CaptureOptions.faceROI.enable &&
            !CaptureOptions.faceROI.areaOffsetEnable
        ) {
            return
        }

        // Draw face blur.
        if (CaptureOptions.blurFaceDetectionBox) {
            this.drawFaceBlur(canvas)
        }

        // Draw face contours.
        if (CaptureOptions.faceContours) {
            this.drawFaceContours(canvas)
        }

        // Draw face region of interest area offset bitmap.
        if (
            CaptureOptions.faceROI.enable &&
            CaptureOptions.faceROI.areaOffsetEnable
        ) {
            this.drawFaceROIAreaOffset(canvas)
        }

        // Draw face detection box.
        if (CaptureOptions.faceDetectionBox) {
            this.drawFaceDetectionBox(canvas)
        }

        super.draw(canvas)
    }

    /**
     * Draw face bitmap blurred above the face detection box.
     *
     * @param faceDetectionBox The face coordinates within the graphic view.
     * @param faceBitmap The face bitmap to be blurred.
     * @param faceContours The face coordinates within the graphic view.
     */
    fun handleDraw(
        faceDetectionBox: RectF?,
        faceBitmap: Bitmap?,
        faceContours: MutableList<PointF>?
    ) {
        this.faceDetectionBox = faceDetectionBox
        faceBitmap?.let {
            this.faceBlurBitmap = BlurBuilder.blur(this.context, it)
        }
        this.faceContours = faceContours

        this.postInvalidate()
    }

    /**
     * Erase anything draw.
     */
    fun clear() {
        this.faceDetectionBox = null
        this.faceBlurBitmap = null
        this.faceContours = null
        this.faceROIAreaOffsetBitmap = null

        this.postInvalidate()
    }

    /**
     * Draw the face blur bitmap.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawFaceBlur(canvas: Canvas) {
        this.faceBlurBitmap?.let { faceBlurBitmap ->
            canvas.drawBitmap(
                faceBlurBitmap,
                null,
                this.faceDetectionBox!!,
                null
            )
        }
    }

    /**
     * Draw the face contours.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawFaceContours(canvas: Canvas) {
        this.faceContours?.let { faceContours ->
            faceContours.forEach { contour ->
                canvas.drawPoint(contour.x, contour.y, Paint().apply {
                    this.color = CaptureOptions.faceContoursColor
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = 5.0f
                })
            }
        }
    }

    /**
     * Draw the face region of interest area offset bitmap.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawFaceROIAreaOffset(canvas: Canvas) {
        val width: Int = canvas.width
        val height: Int = canvas.height

        if (width == 0 || height == 0) {
            return
        }

        this.faceROIAreaOffsetBitmap?.let {
            if (
                CaptureOptions.faceROI.layoutWidth == width &&
                CaptureOptions.faceROI.layoutHeight == height
            ) {
                canvas.drawBitmap(it, 0f, 0f, null)
                return
            }
        }

        CaptureOptions.faceROI.layoutWidth = width
        CaptureOptions.faceROI.layoutHeight = height

        val roi = Rect(
            (width * CaptureOptions.faceROI.leftOffset).toInt(),
            (height * CaptureOptions.faceROI.topOffset).toInt(),
            (width - (width * CaptureOptions.faceROI.rightOffset)).toInt(),
            (height - (height * CaptureOptions.faceROI.bottomOffset)).toInt()
        )

        val faceROIAreaOffsetBitmap: Bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val areaCanvas = Canvas(faceROIAreaOffsetBitmap)

        val offsetAreaPaint = Paint()
        offsetAreaPaint.color = CaptureOptions.faceROI.areaOffsetColor
        areaCanvas.drawRect(Rect(
            0,
            0,
            width,
            height
        ), offsetAreaPaint)

        offsetAreaPaint.color = Color.TRANSPARENT
        offsetAreaPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        areaCanvas.drawRect(roi, offsetAreaPaint)

        this.faceROIAreaOffsetBitmap = faceROIAreaOffsetBitmap

        canvas.drawBitmap(faceROIAreaOffsetBitmap, 0f, 0f, null)
    }

    /**
     * Draw the face detection box.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawFaceDetectionBox(canvas: Canvas) {
        this.faceDetectionBox?.let { faceDetectionBox ->
            canvas.drawRect(faceDetectionBox, FACE_BOUNDING_BOX_PAINT)

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

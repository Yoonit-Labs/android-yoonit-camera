/**
 * +-+-+-+-+-+-+
 * |y|o|o|n|i|t|
 * +-+-+-+-+-+-+
 *
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * | Yoonit Camera lib for Android applications                      |
 * | Haroldo Teruya & Victor Goulart @ Cyberlabs AI 2020-2021        |
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
 * This view is responsible to draw:
 *
 * - face/qrcode detection box;
 * - face/qrcode region of interest area offset;
 * - face blur;
 * - face contours;
 */
class CameraGraphicView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // The face detection box.
    private var detectionBox: RectF? = null

    // The face bitmap.
    private var faceBlurBitmap: Bitmap? = null

    // The face contours within the graphic view.
    private var faceContours: MutableList<PointF>? = null

    // The face/qrcode region of interest area offset bitmap.
    private var roiAreaOffsetBitmap: Bitmap? = null

    // Indicates if the ROI area offset is drawable.
    private var isROIDrawable: Boolean = false

    override fun draw(canvas: Canvas) {
        if (
            !CaptureOptions.detectionBox &&
            !CaptureOptions.blurFaceDetectionBox &&
            !CaptureOptions.faceContours &&
            !CaptureOptions.roi.enable &&
            !CaptureOptions.roi.areaOffsetEnable
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

        // Draw face/qrcode region of interest area offset bitmap.
        if (
            CaptureOptions.roi.enable &&
            CaptureOptions.roi.areaOffsetEnable &&
            this.isROIDrawable
        ) {
            this.drawROIAreaOffset(canvas)
        }

        // Draw face/qrcode detection box.
        if (CaptureOptions.detectionBox) {
            this.drawDetectionBox(canvas)
        }

        super.draw(canvas)
    }

    /**
     * @param detectionBox The face/qrcode coordinates within the graphic view.
     * @param faceBitmap The face bitmap to be blurred.
     * @param faceContours List of points that represents the shape of the face detected .
     */
    fun handleDraw(
        detectionBox: RectF?,
        faceBitmap: Bitmap?,
        faceContours: MutableList<PointF>?
    ) {
        this.detectionBox = detectionBox
        this.faceBlurBitmap = faceBitmap
        this.faceContours = faceContours
        this.isROIDrawable = true

        this.postInvalidate()
    }

    /**
     * Erase anything draw.
     */
    fun clear() {
        this.detectionBox = null
        this.faceBlurBitmap = null
        this.faceContours = null
        this.roiAreaOffsetBitmap = null
        this.isROIDrawable = false

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
                BlurBuilder.blur(this.context, faceBlurBitmap),
                null,
                this.detectionBox!!,
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
                canvas.drawPoint(
                    contour.x, contour.y,
                    FACE_CONTOURS_PAINT
                )
            }
        }
    }

    /**
     * Draw the region of interest area offset bitmap.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawROIAreaOffset(canvas: Canvas) {
        val width: Int = canvas.width
        val height: Int = canvas.height

        if (width == 0 || height == 0) {
            return
        }

        this.roiAreaOffsetBitmap?.let {
            if (
                CaptureOptions.roi.layoutWidth == width &&
                CaptureOptions.roi.layoutHeight == height
            ) {
                canvas.drawBitmap(it, 0f, 0f, null)
                return
            }
        }

        CaptureOptions.roi.layoutWidth = width
        CaptureOptions.roi.layoutHeight = height

        val roi = Rect(
            (width * CaptureOptions.roi.leftOffset).toInt(),
            (height * CaptureOptions.roi.topOffset).toInt(),
            (width - (width * CaptureOptions.roi.rightOffset)).toInt(),
            (height - (height * CaptureOptions.roi.bottomOffset)).toInt()
        )

        val faceROIAreaOffsetBitmap: Bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val areaCanvas = Canvas(faceROIAreaOffsetBitmap)

        val offsetAreaPaint = Paint()
        offsetAreaPaint.color = CaptureOptions.roi.areaOffsetColor
        areaCanvas.drawRect(
            Rect(
                0,
                0,
                width,
                height
            ),
            offsetAreaPaint
        )

        offsetAreaPaint.color = Color.TRANSPARENT
        offsetAreaPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        areaCanvas.drawRect(roi, offsetAreaPaint)

        this.roiAreaOffsetBitmap = faceROIAreaOffsetBitmap

        canvas.drawBitmap(faceROIAreaOffsetBitmap, 0f, 0f, null)
    }

    /**
     * Draw the face/qrcode detection box.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawDetectionBox(canvas: Canvas) {
        this.detectionBox?.let { detectionBox ->
            canvas.drawRect(detectionBox, DETECTION_BOX_PAINT)

            val left: Float = detectionBox.left
            val top: Float = detectionBox.top
            val right: Float = left + detectionBox.width()
            val bottom: Float = top + detectionBox.height()
            val toBottom = top + (detectionBox.height() * 0.3).toFloat()
            val toTop = top + (detectionBox.height() * 0.7).toFloat()
            val toRight = left + (detectionBox.width() * 0.3).toFloat()
            val toLeft = left + (detectionBox.width() * 0.7).toFloat()

            // edge - top-left > bottom-left
            canvas.drawLine(left, top, left, toBottom, DETECTION_BOX_LINE_PAINT)
            // edge - top-right > bottom-right
            canvas.drawLine(right, top, right, toBottom, DETECTION_BOX_LINE_PAINT)
            // edge - bottom-left > top-left
            canvas.drawLine(left, bottom, left, toTop, DETECTION_BOX_LINE_PAINT)
            // edge - bottom-right > top-right
            canvas.drawLine(right, bottom, right, toTop, DETECTION_BOX_LINE_PAINT)
            // edge - top-left > top-right
            canvas.drawLine(left, top, toRight, top, DETECTION_BOX_LINE_PAINT)
            // edge - top-right > left-right
            canvas.drawLine(right, top, toLeft, top, DETECTION_BOX_LINE_PAINT)
            // edge - bottom-left > right-left
            canvas.drawLine(left, bottom, toRight, bottom, DETECTION_BOX_LINE_PAINT)
            // edge - bottom-right > right-left
            canvas.drawLine(right, bottom, toLeft, bottom, DETECTION_BOX_LINE_PAINT)
        }
    }

    companion object {
        const val TAG = "CameraGraphicView"

        // Face detection box styles.
        private var DETECTION_BOX_PAINT: Paint = Paint().apply {
            this.color = CaptureOptions.detectionBoxColor
            this.style = Paint.Style.STROKE
            this.strokeWidth = 5.0f
        }

        // Face detection box styles.
        private var DETECTION_BOX_LINE_PAINT = Paint().apply {
            this.color = CaptureOptions.detectionBoxColor
            this.style = Paint.Style.STROKE
            this.strokeWidth = 16.0f
            this.strokeCap = Paint.Cap.ROUND
        }

        // Face contours styles.
        private var FACE_CONTOURS_PAINT: Paint = Paint().apply {
            this.color = CaptureOptions.faceContoursColor
            this.style = Paint.Style.STROKE
            this.strokeWidth = 5.0f
        }
    }
}

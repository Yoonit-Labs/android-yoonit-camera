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

    override fun draw(canvas: Canvas) {
        if (
            !CaptureOptions.detectionBox &&
            !CaptureOptions.blurFaceDetectionBox &&
            !CaptureOptions.faceContours &&
            !CaptureOptions.ROI.enable &&
            !CaptureOptions.ROI.areaOffsetEnable
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
            CaptureOptions.ROI.enable &&
            CaptureOptions.ROI.areaOffsetEnable
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
        detectionBox: RectF? = null,
        faceBitmap: Bitmap? = null,
        faceContours: MutableList<PointF>? = null
    ) {
        this.detectionBox = detectionBox
        this.faceBlurBitmap = faceBitmap
        this.faceContours = faceContours

        this.postInvalidate()
    }

    /**
     * Erase anything draw.
     */
    fun clear() {
        this.detectionBox = null
        this.faceBlurBitmap = null
        this.faceContours = null

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
                    Paint().apply {
                        this.color = CaptureOptions.faceContoursColor
                        this.style = Paint.Style.STROKE
                        this.strokeWidth = 5.0f
                    }
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

        val roi = Rect(
            (width * CaptureOptions.ROI.leftOffset).toInt(),
            (height * CaptureOptions.ROI.topOffset).toInt(),
            (width - (width * CaptureOptions.ROI.rightOffset)).toInt(),
            (height - (height * CaptureOptions.ROI.bottomOffset)).toInt()
        )

        val roiAreaOffsetBitmap: Bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val areaCanvas = Canvas(roiAreaOffsetBitmap)

        val offsetAreaPaint = Paint()
        offsetAreaPaint.color = CaptureOptions.ROI.areaOffsetColor
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
        canvas.drawBitmap(roiAreaOffsetBitmap, 0f, 0f, null)
    }

    /**
     * Draw the face/qrcode detection box.
     *
     * @param canvas The canvas is where has to draw.
     */
    private fun drawDetectionBox(canvas: Canvas) {
        this.detectionBox?.let { detectionBox ->
            val detectionBoxPaint = Paint().apply {
                this.color = CaptureOptions.detectionBoxColor
                this.style = Paint.Style.STROKE
                this.strokeWidth = 5.0f
            }

            canvas.drawRect(detectionBox, detectionBoxPaint)

            val left: Float = detectionBox.left
            val top: Float = detectionBox.top
            val right: Float = left + detectionBox.width()
            val bottom: Float = top + detectionBox.height()
            val toBottom = top + (detectionBox.height() * 0.3).toFloat()
            val toTop = top + (detectionBox.height() * 0.7).toFloat()
            val toRight = left + (detectionBox.width() * 0.3).toFloat()
            val toLeft = left + (detectionBox.width() * 0.7).toFloat()
            val detectionBoxLinePaint = Paint().apply {
                this.color = CaptureOptions.detectionBoxColor
                this.style = Paint.Style.STROKE
                this.strokeWidth = 16.0f
                this.strokeCap = Paint.Cap.ROUND
            }

            // edge - top-left > bottom-left
            canvas.drawLine(left, top, left, toBottom, detectionBoxLinePaint)
            // edge - top-right > bottom-right
            canvas.drawLine(right, top, right, toBottom, detectionBoxLinePaint)
            // edge - bottom-left > top-left
            canvas.drawLine(left, bottom, left, toTop, detectionBoxLinePaint)
            // edge - bottom-right > top-right
            canvas.drawLine(right, bottom, right, toTop, detectionBoxLinePaint)
            // edge - top-left > top-right
            canvas.drawLine(left, top, toRight, top, detectionBoxLinePaint)
            // edge - top-right > left-right
            canvas.drawLine(right, top, toLeft, top, detectionBoxLinePaint)
            // edge - bottom-left > right-left
            canvas.drawLine(left, bottom, toRight, bottom, detectionBoxLinePaint)
            // edge - bottom-right > right-left
            canvas.drawLine(right, bottom, toLeft, bottom, detectionBoxLinePaint)
        }
    }
}

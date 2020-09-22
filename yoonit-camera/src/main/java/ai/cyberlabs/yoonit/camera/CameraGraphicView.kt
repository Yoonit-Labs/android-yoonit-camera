/**
 *
 * CameraGraphicView.kt
 * CameraGraphicView
 *
 * Created by Haroldo Shigueaki Teruya on 13/08/2020.
 * Copyright © 2020 CyberLabs.AI. All rights reserved.
 *
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
    }
}

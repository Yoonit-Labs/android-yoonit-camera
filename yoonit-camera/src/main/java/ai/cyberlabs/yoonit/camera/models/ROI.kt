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

package ai.cyberlabs.yoonit.camera.models

import android.graphics.Color

/**
 * Model to set region of interest.
 */
class ROI {

    // Enable or disable ROI.
    var enable: Boolean = false

    // Enable/disable region of interest area offset.
    var areaOffsetEnable: Boolean = false

    // Region of interest area offset color.
    var areaOffsetColor: Int = Color.argb(100, 255, 255, 255)

    // Region of interest in percentage.
    // Values valid [0, 1].
    var topOffset: Float = 0.0f
    var rightOffset: Float = 0.0f
    var bottomOffset: Float = 0.0f
    var leftOffset: Float = 0.0f

    // Return if any attributes has modifications.
    var hasChanges: Boolean = false
        get() {
            return (
                this.topOffset != 0.0f ||
                    this.rightOffset != 0.0f ||
                    this.bottomOffset != 0.0f ||
                    this.leftOffset != 0.0f
                )
        }

    /**
     * Current offsets is out of the offset parameters.
     *
     * @param topOffset top offset.
     * @param rightOffset right offset.
     * @param bottomOffset bottom offset.
     * @param leftOffset left offset.
     * @return is out of the offset parameters.
     */
    fun isOutOf(
        topOffset: Float,
        rightOffset: Float,
        bottomOffset: Float,
        leftOffset: Float
    ): Boolean {
        return (
            this.topOffset > topOffset ||
                this.rightOffset > rightOffset ||
                this.bottomOffset > bottomOffset ||
                this.leftOffset > leftOffset
            )
    }
}

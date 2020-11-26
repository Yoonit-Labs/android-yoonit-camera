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

package ai.cyberlabs.yoonit.camera.models

/**
 * Model to set face region of interest.
 */
class FaceROI {

    // Enable or disable ROI.
    var enable: Boolean = false

    // Region of interest in percentage.
    // Values valid [0, 1].
    var topOffset: Float = 0.0f // "Above" the face detected.
    var rightOffset: Float = 0.0f // "Right" of face detected.
    var bottomOffset: Float = 0.0f // "Bottom" face detected.
    var leftOffset: Float = 0.0f // "Left" face detected.

    // Minimum face size in percentage in relation of the ROI.
    var minimumSize: Float = 0.0f

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

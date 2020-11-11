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

object KeyError {

    // Tried to start a process that depends on to start the camera preview.
    const val NOT_STARTED_PREVIEW = "NOT_STARTED_PREVIEW"

    // Tried to start a non-existent capture type.
    const val INVALID_CAPTURE_TYPE = "INVALID_CAPTURE_TYPE"

    // Tried to input invalid face/frame number of images to capture.
    const val INVALID_NUMBER_OF_IMAGES: String = "INVALID_NUMBER_OF_IMAGES"

    // Tried to input invalid face/frame time interval to capture.
    const val INVALID_TIME_BETWEEN_IMAGES: String = "INVALID_TIME_BETWEEN_IMAGES"

    // Tried to input invalid image width.
    const val INVALID_OUTPUT_IMAGE_WIDTH: String = "INVALID_OUTPUT_IMAGE_WIDTH"

    // Tried to input invalid image height.
    const val INVALID_OUTPUT_IMAGE_HEIGHT: String = "INVALID_OUTPUT_IMAGE_HEIGHT"

    // Tried to input invalid face padding percent.
    const val INVALID_FACE_PADDING_PERCENT = "INVALID_FACE_PADDING_PERCENT"

    // Tried to input invalid face capture minimum size.
    const val INVALID_FACE_CAPTURE_MIN_SIZE = "INVALID_FACE_CAPTURE_MIN_SIZE"

    // Tried to input invalid face capture maximum size.
    const val INVALID_FACE_CAPTURE_MAX_SIZE = "INVALID_FACE_CAPTURE_MAX_SIZE"

    // Tried to input invalid frame number of images to capture.
    const val INVALID_FRAME_NUMBER_OF_IMAGES = "INVALID_FRAME_NUMBER_OF_IMAGES"

    // Tried to input invalid frame time interval to capture face.
    const val INVALID_FRAME_TIME_BETWEEN_IMAGES = "INVALID_FRAME_TIME_BETWEEN_IMAGES"

    // Tried to input invalid face region of interesting offset.
    const val INVALID_FACE_ROI_OFFSET = "INVALID_FACE_ROI_OFFSET"

    // Tried to input invalid face region of interest minimum size.
    const val INVALID_FACE_ROI_MIN_SIZE = "INVALID_FACE_ROI_MIN_SIZE"
}

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
    const val NOT_STARTED_PREVIEW: String = "NOT_STARTED_PREVIEW"

    // Tried to start a non-existent capture type.
    const val INVALID_CAPTURE_TYPE: String = "INVALID_CAPTURE_TYPE"

    // Tried to input invalid face number of images to capture.
    const val INVALID_FACE_NUMBER_OF_IMAGES: String = "INVALID_FACE_NUMBER_OF_IMAGES"

    // Tried to input invalid face time interval to capture face.
    const val INVALID_FACE_TIME_BETWEEN_IMAGES: String = "INVALID_FACE_TIME_BETWEEN_IMAGES"

    // Tried to input invalid face padding percent.
    const val INVALID_FACE_PADDING_PERCENT: String = "INVALID_FACE_PADDING_PERCENT"

    // Tried to input invalid image width or height.
    const val INVALID_FACE_IMAGE_SIZE: String = "INVALID_FACE_IMAGE_SIZE"

    // Tried to input invalid face capture minimum size.
    const val INVALID_FACE_CAPTURE_MIN_SIZE: String = "INVALID_FACE_CAPTURE_MIN_SIZE"

    // Tried to input invalid face capture maximum size.
    const val INVALID_FACE_CAPTURE_MAX_SIZE: String = "INVALID_FACE_CAPTURE_MAX_SIZE"

    // Tried to input invalid frame number of images to capture.
    const val INVALID_FRAME_NUMBER_OF_IMAGES: String = "INVALID_FRAME_NUMBER_OF_IMAGES"

    // Tried to input invalid frame time interval to capture face.
    const val INVALID_FRAME_TIME_BETWEEN_IMAGES: String = "INVALID_FRAME_TIME_BETWEEN_IMAGES"
}

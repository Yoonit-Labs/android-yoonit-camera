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
 * Haroldo Teruya, Victor Goulart, Thúlio Noslen & Luigui Delyer @ 2020-2021
 */

package ai.cyberlabs.yoonit.camera

object KeyError {

    // Tried to start a non-existent capture type.
    const val INVALID_CAPTURE_TYPE = "INVALID_CAPTURE_TYPE"

    // Tried to input invalid camera lens.
    const val INVALID_CAMERA_LENS = "INVALID_CAMERA_LENS"

    // Tried to input invalid face/frame number of images to capture.
    const val INVALID_NUMBER_OF_IMAGES = "INVALID_NUMBER_OF_IMAGES"

    // Tried to input invalid face/frame time interval to capture.
    const val INVALID_TIME_BETWEEN_IMAGES = "INVALID_TIME_BETWEEN_IMAGES"

    // Tried to input invalid image width.
    const val INVALID_OUTPUT_IMAGE_WIDTH = "INVALID_OUTPUT_IMAGE_WIDTH"

    // Tried to input invalid image height.
    const val INVALID_OUTPUT_IMAGE_HEIGHT = "INVALID_OUTPUT_IMAGE_HEIGHT"

    // Tried to input invalid detection box ARGB value color.
    const val INVALID_DETECTION_BOX_COLOR = "INVALID_DETECTION_BOX_COLOR"

    // Tried to input invalid minimum size.
    const val INVALID_MINIMUM_SIZE = "INVALID_MINIMUM_SIZE"

    // Tried to input invalid maximum size.
    const val INVALID_MAXIMUM_SIZE = "INVALID_MAXIMUM_SIZE"

    // Tried to input invalid face contour ARGB value color.
    const val INVALID_FACE_CONTOURS_COLOR = "INVALID_FACE_CONTOURS_COLOR"

    // Tried to input invalid face region of interest top offset.
    const val INVALID_ROI_TOP_OFFSET = "INVALID_ROI_TOP_OFFSET"

    // Tried to input invalid face region of interest right offset.
    const val INVALID_ROI_RIGHT_OFFSET = "INVALID_ROI_RIGHT_OFFSET"

    // Tried to input invalid face region of interest bottom offset.
    const val INVALID_ROI_BOTTOM_OFFSET = "INVALID_ROI_BOTTOM_OFFSET"

    // Tried to input invalid face region of interest left offset.
    const val INVALID_ROI_LEFT_OFFSET = "INVALID_ROI_LEFT_OFFSET"

    // Tried to input invalid face region of interest area offset ARGB value color.
    const val INVALID_ROI_COLOR = "INVALID_ROI_COLOR"

    // Tried to input invalid image capture color encoding.
    const val INVALID_IMAGE_CAPTURE_COLOR_ENCODING = "INVALID_IMAGE_CAPTURE_COLOR_ENCODING"

    // Tried to input a non existent computer vision model paths.
    const val INVALID_COMPUTER_VISION_MODEL_PATHS = "INVALID_COMPUTER_VISION_MODEL_PATHS"
}

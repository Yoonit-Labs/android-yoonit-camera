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

object Message {

    // Face/QRCode width percentage in relation of the screen width is less than the set.
    const val INVALID_MINIMUM_SIZE = "INVALID_MINIMUM_SIZE"

    // Face/QRCode width percentage in relation of the screen width is more than the set.
    const val INVALID_MAXIMUM_SIZE = "INVALID_MAXIMUM_SIZE"

    // Face bounding box is out of the setted region of interest.
    const val INVALID_OUT_OF_ROI = "INVALID_OUT_OF_ROI"

    // Not available with camera lens "front".
    const val INVALID_TORCH_LENS_USAGE = "INVALID_TORCH_LENS_USAGE"
}

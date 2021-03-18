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

package ai.cyberlabs.yoonit.camera.controllers

import ai.cyberlabs.yoonit.camera.CameraGraphicView
import androidx.camera.core.ImageAnalysis
import java.util.concurrent.Executors

class ImageAnalyzerController(private val graphicView: CameraGraphicView) {

    var analysis: ImageAnalysis? = null

    /**
     * Instantiate [ImageAnalysis] object.
     */
    fun build() {
        this.analysis = ImageAnalysis
            .Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    /**
     * Start camera image analyzer.
     */
    fun start(analyzer: ImageAnalysis.Analyzer) {
        if (this.analysis == null) {
            return
        }

        this.analysis?.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            analyzer
        )
    }

    /**
     * Stop camera image analyzer and clean graphic view.
     */
    fun stop() {
        if (this.analysis == null) {
            return
        }

        this.analysis?.clearAnalyzer()
        this.analysis?.setAnalyzer(
            Executors.newFixedThreadPool(1),
            ImageAnalysis.Analyzer {
                this.graphicView.clear()
                it.close()
            }
        )
    }

    companion object {
        private const val TAG = "ImageAnalyzerController"
    }
}

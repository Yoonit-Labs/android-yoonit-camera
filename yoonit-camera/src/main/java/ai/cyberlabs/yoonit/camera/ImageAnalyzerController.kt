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

import androidx.camera.core.ImageAnalysis
import java.util.concurrent.Executors

class ImageAnalyzerController(private val graphicView: CameraGraphicView) {

    lateinit var analysis: ImageAnalysis

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
        this.analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            analyzer
        )
    }

    /**
     * Stop camera image analyzer and clean graphic view.
     */
    fun stop() {
        this.analysis.clearAnalyzer()
        this.analysis.setAnalyzer(
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
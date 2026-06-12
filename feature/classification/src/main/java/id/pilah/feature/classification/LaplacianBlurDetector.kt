package id.pilah.feature.classification

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import javax.inject.Inject

/**
 * Implementasi [BlurDetector] ringan: gambar di-downsample lalu dikonvolusi
 * dengan kernel Laplacian 3x3, variansnya menjadi indikator ketajaman (PRD §3.2).
 */
class LaplacianBlurDetector @Inject constructor() : BlurDetector {

    override fun varianceOf(path: String): Double? {
        val bitmap = decodeSampledBitmap(path) ?: return null
        return try {
            laplacianVariance(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeSampledBitmap(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > TARGET_DIMENSION || bounds.outHeight / sampleSize > TARGET_DIMENSION) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun laplacianVariance(bitmap: Bitmap): Double? {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 3 || height < 3) return null

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(pixels.size) { i ->
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (r * 299 + g * 587 + b * 114) / 1000
        }

        var sum = 0.0
        var sumSquares = 0.0
        var count = 0
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = gray[y * width + x]
                val laplacian = gray[(y - 1) * width + x] + gray[(y + 1) * width + x] +
                    gray[y * width + x - 1] + gray[y * width + x + 1] - 4 * center
                sum += laplacian
                sumSquares += laplacian.toDouble() * laplacian
                count++
            }
        }

        val mean = sum / count
        return sumSquares / count - mean * mean
    }

    private companion object {
        const val TARGET_DIMENSION = 256
    }
}

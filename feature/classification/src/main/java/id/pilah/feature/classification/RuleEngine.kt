package id.pilah.feature.classification

import id.pilah.core.model.FileItem

/** Menghitung skor kepentingan 0-100 sebuah file berdasarkan sinyal berbobot (PRD §3.2). */
interface RuleEngine {
    fun classify(file: FileItem, context: RuleContext, weights: RuleWeights): ClassificationResult
}

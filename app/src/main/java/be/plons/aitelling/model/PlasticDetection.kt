
package be.plons.aitelling.model

enum class PlasticCategory(val displayName: String) {
    BOTTLE("Plastic fles"),
    CAP("Plastic dop"),
    BAG("Plastic zak"),
    PACKAGING("Plastic verpakking"),
    CUP("Plastic beker"),
    FRAGMENT("Plastic fragment"),
    OTHER("Overig plastic")
}

data class BoundingBox(
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float
)

data class PlasticDetection(
    val category: PlasticCategory,
    val confidence: Float,
    val boundingBox: BoundingBox
)

data class DetectionResult(
    val detections: List<PlasticDetection>
) {
    val totalCount: Int
        get() = detections.size

    fun countByCategory(): Map<PlasticCategory, Int> {
        return detections
            .groupingBy { it.category }
            .eachCount()
    }
}

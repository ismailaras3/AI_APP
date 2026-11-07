
package be.plons.aitelling.network

import org.json.JSONObject

data class ApiDetection(
    val category: String,
    val confidence: Float,
    val xMin: Float,
    val yMin: Float,
    val xMax: Float,
    val yMax: Float
)

data class ApiAnalysis(
    val message: String,
    val candidateCount: Int,
    val imageWidth: Int,
    val imageHeight: Int,
    val detections: List<ApiDetection>
) {
    companion object {
        fun fromJson(json: JSONObject): ApiAnalysis {
            val image = json.getJSONObject("image")
            val array = json.getJSONArray("detections")

            val detections = buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val box = item.getJSONObject("boundingBox")

                    add(
                        ApiDetection(
                            category = item.getString("category"),
                            confidence = item.getDouble(
                                "confidence"
                            ).toFloat(),
                            xMin = box.getDouble("xMin").toFloat(),
                            yMin = box.getDouble("yMin").toFloat(),
                            xMax = box.getDouble("xMax").toFloat(),
                            yMax = box.getDouble("yMax").toFloat()
                        )
                    )
                }
            }

            return ApiAnalysis(
                message = json.optString("message"),
                candidateCount = json.getInt("candidate_count"),
                imageWidth = image.getInt("width"),
                imageHeight = image.getInt("height"),
                detections = detections
            )
        }
    }
}

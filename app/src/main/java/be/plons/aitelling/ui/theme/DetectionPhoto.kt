
package be.plons.aitelling.ui.theme

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import be.plons.aitelling.network.ApiAnalysis
import java.io.File

@Composable
fun DetectionPhoto(
    photo: File,
    analysis: ApiAnalysis?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView ->
                imageView.setImageURI(Uri.fromFile(photo))
            }
        )

        if (
            analysis != null &&
            analysis.imageWidth > 0 &&
            analysis.imageHeight > 0
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {

                val imageWidth = analysis.imageWidth.toFloat()
                val imageHeight = analysis.imageHeight.toFloat()

                // Zelfde FIT_CENTER-berekening als de foto.
                val scale = minOf(
                    size.width / imageWidth,
                    size.height / imageHeight
                )

                val displayedWidth = imageWidth * scale
                val displayedHeight = imageHeight * scale

                val offsetX =
                    (size.width - displayedWidth) / 2f

                val offsetY =
                    (size.height - displayedHeight) / 2f

                analysis.detections.forEach { detection ->

                    val left =
                        offsetX +
                                detection.xMin * displayedWidth

                    val top =
                        offsetY +
                                detection.yMin * displayedHeight

                    val right =
                        offsetX +
                                detection.xMax * displayedWidth

                    val bottom =
                        offsetY +
                                detection.yMax * displayedHeight

                    drawRect(
                        color = Color(0xFF00C853),
                        topLeft = Offset(left, top),
                        size = Size(
                            width = (right - left).coerceAtLeast(0f),
                            height = (bottom - top).coerceAtLeast(0f)
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}

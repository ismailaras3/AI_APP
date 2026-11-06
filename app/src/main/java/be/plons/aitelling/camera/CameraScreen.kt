
package be.plons.aitelling.camera

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

@Composable
fun CameraScreen(
    onPhotoTaken: (File) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isTakingPhoto by remember {
        mutableStateOf(false)
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    controller = cameraController

                    try {
                        cameraController.bindToLifecycle(lifecycleOwner)
                    } catch (error: Exception) {
                        errorMessage = error.message
                    }
                }
            }
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Button(
            enabled = !isTakingPhoto,
            onClick = {
                isTakingPhoto = true

                val photoDirectory = File(
                    context.cacheDir,
                    "plons_photos"
                )

                photoDirectory.mkdirs()

                val photoFile = File.createTempFile(
                    "plons_",
                    ".jpg",
                    photoDirectory
                )

                val outputOptions =
                    ImageCapture.OutputFileOptions.Builder(
                        photoFile
                    ).build()

                cameraController.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onImageSaved(
                            output: ImageCapture.OutputFileResults
                        ) {
                            isTakingPhoto = false
                            onPhotoTaken(photoFile)
                        }

                        override fun onError(
                            exception: ImageCaptureException
                        ) {
                            photoFile.delete()
                            isTakingPhoto = false
                            errorMessage =
                                "Foto mislukt: ${exception.message}"
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                if (isTakingPhoto) "Foto verwerken..."
                else "Foto maken"
            )
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Text("Terug")
        }
    }
}

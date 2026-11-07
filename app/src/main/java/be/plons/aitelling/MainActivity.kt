
package be.plons.aitelling

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import be.plons.aitelling.camera.CameraScreen
import java.io.File
import android.widget.ImageView
import be.plons.aitelling.storage.PhotoCacheManager
import be.plons.aitelling.network.PlonsApiClient
import be.plons.aitelling.network.ApiAnalysis
import be.plons.aitelling.ui.theme.DetectionPhoto

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Oude tijdelijke foto's verwijderen bij een nieuwe app-start.
        // TEST: automatische cache-opruiming tijdelijk uitgeschakeld.
        if (savedInstanceState == null) {
            PhotoCacheManager.clearCache(this)
        }

        setContent {
            MaterialTheme {
                PlonsApp()
            }
        }
    }
}

@Composable
fun PlonsApp() {

    val context = LocalContext.current

    var selectedPhoto by remember {
        mutableStateOf<File?>(null)
    }

    var showCamera by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf("Selecteer eerst een afbeelding.")
    }
    var isUploading by remember {
        mutableStateOf(false)
    }

    var analysisResult by remember {
        mutableStateOf<ApiAnalysis?>(null)
    }

    // Verwijdert de vorige tijdelijke foto.
    fun replacePhoto(newPhoto: File) {
        selectedPhoto?.delete()
        selectedPhoto = newPhoto
        analysisResult = null
        message = "Foto geladen en tijdelijk opgeslagen."

    }

    // Vraagt cameratoestemming wanneer nodig.
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                showCamera = true
            } else {
                message = "Cameratoestemming geweigerd."
            }
        }

    // Selecteert een foto en maakt een tijdelijke kopie.
    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                var tempFile: File? = null

                try {
                    val photoDirectory = File(
                        context.cacheDir,
                        "plons_photos"
                    )
                    photoDirectory.mkdirs()

                    val newFile = File.createTempFile(
                        "plons_gallery_",
                        ".jpg",
                        photoDirectory
                    )
                    tempFile = newFile

                    val inputStream =
                        context.contentResolver.openInputStream(uri)
                            ?: error("Kan foto niet openen")

                    inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    replacePhoto(tempFile)

                } catch (error: Exception) {
                    tempFile?.delete()
                    message = "Foto laden mislukt: ${error.message}"
                }
            }
        }

    if (showCamera) {

        CameraScreen(
            onPhotoTaken = { photo ->
                replacePhoto(photo)
                showCamera = false
            },
            onBack = {
                showCamera = false
            }
        )

    } else {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F8F7)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    text = "PLONS AI",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF087F68)
                )

                Text(
                    text = "Macroplastic Detection",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Plastic automatisch tellen",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF17352E)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Maak een foto of selecteer een " +
                            "afbeelding om plastic afval te analyseren.",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )

                Spacer(Modifier.height(28.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {

                    val photo = selectedPhoto

                    if (photo != null) {

                        DetectionPhoto(
                            photo = photo,
                            analysis = analysisResult,
                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement =
                                Arrangement.Center,
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Geen foto geselecteerd",
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val hasPermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            showCamera = true
                        } else {
                            permissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF087F68)
                    )
                ) {
                    Text("Camera openen", fontSize = 17.sp)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Foto selecteren", fontSize = 17.sp)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val photo = selectedPhoto

                        if (photo != null && !isUploading) {
                            isUploading = true
                            message = "Foto wordt verzonden..."

                            PlonsApiClient.analysePhoto(photo) { result ->
                                isUploading = false

                                if (selectedPhoto == photo) {
                                    result.onSuccess { analysis ->
                                        analysisResult = analysis
                                        message = analysis.message
                                    }.onFailure { error ->
                                        analysisResult = null
                                        message = "Analyse mislukt: ${error.message}"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF087F68)
                    )
                ) {
                    Text(
                        text = if (isUploading) {
                            "Foto verzenden..."
                        } else {
                            "Foto analyseren"
                        }
                    )
                }


                if (selectedPhoto != null) {

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            selectedPhoto?.delete()
                            selectedPhoto = null
                            analysisResult = null
                            message = "Tijdelijke foto verwijderd."
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Foto verwijderen")
                    }
                }

                analysisResult?.let { analysis ->

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Mogelijke objecten: ${analysis.candidateCount}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF087F68)
                    )

                    Spacer(Modifier.height(8.dp))

                    analysis.detections.forEachIndexed { index, detection ->

                        val percentage = detection.confidence * 100f

                        Text(
                            text = "${index + 1}. ${detection.category} - " +
                                    "%.1f".format(percentage) + "%",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Spacer(Modifier.height(4.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Kandidaatobjecten: plasticmateriaal " +
                                "nog niet door AI bevestigd.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }


                Spacer(Modifier.height(24.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF087F68)
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Plons AI - Development v0.2",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

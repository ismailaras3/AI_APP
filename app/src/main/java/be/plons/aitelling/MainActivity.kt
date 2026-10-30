
package be.plons.aitelling

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PlonsApp()
            }
        }
    }
}

@Composable
fun PlonsApp() {

    var selectedImage by remember {
        mutableStateOf<Uri?>(null)
    }

    var cameraImage by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val context = LocalContext.current

    // Foto selecteren uit de galerij
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri
            cameraImage = null
        }
    }

    // Foto maken met de camera-app
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            cameraImage = bitmap
            selectedImage = null
        }
    }

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

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Plastic automatisch tellen",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF17352E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Maak een foto of selecteer een " +
                        "afbeelding om plastic afval te analyseren.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Fotovoorbeeld
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {

                if (selectedImage != null || cameraImage != null) {

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            ImageView(context).apply {
                                scaleType =
                                    ImageView.ScaleType.FIT_CENTER
                            }
                        },
                        update = { imageView ->
                            if (selectedImage != null) {
                                imageView.setImageURI(selectedImage)
                            } else {
                                imageView.setImageBitmap(cameraImage)
                            }
                        }
                    )

                } else {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Geen foto geselecteerd",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    cameraLauncher.launch(null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF087F68)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Camera openen",
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Foto selecteren",
                    fontSize = 17.sp,
                    color = Color(0xFF087F68)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedImage != null || cameraImage != null) {
                Text(
                    text = "Foto geladen. Klaar voor AI-analyse " +
                            "zodra het AI-model is aangesloten.",
                    fontSize = 14.sp,
                    color = Color(0xFF087F68)
                )
            } else {
                Text(
                    text = "Selecteer eerst een afbeelding.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Plons AI - Development v0.1",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

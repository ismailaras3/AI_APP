
package be.plons.aitelling.network

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object PlonsApiClient {

    private const val API_URL =
        "http://127.0.0.1:8000/detect"


    fun testConnection(
        onResult: (Result<String>) -> Unit
    ) {
        Thread {
            val result = runCatching {
                val connection = URL(
                    "http://127.0.0.1:8000/health"
                ).openConnection() as HttpURLConnection

                try {
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode

                    val response = connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    check(responseCode == 200) {
                        "HTTP $responseCode"
                    }

                    response
                } finally {
                    connection.disconnect()
                }
            }

            Handler(Looper.getMainLooper()).post {
                onResult(result)
            }
        }.start()
    }


    fun analysePhoto(
        photo: File,
        onResult: (Result<ApiAnalysis>) -> Unit
    ) {
        Thread {
            val result = runCatching {
                uploadPhoto(photo)
            }

            Handler(Looper.getMainLooper()).post {
                onResult(result)
            }
        }.start()
    }

    private fun uploadPhoto(photo: File): ApiAnalysis {
        require(photo.isFile) {
            "De tijdelijke foto bestaat niet meer."
        }

        val boundary = "PlonsAIBoundary"
        val connection = URL(API_URL)
            .openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 120_000
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=$boundary"
            )
            connection.setChunkedStreamingMode(8192)

            val isPng = photo.inputStream().use { stream ->
                val signature = ByteArray(8)
                val count = stream.read(signature)
                count == 8 &&
                        signature.contentEquals(
                            byteArrayOf(
                                0x89.toByte(),
                                0x50,
                                0x4E,
                                0x47,
                                0x0D,
                                0x0A,
                                0x1A,
                                0x0A
                            )
                        )
            }

            val mimeType = if (isPng) {
                "image/png"
            } else {
                "image/jpeg"
            }

            val filename = if (isPng) {
                "plons_photo.png"
            } else {
                "plons_photo.jpg"
            }

            connection.outputStream.use { output ->
                val header = (
                        "--$boundary\r\n" +
                                "Content-Disposition: form-data; " +
                                "name=\"file\"; filename=\"$filename\"\r\n" +
                                "Content-Type: $mimeType\r\n\r\n"
                        )

                output.write(header.toByteArray())

                photo.inputStream().use { input ->
                    input.copyTo(output)
                }

                output.write(
                    "\r\n--$boundary--\r\n".toByteArray()
                )
            }

            val statusCode = connection.responseCode

            val responseStream =
                if (statusCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val response = responseStream
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()

            if (statusCode !in 200..299) {
                error(
                    "Serverfout $statusCode: $response"
                )
            }

            return ApiAnalysis.fromJson(JSONObject(response))

        } finally {
            connection.disconnect()
        }
    }
}

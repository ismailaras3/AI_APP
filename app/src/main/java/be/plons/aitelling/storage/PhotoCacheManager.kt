
package be.plons.aitelling.storage

import android.content.Context
import android.util.Log
import java.io.File

object PhotoCacheManager {

    private const val FOLDER_NAME = "plons_photos"

    fun getPhotoDirectory(context: Context): File {
        val directory = File(context.cacheDir, FOLDER_NAME)

        if (!directory.exists()) {
            check(directory.mkdirs()) {
                "Kan tijdelijke fotomap niet maken."
            }
        }

        return directory
    }

    fun deletePhoto(photo: File?): Boolean {
        if (photo == null || !photo.exists()) {
            return true
        }

        return photo.delete()
    }

    fun clearCache(context: Context) {
        val directory = File(context.cacheDir, FOLDER_NAME)

        if (!directory.exists()) return

        directory.listFiles()?.forEach { file ->
            if (file.isFile && !file.delete()) {
                Log.w(
                    "PlonsAI",
                    "Tijdelijke foto kon niet verwijderd worden."
                )
            }
        }
    }
}

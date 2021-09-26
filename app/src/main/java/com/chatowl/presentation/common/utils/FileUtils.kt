package com.chatowl.presentation.common.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.chatowl.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileUtils (private val context: Context, mediaFolder: String) {

    val mediaPath = File(context.getExternalFilesDir(null), mediaFolder)

    fun copyFile(sourceUri: Uri): Uri {
        val destinationFile = getTempFilePath()
        val inputStream: InputStream = context.contentResolver.openInputStream(sourceUri)
            ?: throw NoSuchFileException(File(sourceUri.toString()))
        val fileOutputStream = FileOutputStream(destinationFile)
        inputStream.copyTo(fileOutputStream, DEFAULT_BUFFER_SIZE)
        fileOutputStream.close()
        inputStream.close()
        return getFileUri(destinationFile)
    }

    fun getAudioFilePath(): File {
        if (!mediaPath.exists()) {
            mediaPath.mkdirs()
        }
        val fileName = getStringTimestampForMedia() + ".3gpp"
        return File(mediaPath, fileName)
    }

    fun getTempFilePath(): File {
        if (!mediaPath.exists()) {
            mediaPath.mkdirs()
        }
        val fileName = getStringTimestampForMedia() + ".jpeg"
        return File(mediaPath, fileName)
    }


    fun getFileUri(file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    companion object {
        public fun fileExists(uri: Uri?): Boolean {
            if (uri == null) {
                return false
            }

            return File(uri.path).exists()

        }
    }
}
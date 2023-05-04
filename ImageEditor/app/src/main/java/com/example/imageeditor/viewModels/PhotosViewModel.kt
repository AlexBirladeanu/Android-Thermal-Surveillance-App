package com.example.imageeditor.viewModels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PhotosViewModel: ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPhotoTitle(takenAt: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH:mm:ss");
        val instant = Instant.ofEpochMilli(takenAt)
        val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return "IMG_" + formatter.format(date)
    }

    fun saveToGallery(context: Context, bitmap: Bitmap, fileName: String) {
        val albumName = "SeeYouApp"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, fileName)
            write(FileOutputStream(image))
        }
    }
}
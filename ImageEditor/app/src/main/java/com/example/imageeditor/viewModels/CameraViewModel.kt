package com.example.imageeditor.viewModels

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageeditor.MainActivity
import com.example.imageeditor.database.entity.Photo
import com.example.imageeditor.database.entity.Recording
import com.example.imageeditor.utils.AppSettingsProvider
import com.example.imageeditor.utils.BitmapConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Timestamp
import kotlin.properties.Delegates

class CameraViewModel : ViewModel() {

    private val recordingsDao = MainActivity.database.recordingDao
    private lateinit var currentRecording: Recording
    private var lastPhotoTimestamp: Long = 0

    fun startRecording() {
        val currentTime = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            recordingsDao.insertRecording(
                Recording(
                    recordingId = null,
                    startedAt = currentTime,
                    endedAt = null
                )
            )
            currentRecording =
                recordingsDao.getAllRecordings().first { it.startedAt == currentTime }
            notifyRecordingsViewModel()
        }
    }

    fun stopRecording() {
        val currentTime = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            recordingsDao.insertRecording(
                Recording(
                    recordingId = currentRecording.recordingId,
                    startedAt = currentRecording.startedAt,
                    endedAt = currentTime
                )
            )
            notifyRecordingsViewModel()
        }
    }

    fun insertPhoto(bitmap: Bitmap, peopleNr: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val timeBetweenPhotos = currentTime - lastPhotoTimestamp
            if (timeBetweenPhotos > AppSettingsProvider.getTimeBetweenPhotos()) {
                val encodedBitmap = BitmapConverter.convertBitmapToString(bitmap)
                recordingsDao.insertPhoto(
                    Photo(
                        id = null,
                        takenAt = currentTime,
                        peopleNr = peopleNr,
                        bitmap = encodedBitmap,
                        recordingId = currentRecording.recordingId!!
                    )
                )
                lastPhotoTimestamp = currentTime
            }
        }
    }

    private fun notifyRecordingsViewModel() {
        RecordingsViewModel.eventLiveData.postValue(Unit)
    }
}
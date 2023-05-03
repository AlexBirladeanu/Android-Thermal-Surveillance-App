package com.example.imageeditor.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageeditor.MainActivity
import com.example.imageeditor.database.entity.Recording
import com.example.imageeditor.database.entity.relations.RecordingWithPhotos
import com.example.imageeditor.model.SelectableRecording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class RecordingsViewModel : ViewModel() {
    private val recordingsDao = MainActivity.database.recordingDao

    private val _recordingsList: MutableStateFlow<List<SelectableRecording>> =
        MutableStateFlow(listOf())
    val recordingsList = _recordingsList.asStateFlow()

    init {
        resetList()
        eventLiveData.observeForever { resetList() }
    }

    private fun resetList() {
        _recordingsList.value = listOf()
        viewModelScope.launch(Dispatchers.IO) {
            recordingsDao.apply {
                _recordingsList.value = getAllRecordings().map { SelectableRecording(RecordingWithPhotos(it, listOf()), false) }.sortedByDescending { it.recordingWithPhotos.recording.recordingId }

                val newList = mutableListOf<SelectableRecording>()
                newList.addAll(getAllRecordings().map { SelectableRecording(getRecordingWithPhotos(it.recordingId!!).first(), false) })
                _recordingsList.value = newList.sortedByDescending { it.recordingWithPhotos.recording.recordingId }
            }
        }
    }

    fun deleteRecordingWithPhotos(recordingWithPhotos: RecordingWithPhotos) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = mutableListOf<SelectableRecording>()
            newList.addAll(_recordingsList.value)
            newList.remove(_recordingsList.value.first { it.recordingWithPhotos == recordingWithPhotos })
            _recordingsList.value = newList

            recordingWithPhotos.photos.forEach {
                recordingsDao.deletePhoto(it)
            }
            recordingsDao.deleteRecording(recordingWithPhotos.recording)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = mutableListOf<SelectableRecording>()
            newList.addAll(_recordingsList.value.filter { !it.isSelected })
            _recordingsList.value = newList

            _recordingsList.value.filter { it.isSelected }.forEach {
                it.recordingWithPhotos.photos.forEach { photo ->
                    recordingsDao.deletePhoto(photo)
                }
                recordingsDao.deleteRecording(it.recordingWithPhotos.recording)
            }
        }
    }

    fun onRecordingClicked(selectableRecording: SelectableRecording) {
        val newList = mutableListOf<SelectableRecording>()
        newList.addAll(_recordingsList.value)
        val index = newList.indexOf(selectableRecording)
        newList[index] = selectableRecording.copy(isSelected = !selectableRecording.isSelected)
        _recordingsList.value = newList
    }

    fun deselectAll() {
        val newList = mutableListOf<SelectableRecording>()
        newList.addAll(_recordingsList.value)
        _recordingsList.value.filter{it.isSelected}.forEach {
            val index = newList.indexOf(it)
            newList[index] = it.copy(isSelected = false)
        }
        _recordingsList.value = newList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isDateValid(localDate: LocalDate): Boolean {
        _recordingsList.value.forEach {
            if (isDateDuringRecording(localDate, it.recordingWithPhotos.recording)) {
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateDuringRecording(localDate: LocalDate, recording: Recording): Boolean {
        recording.endedAt?.let { endedAtMillis ->
            val startedAt = Instant.ofEpochMilli(recording.startedAt)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            val endedAt = Instant.ofEpochMilli(endedAtMillis)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            return localDate.isEqual(startedAt) || localDate.isEqual(endedAt) || (localDate.isAfter(startedAt) && localDate.isBefore(endedAt))
        }
        return false
    }

    fun filterByMinDetections(minDetections: Int?) {
        minDetections?.let { nonNullMinDetections ->
            viewModelScope.launch(Dispatchers.IO) {
                recordingsDao.apply {
                    val newList = mutableListOf<SelectableRecording>()
                    getAllRecordings().map { SelectableRecording(getRecordingWithPhotos(it.recordingId!!).first(), false) }.forEach {
                        var detections = 0
                        it.recordingWithPhotos.photos.forEach { photo ->
                            detections += photo.peopleNr
                        }
                        if (detections >= nonNullMinDetections) {
                            newList.add(it)
                        }
                    }
                    _recordingsList.value = newList.reversed()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterByDate(localDate: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            recordingsDao.apply {
                _recordingsList.value = getAllRecordings().map { SelectableRecording(getRecordingWithPhotos(it.recordingId!!).first(), false) }.filter {
                    isDateDuringRecording(localDate, it.recordingWithPhotos.recording)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterByMinDetectionsAndDate(minDetections: Int?, localDate: LocalDate) {
        minDetections?.let { nonNullMinDetections ->
            viewModelScope.launch(Dispatchers.IO) {
                recordingsDao.apply {
                    val newList = mutableListOf<SelectableRecording>()
                    getAllRecordings().map { SelectableRecording(getRecordingWithPhotos(it.recordingId!!).first(), false) }.forEach {
                        var detections = 0
                        it.recordingWithPhotos.photos.forEach { photo ->
                            detections += photo.peopleNr
                        }
                        if (detections >= nonNullMinDetections) {
                            newList.add(it)
                        }
                    }
                    newList.removeIf{
                        !isDateDuringRecording(localDate, it.recordingWithPhotos.recording)
                    }
                    _recordingsList.value = newList.reversed()
                }
            }
        }
    }

    fun clearFilters() {
        resetList()
    }

    companion object {
        val eventLiveData = MutableLiveData<Unit>()
        lateinit var selectedRecordingWithPhotos: RecordingWithPhotos
    }
}
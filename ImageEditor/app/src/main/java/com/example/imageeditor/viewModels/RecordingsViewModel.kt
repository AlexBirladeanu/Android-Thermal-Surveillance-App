package com.example.imageeditor.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageeditor.MainActivity
import com.example.imageeditor.database.entity.relations.RecordingWithPhotos
import com.example.imageeditor.model.SelectableRecording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

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

    companion object {
        val eventLiveData = MutableLiveData<Unit>()
        lateinit var selectedRecordingWithPhotos: RecordingWithPhotos
    }
}
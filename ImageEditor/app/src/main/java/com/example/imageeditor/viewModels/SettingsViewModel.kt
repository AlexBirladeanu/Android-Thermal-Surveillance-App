package com.example.imageeditor.viewModels

import androidx.lifecycle.ViewModel
import com.example.imageeditor.utils.AppSettingsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel: ViewModel() {

    private val _isVibrationEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isVibrationEnabled = _isVibrationEnabled.asStateFlow()

    private val _isSoundEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _timeBetweenPhotos: MutableStateFlow<Int> = MutableStateFlow(1)
    val timeBetweenPhotos = _timeBetweenPhotos.asStateFlow()

    private val _isBodyMergeEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isBodyMergeEnabled = _isBodyMergeEnabled.asStateFlow()

    private val _autoStart: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val autoStart = _autoStart.asStateFlow()

    private val _detectPeople: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val detectPeople = _detectPeople.asStateFlow()

    init {
        _isVibrationEnabled.value = AppSettingsProvider.getVibrations()
        _isSoundEnabled.value = AppSettingsProvider.getSoundNotifications()
        _timeBetweenPhotos.value = (AppSettingsProvider.getTimeBetweenPhotos() / 1000).toInt()
        _isBodyMergeEnabled.value = AppSettingsProvider.isBodyMergeEnabled()
        _autoStart.value = AppSettingsProvider.getAutoStart()
        _detectPeople.value = AppSettingsProvider.getDetectPeople()
    }

    fun updateVibration(isEnabled: Boolean) {
        _isVibrationEnabled.value = isEnabled
        AppSettingsProvider.setVibrations(isEnabled)
    }

    fun updateSoundNotifications(isEnabled: Boolean) {
        _isSoundEnabled.value = isEnabled
        AppSettingsProvider.setSoundNotifications(isEnabled)
    }

    fun updateTimeBetweenPhotos(seconds: Int) {
        _timeBetweenPhotos.value = seconds
        val milliseconds = (seconds * 1000).toLong()
        AppSettingsProvider.setTimeBetweenPhotos(milliseconds)
    }

    fun updateIsBodyMergeEnabled(isEnabled: Boolean) {
        _isBodyMergeEnabled.value = isEnabled
        AppSettingsProvider.setIsBodyMergeEnabled(isEnabled)
    }

    fun updateAutoStart(isEnabled: Boolean) {
        _autoStart.value = isEnabled
        AppSettingsProvider.setAutoStart(isEnabled)
        CameraViewModel.autoStartChangedEvent.postValue(Unit)
    }

    fun updateDetectPeople(isEnabled: Boolean) {
        _detectPeople.value = isEnabled
        AppSettingsProvider.setDetectPeople(isEnabled)
    }
}
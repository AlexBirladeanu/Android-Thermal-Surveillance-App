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

    init {
        _isVibrationEnabled.value = AppSettingsProvider.getVibrations()
        _isSoundEnabled.value = AppSettingsProvider.getSoundNotifications()
        _timeBetweenPhotos.value = (AppSettingsProvider.getTimeBetweenPhotos() / 1000).toInt()
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
}
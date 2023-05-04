package com.example.imageeditor.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

object AppSettingsProvider {
    private const val KEY_VIBRATIONS = "Vibrations"
    private const val KEY_SOUND_NOTIFICATIONS = "Sound notifications"
    private const val KEY_TIME_BETWEEN_PHOTOS = "Time between photos"
    private const val KEY_BODY_MERGE_ENABLED = "Enable body merge"

    private const val DEFAULT_VIBRATIONS_VALUE = true
    private const val DEFAULT_SOUND_NOTIFICATIONS_VALUE = true
    private const val DEFAULT_TIME_BETWEEN_PHOTOS_VALUE = 1000L
    private const val DEFAULT_BODY_MERGE_ENABLED = false

    private lateinit var sharedPreferences: SharedPreferences

    fun initializeSharedPreferences(activity: AppCompatActivity) {
        sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)
    }

    fun setVibrations(value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_VIBRATIONS, value)
            apply()
        }
    }

    fun setSoundNotifications(value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_SOUND_NOTIFICATIONS, value)
            apply()
        }
    }

    fun setTimeBetweenPhotos(value: Long) {
        with(sharedPreferences.edit()) {
            putLong(KEY_TIME_BETWEEN_PHOTOS, value)
            apply()
        }
    }

    fun setIsBodyMergeEnabled(value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_BODY_MERGE_ENABLED, value)
            apply()
        }
    }

    fun getVibrations(): Boolean =
        sharedPreferences.getBoolean(KEY_VIBRATIONS, DEFAULT_VIBRATIONS_VALUE)

    fun getSoundNotifications(): Boolean =
        sharedPreferences.getBoolean(KEY_SOUND_NOTIFICATIONS, DEFAULT_SOUND_NOTIFICATIONS_VALUE)

    fun getTimeBetweenPhotos(): Long =
        sharedPreferences.getLong(KEY_TIME_BETWEEN_PHOTOS, DEFAULT_TIME_BETWEEN_PHOTOS_VALUE)

    fun isBodyMergeEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_BODY_MERGE_ENABLED, DEFAULT_BODY_MERGE_ENABLED)
}
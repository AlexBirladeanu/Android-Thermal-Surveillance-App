package com.example.imageeditor.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.unusedapprestrictions.IUnusedAppRestrictionsBackportCallback.Default

object AppSettingsProvider {
    private const val KEY_VIBRATIONS = "Vibrations"
    private const val KEY_SOUND_NOTIFICATIONS = "Sound notifications"
    private const val KEY_TIME_BETWEEN_PHOTOS = "Time between photos"
    private const val KEY_BODY_MERGE_ENABLED = "Enable body merge"
    private const val KEY_AUTO_START = "Start/Stop"
    private const val KEY_DETECT_PEOPLE = "Detection Mode"
    private const val KEY_SAVE_RECORDINGS_DATA = "Save recordings Data"
    private const val KEY_DEBUG_OPTION = "Debug Option"

    private const val DEFAULT_VIBRATIONS_VALUE = true
    private const val DEFAULT_SOUND_NOTIFICATIONS_VALUE = true
    private const val DEFAULT_TIME_BETWEEN_PHOTOS_VALUE = 1000L
    private const val DEFAULT_BODY_MERGE_ENABLED = false
    private const val DEFAULT_AUTO_START = false
    private const val DEFAULT_DETECT_PEOPLE = true
    private const val DEFAULT_SAVE_RECORDINGS_DATA_VALUE = true
    private val DEFAULT_DEBUG_OPTION = DebugOptionType.OFF.ordinal

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

    fun setAutoStart(value: Boolean) {
        with (sharedPreferences.edit()) {
            putBoolean(KEY_AUTO_START, value)
            apply()
        }
    }

    fun setDetectPeople(value: Boolean) {
        with (sharedPreferences.edit()) {
            putBoolean(KEY_DETECT_PEOPLE, value)
            apply()
        }
    }

    fun setSaveRecordingsData(value: Boolean) {
        with (sharedPreferences.edit()) {
            putBoolean(KEY_SAVE_RECORDINGS_DATA, value)
            apply()
        }
    }

    fun setDebugOption(value: Int) {
        with (sharedPreferences.edit()) {
            putInt(KEY_DEBUG_OPTION, value)
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

    fun getAutoStart(): Boolean =
        sharedPreferences.getBoolean(KEY_AUTO_START, DEFAULT_AUTO_START)

    fun getDetectPeople(): Boolean =
        sharedPreferences.getBoolean(KEY_DETECT_PEOPLE, DEFAULT_DETECT_PEOPLE)

    fun isSaveRecordingsDataEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_SAVE_RECORDINGS_DATA, DEFAULT_SAVE_RECORDINGS_DATA_VALUE)

    fun getDebugOption(): Int =
        sharedPreferences.getInt(KEY_DEBUG_OPTION, DEFAULT_DEBUG_OPTION)
}

enum class DebugOptionType {
    OFF,
    SHOW_ALL_CLUSTERS,
    SHOW_FIRST_CLUSTER
}
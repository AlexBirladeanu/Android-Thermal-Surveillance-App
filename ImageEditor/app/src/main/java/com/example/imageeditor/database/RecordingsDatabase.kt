package com.example.imageeditor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.imageeditor.database.dao.RecordingDao
import com.example.imageeditor.database.entity.Photo
import com.example.imageeditor.database.entity.Recording

@Database(
    entities = [
        Photo::class,
        Recording::class
    ],
    version = 1
)
abstract class RecordingsDatabase: RoomDatabase() {

    abstract val recordingDao: RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: RecordingsDatabase? = null

        fun getInstance(context: Context): RecordingsDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecordingsDatabase::class.java,
                    "recordings_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
package com.example.imageeditor.database.dao

import androidx.room.*
import com.example.imageeditor.database.entity.Photo
import com.example.imageeditor.database.entity.Recording
import com.example.imageeditor.database.entity.relations.RecordingWithPhotos

@Dao
interface RecordingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)

    @Delete
    fun deleteRecording(recording: Recording)

    @Delete
    fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM recording")
    fun getAllRecordings(): List<Recording>

    @Transaction
    @Query(value = "SELECT * FROM recording WHERE recordingId = :recordingId")
    suspend fun getRecordingWithPhotos(recordingId: Int): List<RecordingWithPhotos>
}
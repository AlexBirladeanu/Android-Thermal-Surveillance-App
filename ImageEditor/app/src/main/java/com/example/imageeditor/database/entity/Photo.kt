package com.example.imageeditor.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "taken_at") val takenAt: Long,
    @ColumnInfo(name = "people_nr") val peopleNr: Int,
    @ColumnInfo(name = "bitmap") val bitmap: String,
    @ColumnInfo(name = "recordingId") val recordingId: Int
)
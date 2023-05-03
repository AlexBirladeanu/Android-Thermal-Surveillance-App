package com.example.imageeditor.database.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.imageeditor.database.entity.Photo
import com.example.imageeditor.database.entity.Recording

data class RecordingWithPhotos(
    @Embedded val recording: Recording,
    @Relation(
        parentColumn = "recordingId",
        entityColumn = "recordingId"
    )
    val photos: List<Photo>
)
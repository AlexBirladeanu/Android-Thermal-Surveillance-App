package com.example.imageeditor.model

import com.example.imageeditor.database.entity.relations.RecordingWithPhotos

data class SelectableRecording(
    val recordingWithPhotos: RecordingWithPhotos,
    val isSelected: Boolean = false
)
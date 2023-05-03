package com.example.imageeditor.ui.views

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imageeditor.database.entity.Photo
import com.example.imageeditor.utils.BitmapConverter
import com.example.imageeditor.viewModels.RecordingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordingDetailsView(
    onScreenClose: () -> Unit,
) {
    val recordingWithPhotos = RecordingsViewModel.selectedRecordingWithPhotos
    BackHandler(onBack = onScreenClose)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recording " + recordingWithPhotos.recording.recordingId,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                backgroundColor = Color.Black,
                navigationIcon = {
                    IconButton(onClick = onScreenClose) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) {
        val selectedBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedBitmap.value != null) {
                FullScreenImage(
                    imageBitmap = selectedBitmap.value!!,
                    onClick = {
                        selectedBitmap.value = null
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                        .padding(it)
                        .padding(vertical = 16.dp)
                ) {
                    items(recordingWithPhotos.photos) {
                        PhotoCard(it) {
                            selectedBitmap.value = it
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PhotoCard(
    photo: Photo,
    onClick: (ImageBitmap) -> Unit
) {
    val bitmap = BitmapConverter.convertStringToBitmap(photo.bitmap)!!.asImageBitmap()
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .background(Color.Black)
            .padding(8.dp)
            .clickable {
                onClick(bitmap)
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(Color.Black)
                .padding(4.dp)
        ) {
            Image(
                bitmap = bitmap,
                modifier = Modifier
                    .rotate(-90f)
                    .size(150.dp),
                contentDescription = null
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 4.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = photo.peopleNr.toString())
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val formattedTime = formatter.format(photo.takenAt)
                Text(text = formattedTime)
            }

        }
    }
}

@Composable
private fun FullScreenImage(
    imageBitmap: ImageBitmap,
    onClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }

    Image(
        bitmap = imageBitmap,
        modifier = Modifier
            .rotate(-90f)
            .fillMaxSize()
            .clickable { onClick() }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale *= zoom
                }
            },
        contentDescription = null,
    )
}
package com.example.imageeditor.ui.views

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imageeditor.database.entity.Photo
import com.example.imageeditor.utils.BitmapConverter
import com.example.imageeditor.viewModels.PhotosViewModel
import com.example.imageeditor.viewModels.RecordingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PhotosView(
    viewModel: PhotosViewModel = PhotosViewModel(),
    onScreenClose: () -> Unit,
) {
    val context = LocalContext.current
    val permissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
        //    Toast.makeText(context, "Permission granted = $isGranted", Toast.LENGTH_SHORT).show()
        }
    )
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
        var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedPhoto != null) {
                FullScreenImage(
                    photo = selectedPhoto!!,
                    photoTitle = viewModel.getPhotoTitle(selectedPhoto!!.takenAt),
                    onClick = {
                        selectedPhoto = null
                    },
                    onSaveButtonClick = { fileName ->
                        viewModel.saveToGallery(context, BitmapConverter.convertStringToBitmap(
                            selectedPhoto!!.bitmap)!!, fileName)
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
                            selectedPhoto = it
                            permissionResultLauncher.launch(
                                WRITE_EXTERNAL_STORAGE
                            )
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
    onClick: () -> Unit
) {
    val bitmap = BitmapConverter.convertStringToBitmap(photo.bitmap)!!
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .background(Color.Black)
            .padding(8.dp)
            .clickable {
                onClick()
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
                bitmap = bitmap.asImageBitmap(),
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
    photo: Photo,
    photoTitle: String,
    onClick: () -> Unit,
    onSaveButtonClick: (String) -> Unit
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    Column(
        modifier = Modifier.padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = {
                showExportDialog = true
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Save to Gallery",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Share,
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null
                )
            }
        }

        Image(
            bitmap = BitmapConverter.convertStringToBitmap(photo.bitmap)!!.asImageBitmap(),
            modifier = Modifier
                .rotate(-90f)
                .fillMaxSize()
                .clickable { onClick() },
//            .graphicsLayer(
//                scaleX = scale,
//                scaleY = scale
//            )
//            .pointerInput(Unit) {
//                detectTransformGestures { _, _, zoom, _ ->
//                    scale *= zoom
//                }
//            }
            contentDescription = null,
        )
    }
    if (showExportDialog) {
        AlertDialog(
            title = {
              Text(text = "Are you sure you want to add $photoTitle to Photos?", fontSize = 18.sp)
            },
            shape = RoundedCornerShape(12.dp),
            onDismissRequest = { showExportDialog = false },
            confirmButton = {
                OutlinedButton(
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp, end = 16.dp),
                    onClick = {
                        onSaveButtonClick(photoTitle)
                        showExportDialog = false
                    }) {
                    Text(text = "Yes")
                }
            }
        )
    }
}
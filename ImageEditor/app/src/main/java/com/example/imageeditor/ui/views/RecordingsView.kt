package com.example.imageeditor.ui.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.imageeditor.R
import com.example.imageeditor.database.entity.relations.RecordingWithPhotos
import com.example.imageeditor.viewModels.RecordingsViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.DateFormat;
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordingsView(
    viewModel: RecordingsViewModel = RecordingsViewModel(),
    navigateToDetails: (RecordingWithPhotos) -> Unit
) {
    var isInSelectMode by remember { mutableStateOf(false) }
    var minDetections by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    val recordings = viewModel.recordingsList.collectAsState()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = "Recordings",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = Color.White,
                fontSize = 22.sp
            )
        }, backgroundColor = Color.Black, actions = {
            if (!isInSelectMode) {
                Text(text = "Select",
                    color = Color.White,
                    modifier = Modifier
                        .padding(end = 24.dp, bottom = 2.dp)
                        .clickable { isInSelectMode = true })
            } else {
                if (recordings.value.any { it.isSelected }) {
                    Text(text = "Cancel",
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 2.dp)
                            .clickable {
                                viewModel.deselectAll()
                                isInSelectMode = false
                            })
                    Text(text = "Delete",
                        color = Color.Red,
                        modifier = Modifier
                            .padding(end = 24.dp, bottom = 2.dp)
                            .clickable {
                                viewModel.deleteSelected()
                                isInSelectMode = false
                            })
                } else {
                    Text(text = "Cancel",
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 24.dp, bottom = 2.dp)
                            .clickable {
                                viewModel.deselectAll()
                                isInSelectMode = false
                            })
                }
            }
        })
    }) {
        val backgroundColor = Color.Black
        Card(
            modifier = Modifier
                .background(backgroundColor)
                .padding(it)
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            var showFilters by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = Modifier
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                item {
                    Column {
                        FiltersButton(
                            isExpanded = showFilters,
                            onClick = {
                                showFilters = !showFilters
                            }
                        )
                        if (showFilters) {
                            Filters(
                                minDetections = minDetections,
                                dateString = dateString,
                                updateMinDetections = {
                                    minDetections = it
                                },
                                updateDateString = {
                                    dateString = it
                                },
                                onDone = { minDetections, localDate ->
                                    if (minDetections.isNotEmpty() && dateString.isEmpty()) {
                                        viewModel.filterByMinDetections(minDetections.toIntOrNull())
                                    }
                                    if (dateString.isNotEmpty() && minDetections.isEmpty()) {
                                        viewModel.filterByDate(localDate)
                                    }
                                    if (dateString.isNotEmpty() && minDetections.isNotEmpty()) {
                                        viewModel.filterByMinDetectionsAndDate(
                                            minDetections.toIntOrNull(),
                                            localDate
                                        )
                                    }
                                    if (minDetections.isEmpty() && dateString.isEmpty()) {
                                        viewModel.clearFilters()
                                    }
                                }
                            )
                        }
                    }
                }

                items(recordings.value) { selectableRecording ->
                    if (isInSelectMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectableRecording.isSelected,
                                onCheckedChange = {
                                    if (selectableRecording.recordingWithPhotos.recording.endedAt != null) {
                                        viewModel.onRecordingClicked(selectableRecording)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colors.primary
                                )
                            )
                            if (selectableRecording.recordingWithPhotos.recording.endedAt == null) {
                                RecordingInProgressCard(selectableRecording.recordingWithPhotos)
                            } else {
                                RecordingCard(recordingWithPhotos = selectableRecording.recordingWithPhotos,
                                    navigateToDetails = navigateToDetails,
                                    onDelete = {
                                        viewModel.deleteRecordingWithPhotos(it)
                                    })

                            }
                        }
                    } else {
                        if (selectableRecording.recordingWithPhotos.recording.endedAt == null) {
                            RecordingInProgressCard(selectableRecording.recordingWithPhotos)
                        } else {
                            RecordingCard(recordingWithPhotos = selectableRecording.recordingWithPhotos,
                                navigateToDetails = navigateToDetails,
                                onDelete = {
                                    viewModel.deleteRecordingWithPhotos(it)
                                })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingCard(
    recordingWithPhotos: RecordingWithPhotos,
    navigateToDetails: (RecordingWithPhotos) -> Unit,
    onDelete: (RecordingWithPhotos) -> Unit
) {
    var peopleDetected = 0
    val textColor = Color.White
    recordingWithPhotos.photos.forEach {
        peopleDetected += it.peopleNr
    }
    val cardModifier = if (peopleDetected > 0) {
        Modifier
            .background(Color.Black)
            .padding(vertical = 8.dp)
            .clickable {
                navigateToDetails(recordingWithPhotos)
            }
    } else {
        Modifier
            .background(Color.Black)
            .padding(vertical = 8.dp)
    }

    Card(
        modifier = cardModifier,
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.secondary)
                .padding(8.dp)
        ) {
            Text(
                text = "Recording " + recordingWithPhotos.recording.recordingId.toString(),
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Detected:   ", color = textColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = peopleDetected.toString(), color = textColor)
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (peopleDetected > 0) MaterialTheme.colors.primary else textColor
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Started At:   ", color = textColor)
                    Text(text = "Ended At:   ", color = textColor)
                }
                Column {
                    Text(
                        text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(recordingWithPhotos.recording.startedAt), color = textColor
                    )
                    Text(
                        text = if (recordingWithPhotos.recording.endedAt != null) DateFormat.getDateTimeInstance(
                            DateFormat.SHORT, DateFormat.SHORT
                        ).format(recordingWithPhotos.recording.endedAt) else "In progress",
                        color = textColor
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable {
                        onDelete(recordingWithPhotos)
                    })
                if (peopleDetected > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "See photos", color = textColor)
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingInProgressCard(
    recordingWithPhotos: RecordingWithPhotos
) {
    Card(
        modifier = Modifier
            .background(Color.Black)
            .padding(vertical = 8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.secondary)
                .padding(8.dp)
        ) {
            Text(
                text = "Recording " + recordingWithPhotos.recording.recordingId.toString(),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Started At:   ", color = Color.White)
                }
                Column {
                    Text(
                        text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(recordingWithPhotos.recording.startedAt), color = Color.White
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, end = 16.dp, top = 40.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "In Progress",
                    color = MaterialTheme.colors.primary,
                    fontSize = 24.sp
                )
                CircularProgressIndicator()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDialog(
    dialogState: MaterialDialogState,
    onDone: (LocalDate) -> Unit,
    isDateValid: (LocalDate) -> Boolean
) {

    val ld: LocalDate =
        Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault())
            .toLocalDate()


    var pickedDate by remember { mutableStateOf(ld) }

    MaterialDialog(dialogState = dialogState, properties = DialogProperties(
        dismissOnBackPress = true, dismissOnClickOutside = true
    ), buttons = {
        positiveButton(text = "OK", onClick = { onDone(pickedDate) })
        negativeButton(text = "Cancel")
    }) {
        datepicker(initialDate = LocalDate.now(), allowedDateValidator = {
            isDateValid(it)
        }) {
            pickedDate = it
        }
    }
}

@Composable
private fun FiltersButton(
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.ic_filter
                ),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(start = 8.dp, end = 2.dp)
            )
            Text(
                text = "Filters",
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(end = 12.dp)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun Filters(
    minDetections: String,
    dateString: String,
    updateMinDetections: (String) -> Unit,
    updateDateString: (String) -> Unit,
    onDone: (String, LocalDate) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    val dateDialogState = rememberMaterialDialogState()
    val focusManager = LocalFocusManager.current
    Column(
    ) {
        TextField(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            label = {
                Text(text = "Minimum Detections")
            },
            value = minDetections,
            onValueChange = {
                if (it.toIntOrNull() != null || it.isEmpty()) {
                    updateMinDetections(it)
                }
            },
            trailingIcon = {
                Icon(imageVector = Icons.Filled.Person, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                onDone(minDetections, date)
            }),
        )
        TextField(
            label = {
                Text(text = "Date")
            },
            enabled = false,
            value = dateString,
            onValueChange = {},
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .clickable {
                    dateDialogState.show()
                },
            trailingIcon = {
                Icon(imageVector = Icons.Filled.DateRange, contentDescription = null)
            },
            colors = TextFieldDefaults.textFieldColors(
                disabledTrailingIconColor = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.IconOpacity),
                disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
            ),
        )
        if (!(dateString.isEmpty() && minDetections.isEmpty())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            updateMinDetections("")
                            updateDateString("")
                            onDone("", date)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clear Filters",
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }

    CalendarDialog(dialogState = dateDialogState, onDone = {
        date = it
        updateDateString(date.toString())
        onDone(minDetections, date)
    }, isDateValid = {
        //viewModel.isDateValid(it)
        true
    })
}
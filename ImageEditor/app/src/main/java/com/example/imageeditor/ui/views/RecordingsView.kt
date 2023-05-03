package com.example.imageeditor.ui.views

import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.imageeditor.database.entity.relations.RecordingWithPhotos
import com.example.imageeditor.viewModels.RecordingsViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.DateFormat;
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordingsView(
    viewModel: RecordingsViewModel = RecordingsViewModel(),
    navigateToDetails: (RecordingWithPhotos) -> Unit
) {
    var isInSelectMode by remember { mutableStateOf(false) }

    val recordings = viewModel.recordingsList.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recordings",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                backgroundColor = Color.Black,
                actions = {
                    if (!isInSelectMode) {
                        Text(
                            text = "Select",
                            color = Color.White,
                            modifier = Modifier
                                .padding(end = 24.dp, bottom = 2.dp)
                                .clickable { isInSelectMode = true }
                        )
                    } else {
                        if (recordings.value.any { it.isSelected }) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                modifier = Modifier
                                    .padding(end = 16.dp, bottom = 2.dp)
                                    .clickable {
                                        viewModel.deselectAll()
                                        isInSelectMode = false
                                    }
                            )
                            Text(
                                text = "Delete",
                                color = Color.Red,
                                modifier = Modifier
                                    .padding(end = 24.dp, bottom = 2.dp)
                                    .clickable {
                                        viewModel.deleteSelected()
                                        isInSelectMode = false
                                    }
                            )
                        } else {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                modifier = Modifier
                                    .padding(end = 24.dp, bottom = 2.dp)
                                    .clickable {
                                        viewModel.deselectAll()
                                        isInSelectMode = false
                                    }
                            )
                        }
                    }
                }
            )
        }
    ) {
        val backgroundColor = Color.Black
        Card(
            modifier = Modifier
                .background(backgroundColor)
                .padding(it)
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            var pickedDate by remember { mutableStateOf(LocalDate.now())}
            val dateDialogState = rememberMaterialDialogState()
            MaterialDialog(
                dialogState = dateDialogState,
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                buttons = {
                    positiveButton(text = "OK")
                    negativeButton(text = "Cancel")
                }
            ) {
                datepicker(
                    initialDate = LocalDate.now(),
                    title = "Pick a date",
                    colors = DatePickerDefaults.colors(

                    ),
                    allowedDateValidator = {
                        it.dayOfMonth % 2 == 0
                    }
                ) {
                    pickedDate = it
                }

            }
            LazyColumn(
                modifier = Modifier
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = pickedDate.toString(),
                        color = Color.White,
                        fontSize = 22.sp,
                        modifier = Modifier.clickable {
                            dateDialogState.show()
                        }
                    )
                }

                items(recordings.value) { selectableRecording ->
                    if (isInSelectMode) {
                        Row(
                            //modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //var isChecked by remember { mutableStateOf(false) }
                            Checkbox(
                                checked = selectableRecording.isSelected,
                                onCheckedChange = { viewModel.onRecordingClicked(selectableRecording) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colors.primary
                                )
                            )
                            RecordingCard(
                                recordingWithPhotos = selectableRecording.recordingWithPhotos,
                                navigateToDetails = navigateToDetails,
                                onDelete = {
                                    viewModel.deleteRecordingWithPhotos(it)
                                }
                            )
                        }
                    } else {
                        RecordingCard(
                            recordingWithPhotos = selectableRecording.recordingWithPhotos,
                            navigateToDetails = navigateToDetails,
                            onDelete = {
                                viewModel.deleteRecordingWithPhotos(it)
                            }
                        )
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
                            .format(recordingWithPhotos.recording.startedAt),
                        color = textColor
                    )
                    Text(
                        text = if (recordingWithPhotos.recording.endedAt != null) DateFormat.getDateTimeInstance(
                            DateFormat.SHORT,
                            DateFormat.SHORT
                        )
                            .format(recordingWithPhotos.recording.endedAt) else "In progress",
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
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable {
                        onDelete(recordingWithPhotos)
                    }
                )
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
private fun CalendarDialog(

) {
    // Fetching the Local Context
    val mContext = LocalContext.current

    // Declaring integer values
    // for year, month and day
    val mYear: Int
    val mMonth: Int
    val mDay: Int

    // Initializing a Calendar
    val mCalendar = Calendar.getInstance()

    // Fetching current year, month and day
    mYear = mCalendar.get(Calendar.YEAR)
    mMonth = mCalendar.get(Calendar.MONTH)
    mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    mCalendar.time = Date()

    // Declaring a string value to
    // store date in string format
    val mDate = remember { mutableStateOf("") }

    // Declaring DatePickerDialog and setting
    // initial values as current values (present year, month and day)
    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            mDate.value = "$mDayOfMonth/${mMonth + 1}/$mYear"
        }, mYear, mMonth, mDay
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDialog2() {
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    val dateDialogState = rememberMaterialDialogState()
}

@Preview
@Composable
private fun CalendarPreview() {
    CalendarDialog()
}
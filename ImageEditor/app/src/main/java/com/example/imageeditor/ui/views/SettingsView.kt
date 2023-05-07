package com.example.imageeditor.ui.views

import android.util.Log
import android.widget.ToggleButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imageeditor.ui.theme.BabyBlue
import com.example.imageeditor.ui.theme.Blue
import com.example.imageeditor.ui.theme.Orange
import com.example.imageeditor.viewModels.SettingsViewModel

@Composable
fun SettingsView(
    viewModel: SettingsViewModel = SettingsViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                },
                backgroundColor = Color.Black,
            )
        }
    ) {
        val backgroundColor = Color.Black
        val textColor = Color.White
        val switchColors = SwitchDefaults.colors(
            checkedThumbColor = Orange,
            checkedTrackColor = Orange,
            uncheckedThumbColor = BabyBlue,
            uncheckedTrackColor = BabyBlue,
        )
        Card(
            modifier = Modifier
                .background(backgroundColor)
                .padding(it)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            val scrollPosition = rememberLazyListState()
            LazyColumn(
                state = scrollPosition,
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                item {
                    val isVibrationEnabled = viewModel.isVibrationEnabled.collectAsState()
                    val isSoundEnabled = viewModel.isSoundEnabled.collectAsState()
                    val timeBetweenPhotos = viewModel.timeBetweenPhotos.collectAsState()
                    val isBodyMergeEnabled by viewModel.isBodyMergeEnabled.collectAsState()
                    val isAutoStart by viewModel.autoStart.collectAsState()
                    val isDetectPeopleEnabled by viewModel.detectPeople.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibrations",
                            color = textColor,
                            fontSize = 18.sp
                        )
                        Switch(
                            checked = isVibrationEnabled.value,
                            onCheckedChange = {
                                viewModel.updateVibration(it)
                            },
                            colors = switchColors
                        )
                    }
                    Divider(
                        color = Blue,
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sound Notifications",
                            color = textColor,
                            fontSize = 18.sp
                        )
                        Switch(
                            checked = isSoundEnabled.value,
                            onCheckedChange = {
                                viewModel.updateSoundNotifications(it)
                            },
                            colors = switchColors
                        )
                    }
                    Divider(
                        color = Blue,
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Time Between Saved Photos",
                            color = textColor,
                            fontSize = 18.sp
                        )
                        TimeBetweenPhotosField(
                            inputValue = timeBetweenPhotos.value.toString(),
                            onSubmit = {
                                viewModel.updateTimeBetweenPhotos(it)
                            }
                        )
                    }
                    Divider(
                        color = Blue,
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = "Start / Stop Recordings",
                            color = textColor,
                            fontSize = 18.sp
                        )
                        val startStopModes = listOf("Manually", "Auto")
                        ToggleButtons(
                            items = startStopModes,
                            currentSelection = if (isAutoStart) startStopModes[1] else startStopModes[0],
                            onChange = { index ->
                                when (startStopModes[index]) {
                                    "Manually" -> {
                                        viewModel.updateAutoStart(false)
                                    }
                                    "Auto" -> {
                                        viewModel.updateAutoStart(true)
                                    }
                                }
                            }
                        )
                    }
                    Divider(
                        color = Blue,
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = "Detection Mode",
                            color = textColor,
                            fontSize = 18.sp
                        )
                        val detectModeItems = listOf("People", "Motion")
                        ToggleButtons(
                            items = detectModeItems,
                            currentSelection = if (isDetectPeopleEnabled) detectModeItems[0] else detectModeItems[1],
                            onChange = { index ->
                                when (detectModeItems[index]) {
                                    "People" -> {
                                        viewModel.updateDetectPeople(true)
                                    }
                                    "Motion" -> {
                                        viewModel.updateDetectPeople(false)
                                    }
                                }
                            }
                        )
                    }
                    if(isDetectPeopleEnabled) {
                        Divider(
                            color = Blue,
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp
                        )
                        PrioritizationSetting(
                            isBodyMergeEnabled = isBodyMergeEnabled,
                            onClick = {
                                viewModel.updateIsBodyMergeEnabled(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeBetweenPhotosField(
    inputValue: String,
    onSubmit: (Int) -> Unit
) {
    var currentValue by remember { mutableStateOf(inputValue.toInt()) }
    Text(
        text = if (currentValue > 1) "$currentValue seconds" else "$currentValue second",
        color = MaterialTheme.colors.primary,
    )
    var sliderValue by remember { mutableStateOf(currentValue.toFloat()) }
    Slider(
        value = sliderValue,
        onValueChange = {
            sliderValue = it
        },
        onValueChangeFinished = {
            currentValue = (sliderValue + 0.1).toInt()
            onSubmit(currentValue)
        },
        valueRange = 1f..10f,
        steps = 8
    )

}

@Composable
private fun PrioritizationSetting(
    isBodyMergeEnabled: Boolean,
    onClick: (Boolean) -> Unit
) {
    val textColor = Color.White
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = "Focus On",
            color = textColor,
            fontSize = 18.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Face detection",
                color = MaterialTheme.colors.primary
            )
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = "Body detection",
                color = MaterialTheme.colors.primary
            )
        }
        var sliderValue by remember { mutableStateOf(if (isBodyMergeEnabled) 1f else 0f) }
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
            },
            onValueChangeFinished = {
                (sliderValue > 0.5f).let { enableBodyMerge ->
                    sliderValue = if (enableBodyMerge) 1f else 0f
                    onClick(enableBodyMerge)
                }
            },
            valueRange = 0f..1f,
        )
    }
}

@Composable
private fun ToggleButtons(
    items: List<String>,
    currentSelection: String,
    onChange: (Int) -> Unit
) {
    val dividerColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    var currentSelectionState by remember { mutableStateOf(currentSelection) }
    val selectedIndex = items.indexOf(currentSelectionState)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(BorderStroke(0.2.dp, dividerColor), shape = RoundedCornerShape(4.dp))
    ) {
        items.forEachIndexed { index, s ->
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Max),
                onClick = {
                    currentSelectionState = items[index]
                    onChange(index)
                },
                shape = RectangleShape,
                colors = if (selectedIndex == index) {
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = dividerColor,
                        backgroundColor = MaterialTheme.colors.primary
                    )
                } else {
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = dividerColor,
                        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                    )
                },
                contentPadding = PaddingValues(
                    horizontal = 4.dp,
                    vertical = 8.dp,
                )
            ) {
                Text(
                    text = s,
                    modifier = Modifier.padding(vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
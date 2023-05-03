package com.example.imageeditor.ui.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                item {
                    val isVibrationEnabled = viewModel.isVibrationEnabled.collectAsState()
                    val isSoundEnabled = viewModel.isSoundEnabled.collectAsState()
                    val timeBetweenPhotos = viewModel.timeBetweenPhotos.collectAsState()

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
                    Divider(color = Blue, modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
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
                    Divider(color = Blue, modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
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
package com.example.imageeditor.ui.views

import android.widget.Chronometer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imageeditor.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraView(
    isAutoStartOn: Boolean,
    isDetectionOn: Boolean,
    onClick: () -> Unit
) {
    var time by remember { mutableStateOf(0) }
    LaunchedEffect(isDetectionOn) {
        while (isDetectionOn) {
            delay(1000L)
            time += 1
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (isDetectionOn) {
                Card(
                    backgroundColor = Color.Red,
                    shape = RoundedCornerShape(4.dp)
                ) {

                    Text(
                        text = String.format("%02d", time / 3600) + ":" + String.format(
                            "%02d",
                            time % 3600 / 60
                        ) + ":" + String.format("%02d", time % 60),
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            } else {
                time = 0
                Text(
                    text = String.format("%02d", time / 3600) + ":" + String.format(
                        "%02d",
                        time % 3600 / 60
                    ) + ":" + String.format("%02d", time % 60),
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = if (isAutoStartOn) Modifier
                    .clip(RoundedCornerShape(100.dp)) else Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable {
                        onClick()
                    }
            ) {
                Image(
                    painter = painterResource(
                        id = R.drawable.ic_circle_outline
                    ),
                    contentDescription = null,
                )

                if (isDetectionOn) {
                    Image(
                        painter = painterResource(
                            id = R.drawable.ic_square
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp),
                        contentDescription = null,
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = R.drawable.ic_circle
                        ),
                        modifier = Modifier
                            .padding(start = 12.dp, top = 12.dp),
                        contentDescription = null,
                    )
                }
            }
        }
        if (isAutoStartOn) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Auto Start/Stop Enabled",
                    color = Color.Red,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
@Preview
fun CameraOnPreview() {
    CameraView(
        isAutoStartOn = true,
        isDetectionOn = true,
    ) {}
}

@Composable
@Preview
fun CameraOffPreview() {
    CameraView(
        isAutoStartOn = false,
        isDetectionOn = false,
    ) {}
}
package com.example.imageeditor.ui.views

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.imageeditor.R
import kotlinx.coroutines.delay

@Composable
fun SplashView(
    onTimeLimitReached: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )
        delay(1100L)
        onTimeLimitReached()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))
        Image(
            painter = painterResource(
                id = R.drawable.ic_splash
            ),
            contentDescription = null,
            modifier = Modifier.scale(scale.value)
        )
        Spacer(Modifier.weight(0.1f))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 60.sp
                )
                ) {
                    append("See")
                }
                withStyle(style = SpanStyle(
                    color = if(isSystemInDarkTheme()) Color.White else Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 60.sp
                )
                ) {
                    append("You")
                }
            }
        )
        Spacer(Modifier.weight(0.4f))
    }
}
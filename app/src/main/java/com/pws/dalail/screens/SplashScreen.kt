package com.pws.dalail.screens

import android.window.SplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.pws.dalail.R

@Composable
fun SplashScreen() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color(0xffF8F9FA)), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.logoapk2),
            contentDescription = "Logo",
        )


    }

}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()

}
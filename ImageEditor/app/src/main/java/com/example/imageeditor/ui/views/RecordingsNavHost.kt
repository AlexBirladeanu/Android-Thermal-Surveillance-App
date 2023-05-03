package com.example.imageeditor.ui.views

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.imageeditor.ui.theme.ThiefBusterTheme
import com.example.imageeditor.viewModels.RecordingsViewModel

@Composable
fun RecordingsNavHost(
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(navController = navHostController, startDestination = "RecordingsView") {
        composable("RecordingsView") {
            ThiefBusterTheme {
                RecordingsView(
                    navigateToDetails = {
                        RecordingsViewModel.selectedRecordingWithPhotos = it
                        navHostController.navigate("RecordingDetailsView")
                    }
                )
            }
        }
        composable("RecordingDetailsView") {
            ThiefBusterTheme {
                RecordingDetailsView(
                    onScreenClose = {
                        navHostController.navigateUp()
                    }
                )
            }
        }
    }
}
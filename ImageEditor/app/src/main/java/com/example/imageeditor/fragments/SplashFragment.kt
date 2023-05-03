package com.example.imageeditor.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.imageeditor.ui.theme.ThiefBusterTheme
import com.example.imageeditor.ui.views.SplashView

class SplashFragment(
    val onExit: () -> Unit
): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ThiefBusterTheme {
                    SplashView(
                        onTimeLimitReached = onExit
                    )
                }
            }
        }
    }
}
package com.example.charfreq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.charfreq.navigation.AppNavigation
import com.example.charfreq.ui.CharFreqViewModel
import com.example.charfreq.ui.theme.CharFreqTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CharFreqViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            CharFreqTheme(
                themeSetting = uiState.theme,
                dynamicColor = uiState.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

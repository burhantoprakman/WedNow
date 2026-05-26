package com.wednowapp.wednow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.wednowapp.wednow.core.navigation.WedNowNavGraph
import com.wednowapp.wednow.ui.theme.WedNowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WedNowTheme {
                val navController = rememberNavController()
                WedNowNavGraph(navController = navController)
            }
        }
    }
}

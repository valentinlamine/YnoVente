package com.example.ynovente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ynovente.ui.screens.MainScreenWithBottomNav
import com.example.ynovente.ui.screens.login.LoginScreen
import com.example.ynovente.ui.theme.YnoventeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YnoventeTheme { // <-- ici
                val rootNavController = rememberNavController()
                NavHost(rootNavController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(onLoginSuccess = {
                            rootNavController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }
                    composable("main") {
                        MainScreenWithBottomNav()
                    }
                }
            }
        }
    }
}
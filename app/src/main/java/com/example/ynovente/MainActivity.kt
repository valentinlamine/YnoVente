package com.example.ynovente

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ynovente.ui.screens.MainScreenWithBottomNav
import com.example.ynovente.ui.screens.login.LoginScreen
import com.example.ynovente.ui.screens.login.LoginViewModel
import com.example.ynovente.data.repository.FirebaseAuthRepository
import com.example.ynovente.ui.theme.YnoventeTheme
import com.google.firebase.FirebaseApp
import androidx.compose.ui.platform.LocalContext
import com.example.ynovente.ui.screens.register.RegisterScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            YnoventeTheme {
                val rootNavController = rememberNavController()
                NavHost(rootNavController, startDestination = "login") {
                    composable("login") {
                        // -- INJECTION MANUELLE DU VIEWMODEL --
                        val activity = this@MainActivity
                        val loginViewModel = remember {
                            LoginViewModel(FirebaseAuthRepository(activity))
                        }
                        LoginScreen(
                            onLoginSuccess = {
                                rootNavController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            navController = rootNavController,
                            viewModel = loginViewModel
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                rootNavController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            navController = rootNavController
                        )
                    }
                    composable("main") {
                        MainScreenWithBottomNav(onLogout = {
                            rootNavController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }
                }
            }
        }
    }
}
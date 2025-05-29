package com.example.ynovente

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        // *** AJOUT ***
        // Demande la permission POST_NOTIFICATIONS dès le lancement si besoin (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                val requestPermissionLauncher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                        // Optionnel : tu peux afficher un toast ou log ici
                    }
                requestPermissionLauncher.launch(permission)
            }
        }
        // *** FIN AJOUT ***

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
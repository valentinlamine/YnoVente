package com.example.ynovente

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ynovente.ui.screens.MainScreenWithBottomNav
import com.example.ynovente.ui.screens.login.LoginScreen
import com.example.ynovente.ui.screens.login.LoginViewModel
import com.example.ynovente.data.repository.FirebaseAuthRepository
import com.example.ynovente.ui.theme.YnoventeTheme
import com.google.firebase.FirebaseApp
import com.example.ynovente.ui.screens.register.RegisterScreen
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuration du edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)

        // Demande la permission POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                val requestPermissionLauncher =
                    registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
                requestPermissionLauncher.launch(permission)
            }
        }

        setContent {
            YnoventeTheme {
                // Gestion des barres système
                val view = LocalView.current
                val isDarkTheme = isSystemInDarkTheme()

                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as MainActivity).window
                        val windowController = WindowCompat.getInsetsController(window, view)

                        WindowCompat.setDecorFitsSystemWindows(window, false)

                        windowController.isAppearanceLightStatusBars = !isDarkTheme
                        windowController.isAppearanceLightNavigationBars = !isDarkTheme

                        windowController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val rootNavController = rememberNavController()
                    NavHost(rootNavController, startDestination = "login") {
                        composable("login") {
                            val loginViewModel = remember {
                                LoginViewModel(FirebaseAuthRepository(this@MainActivity))
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
}

@Composable
private fun isSystemInDarkTheme(): Boolean {
    val configuration = LocalConfiguration.current
    return when (configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
        android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}
package com.example.ynovente.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.ynovente.ui.components.BottomNavigationBar
import com.example.ynovente.ui.navigation.AppNavHost

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreenWithBottomNav(onLogout: () -> Unit) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            innerPadding = innerPadding,
            onLogout = onLogout
        )
    }
}
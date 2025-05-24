package com.example.ynovente.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.ynovente.ui.components.BottomNavigationBar
import com.example.ynovente.ui.navigation.AppNavHost

@Composable
fun MainScreenWithBottomNav() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        AppNavHost(navController, innerPadding)
    }
}
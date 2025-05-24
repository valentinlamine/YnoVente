package com.example.ynovente.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ynovente.ui.screens.home.HomeScreen
import com.example.ynovente.ui.screens.products.MyProductsScreen
import com.example.ynovente.ui.screens.profile.ProfileScreen
// import com.example.ynovente.ui.screens.products.MyProductsScreen

@Composable
fun AppNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("home") { HomeScreen(navController, innerPadding = innerPadding) }
        composable("products") { MyProductsScreen(navController, innerPadding = innerPadding) }
        composable("profile") { ProfileScreen(navController, innerPadding = innerPadding) }
    }
}
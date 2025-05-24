package com.example.ynovente.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ynovente.ui.screens.home.HomeScreen
import com.example.ynovente.ui.screens.home.OfferDetailScreen
import com.example.ynovente.ui.screens.products.MyProductsScreen
import com.example.ynovente.ui.screens.profile.ProfileScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("home") { HomeScreen(navController) }
        composable("products") { MyProductsScreen(navController) }
        composable("profile") { ProfileScreen(navController = navController, onLogout = onLogout) }
        composable("offerDetail/{offerId}") { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            OfferDetailScreen(navController, offerId)
        }
    }
}
package com.example.ynovente.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ynovente.data.repository.FakeOfferRepository
import com.example.ynovente.data.repository.FirebaseOfferRepository
import com.example.ynovente.ui.screens.home.HomeScreen
import com.example.ynovente.ui.screens.home.OfferDetailScreen
import com.example.ynovente.ui.screens.myproducts.CreateOfferScreen
import com.example.ynovente.ui.screens.myproducts.MyProductsScreen
import com.example.ynovente.ui.screens.profile.ProfileScreen

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onLogout: () -> Unit
) {
    val offerRepository = remember { FakeOfferRepository() }
    val firebaseOfferRepository = remember { FirebaseOfferRepository() }
    // User fake pour le repo fake uniquement
    val currentUser = com.example.ynovente.data.model.User("1", "Alice", "alice@mail.com", "password")

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("home") { HomeScreen(navController) }
        composable("products") {
            MyProductsScreen(
                navController = navController,
                offerRepository = offerRepository,
                currentUser = currentUser
            )
        }
        composable("profile") { ProfileScreen(navController = navController, onLogout = onLogout) }
        composable("offerDetail/{offerId}") { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: ""
            OfferDetailScreen(navController, offerId)
        }
        composable("createOffer") {
            CreateOfferScreen(
                navController = navController,
                offerRepository = firebaseOfferRepository // Utilise Firebase pour la création
            )
        }
    }
}
package com.example.ynovente.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Ventes", Icons.Filled.Home)
    object MyProducts : BottomNavScreen("my_products", "Mes produits", Icons.AutoMirrored.Filled.List)
    object Profile : BottomNavScreen("profile", "Profil", Icons.Filled.Person)

    companion object {
        val items = listOf(Home, MyProducts, Profile)
    }
}
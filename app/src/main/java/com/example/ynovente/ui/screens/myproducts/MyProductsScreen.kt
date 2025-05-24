package com.example.ynovente.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(navController: NavController, innerPadding: PaddingValues = PaddingValues(0.dp)) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes produits") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValues)
        ) {
            Text("Bienvenue sur la page Mes produits !", style = MaterialTheme.typography.titleLarge)
        }
    }
}
package com.example.ynovente.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ynovente.data.model.Offer
import java.time.format.DateTimeFormatter

enum class FilterType { DATE, PRICE, NAME }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val offers by viewModel.offers.collectAsState()
    var filter by remember { mutableStateOf(FilterType.DATE) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // Tri selon le filtre sélectionné
    val sortedOffers = remember(offers, filter) {
        when (filter) {
            FilterType.DATE -> offers.sortedBy { it.endDate }
            FilterType.PRICE -> offers.sortedBy { it.price }
            FilterType.NAME -> offers.sortedBy { it.title }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventes aux enchères") },
                actions = {
                    IconButton(onClick = { filterMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Filtrer les offres"
                        )
                    }
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Trier par prix") },
                            onClick = {
                                filter = FilterType.PRICE
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trier par date") },
                            onClick = {
                                filter = FilterType.DATE
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trier par nom") },
                            onClick = {
                                filter = FilterType.NAME
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            )
        },
        bottomBar = { /* BottomNav ici si besoin */ }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {
            items(sortedOffers) { offer ->
                AuctionCard(
                    offer = offer,
                    onClick = {
                        navController.navigate("offerDetail/${offer.id}")
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AuctionCard(
    offer: Offer,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                offer.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                offer.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${offer.price} €",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    "Fin: ${offer.endDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
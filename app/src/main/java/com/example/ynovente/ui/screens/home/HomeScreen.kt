package com.example.ynovente.ui.screens.home

import android.content.res.Configuration
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
import com.example.ynovente.data.repository.FirebaseOfferRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalConfiguration

enum class FilterType { DATE, PRICE, NAME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    firebaseOfferRepository: FirebaseOfferRepository,
) {
    val offerFlow = remember { firebaseOfferRepository.getOffersFlow() }
    val offers by offerFlow.collectAsState(initial = emptyList())
    var filter by remember { mutableStateOf(FilterType.DATE) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // Tri selon le filtre sélectionné
    val sortedOffers = remember(offers, filter) {
        when (filter) {
            FilterType.DATE -> offers.sortedBy {
                try { LocalDateTime.parse(it.endDate) } catch (_: Exception) { null }
            }
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

@Composable
fun AuctionCard(
    offer: Offer,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    val formattedEndDate = try {
        LocalDateTime.parse(offer.endDate).format(dateFormatter)
    } catch (_: Exception) {
        offer.endDate
    }

    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDarkTheme) 8.dp else 4.dp,
            pressedElevation = if (isDarkTheme) 4.dp else 2.dp,
            focusedElevation = if (isDarkTheme) 8.dp else 4.dp,
            hoveredElevation = if (isDarkTheme) 10.dp else 6.dp
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (isDarkTheme) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                offer.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(Modifier.height(4.dp))
            Text(
                offer.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
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
                    "Fin: $formattedEndDate",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = if (isDarkTheme) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun isSystemInDarkTheme(): Boolean {
    val configuration = LocalConfiguration.current
    return when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}
package com.example.ynovente.ui.screens.myproducts

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ynovente.data.model.Offer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class MyProductsFilterType { DATE, PRICE, NAME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    navController: NavController,
    viewModel: MyProductsViewModel
) {
    val finishedOffersWithWinner by viewModel.finishedOffersWithWinner.collectAsState()
    val activeOffers by viewModel.activeOffers.collectAsState()
    var filter by remember { mutableStateOf(MyProductsFilterType.DATE) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    val sortedActiveOffers = remember(activeOffers, filter) {
        when (filter) {
            MyProductsFilterType.DATE -> activeOffers.sortedBy { it.endDate }
            MyProductsFilterType.PRICE -> activeOffers.sortedBy { it.price }
            MyProductsFilterType.NAME -> activeOffers.sortedBy { it.title }
        }
    }
    val sortedFinishedOffersWithWinner = remember(finishedOffersWithWinner) { finishedOffersWithWinner.sortedByDescending { it.offer.endDate } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes produits") },
                actions = {
                    IconButton(onClick = { filterMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu des filtres"
                        )
                    }
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Trier par prix") },
                            onClick = {
                                filter = MyProductsFilterType.PRICE
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trier par date") },
                            onClick = {
                                filter = MyProductsFilterType.DATE
                                filterMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trier par nom") },
                            onClick = {
                                filter = MyProductsFilterType.NAME
                                filterMenuExpanded = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createOffer") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer une offre")
            }
        }
    ) { paddingValues ->
        if (sortedActiveOffers.isEmpty() && sortedFinishedOffersWithWinner.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Vous n'avez pas encore créé d'offre.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
            ) {
                if (sortedFinishedOffersWithWinner.isNotEmpty()) {
                    item {
                        Text(
                            "Enchères terminées",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF388E3C),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                    items(sortedFinishedOffersWithWinner) { display ->
                        MyProductCard(
                            offer = display.offer,
                            onClick = { navController.navigate("offerDetail/${display.offer.id}") },
                            isFinished = true,
                            winnerEmail = display.winnerEmail
                        )
                    }
                }
                if (sortedActiveOffers.isNotEmpty()) {
                    item {
                        Text(
                            "Enchères en cours",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    items(sortedActiveOffers) { offer ->
                        MyProductCard(
                            offer = offer,
                            onClick = { navController.navigate("offerDetail/${offer.id}") },
                            isFinished = false,
                            winnerEmail = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyProductCard(
    offer: Offer,
    onClick: () -> Unit,
    isFinished: Boolean = false,
    winnerEmail: String? = null
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    val formattedEndDate = try {
        val date = LocalDateTime.parse(offer.endDate)
        date.format(dateFormatter)
    } catch (_: Exception) {
        offer.endDate
    }

    val isDarkTheme = isSystemInDarkTheme()

    val cardBackground = if (isFinished) {
        if (isDarkTheme) {
            Color(0xFF1B5E20)
        } else {
            Color(0xFFE8F5E9)
        }
    } else {
        if (isDarkTheme) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    }

    val textColor = if (isFinished) {
        if (isDarkTheme) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDarkTheme) 8.dp else 4.dp,
            pressedElevation = if (isDarkTheme) 4.dp else 2.dp,
            focusedElevation = if (isDarkTheme) 8.dp else 4.dp,
            hoveredElevation = if (isDarkTheme) 10.dp else 6.dp
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (isDarkTheme && !isFinished) {
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
                maxLines = 1,
                color = textColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                offer.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFinished) {
                    if (isDarkTheme) Color(0xFFB3E0B3) else Color(0xFF2E7D32)
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
                        color = if (isFinished) {
                            if (isDarkTheme) Color(0xFFA5D6A7) else Color(0xFF1B5E20)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                )
                Text(
                    "Fin: $formattedEndDate",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = if (isFinished) {
                        if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            winnerEmail?.let { email ->
                Spacer(Modifier.height(4.dp))
                Text(
                    "Gagnant: $email",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color(0xFF81C784) else Color(0xFF2E7D32)
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
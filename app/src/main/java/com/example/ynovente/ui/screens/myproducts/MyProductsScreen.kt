package com.example.ynovente.ui.screens.myproducts

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.model.User
import com.example.ynovente.data.repository.FakeOfferRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class MyProductsFilterType { DATE, PRICE, NAME }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    navController: NavController,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: MyProductsViewModel
) {
    val myOffers by viewModel.myOffers.collectAsState()
    var filter by remember { mutableStateOf(MyProductsFilterType.DATE) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    val sortedOffers = remember(myOffers, filter) {
        when (filter) {
            MyProductsFilterType.DATE -> myOffers.sortedBy { it.endDate }
            MyProductsFilterType.PRICE -> myOffers.sortedBy { it.price }
            MyProductsFilterType.NAME -> myOffers.sortedBy { it.title }
        }
    }

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
        if (sortedOffers.isEmpty()) {
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
                items(sortedOffers) { offer ->
                    MyProductCard(
                        offer = offer,
                        onClick = {
                            navController.navigate("offerDetail/${offer.id}")
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyProductCard(
    offer: Offer,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    val formattedEndDate = try {
        val date = LocalDateTime.parse(offer.endDate)
        date.format(dateFormatter)
    } catch (e: Exception) {
        offer.endDate
    }
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
                    "Fin: $formattedEndDate",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
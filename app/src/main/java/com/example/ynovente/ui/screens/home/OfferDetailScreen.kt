package com.example.ynovente.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.model.Offer
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    navController: NavController,
    offerId: String,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val offers by viewModel.offers.collectAsState()
    val offer = offers.find { it.id == offerId }
    val snackbarHostState = remember { SnackbarHostState() }
    var lastBidPlaced by remember { mutableStateOf<Double?>(null) }

    // Affiche le snackbar quand une enchère est validée
    LaunchedEffect(lastBidPlaced) {
        lastBidPlaced?.let { amount ->
            snackbarHostState.showSnackbar("Enchère de $amount€ placée !")
            lastBidPlaced = null // reset pour éviter les répétitions
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(offer?.title ?: "Détail de l'enchère") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (offer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Offre introuvable", style = MaterialTheme.typography.headlineSmall)
            }
        } else {
            OfferDetailContent(
                offer = offer,
                onBid = { amount ->
                    // Ici tu déclenches juste la mise à jour d'état
                    lastBidPlaced = amount
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OfferDetailContent(
    offer: Offer,
    onBid: (Double) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var bidAmount by remember { mutableStateOf("") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image (dummy si pas encore branché)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Remplace par une vraie image si tu en as
            Image(
                painter = painterResource(id = com.example.ynovente.R.drawable.ic_launcher_foreground),
                contentDescription = offer.title,
                modifier = Modifier.size(120.dp)
            )
        }

        Text(
            offer.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            offer.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Prix actuel : ${offer.price} €",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Fin : ${offer.endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Champ pour enchérir
        OutlinedTextField(
            value = bidAmount,
            onValueChange = { bidAmount = it },
            label = { Text("Votre enchère (€)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                val amount = bidAmount.toDoubleOrNull()
                if (amount != null && amount > offer.price) {
                    onBid(amount)
                    bidAmount = ""
                } else {
                    // Affichage d'une erreur simple (snackbar ou toast peut être ajouté)
                }
            },
            enabled = bidAmount.toDoubleOrNull()?.let { it > offer.price } ?: false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enchérir")
        }
        val bids by viewModel.getBidsForOfferFlow(offer.id).collectAsState(initial = emptyList())
        BidsHistorySection(bids = bids)
    }
}

@Composable
fun BidsHistorySection(bids: List<Bid>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Historique des enchères", style = MaterialTheme.typography.titleMedium)
        if (bids.isEmpty()) {
            Text("Aucune enchère pour le moment.")
        } else {
            bids.forEach { bid ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${bid.user.name} :", fontWeight = FontWeight.Bold)
                    Text("${bid.amount} €")
                    Text(bid.date.substring(11,16)) // Heure HH:mm
                }
            }
        }
    }
}
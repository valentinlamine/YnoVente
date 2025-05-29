package com.example.ynovente.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.repository.FirebaseOfferRepository
import com.example.ynovente.data.repository.FirebaseUserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    navController: NavController,
    offerId: String,
    firebaseOfferRepository: FirebaseOfferRepository,
    firebaseUserRepository: FirebaseUserRepository = FirebaseUserRepository()
) {
    val offerFlow = remember(offerId) { firebaseOfferRepository.getOfferByIdFlow(offerId) }
    val offer by offerFlow.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    var lastBidPlaced by remember { mutableStateOf<Double?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val userName = user?.displayName ?: user?.email ?: "Utilisateur"

    var isEditing by remember { mutableStateOf(false) }
    var editSuccess by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(lastBidPlaced) {
        lastBidPlaced?.let { amount ->
            snackbarHostState.showSnackbar("Enchère de $amount€ placée !")
            lastBidPlaced = null
        }
    }

    LaunchedEffect(editSuccess) {
        if (editSuccess) {
            isEditing = false
            snackbarHostState.showSnackbar("Offre modifiée avec succès")
            editSuccess = false
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
            if (!isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Offre introuvable", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            val currentOffer: Offer = offer!!
            val isMyOffer = userId != null && currentOffer.userId == userId

            if (isEditing && isMyOffer) {
                // Placez ici votre composant d'édition OfferEditContent, à créer séparément
                // OfferEditContent(...)
            } else {
                OfferDetailContent(
                    offer = currentOffer,
                    onBid = { amount ->
                        coroutineScope.launch {
                            try {
                                firebaseOfferRepository.placeBid(
                                    offerId = currentOffer.id,
                                    userId = user?.uid ?: "",
                                    userName = userName,
                                    amount = amount
                                )
                                lastBidPlaced = amount
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Erreur lors de la surenchère : ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues),
                    firebaseOfferRepository = firebaseOfferRepository,
                    firebaseUserRepository = firebaseUserRepository,
                    showEditButton = isMyOffer,
                    onEdit = { isEditing = true },
                    onDelete = {
                        coroutineScope.launch {
                            try {
                                isDeleting = true
                                firebaseOfferRepository.deleteOffer(currentOffer.id)
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                isDeleting = false
                                snackbarHostState.showSnackbar("Erreur lors de la suppression : ${e.message}")
                            }
                        }
                    },
                    isMyOffer = isMyOffer
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OfferDetailContent(
    offer: Offer,
    onBid: (Double) -> Unit,
    modifier: Modifier = Modifier,
    firebaseOfferRepository: FirebaseOfferRepository,
    firebaseUserRepository: FirebaseUserRepository,
    showEditButton: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    isMyOffer: Boolean = false
) {
    var bidAmount by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    val bids by firebaseOfferRepository.getBidsForOfferFlow(offer.id).collectAsState(initial = emptyList())
    val context = LocalContext.current

    val isFinished = try {
        LocalDateTime.parse(offer.endDate).isBefore(LocalDateTime.now())
    } catch (e: Exception) {
        false
    }

    val maxAmount = bids.maxOfOrNull { it.amount }
    val bestBid = bids
        .filter { it.amount == maxAmount }
        .maxByOrNull { it.date }

    var winnerEmail by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isFinished, bestBid?.userId) {
        winnerEmail = null
        if (isFinished && bestBid?.userId != null) {
            winnerEmail = firebaseUserRepository.getUserById(bestBid.userId)?.email
        }
    }

    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CARD PRINCIPALE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // IMAGE HERO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE3E3E3)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!offer.imageUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(offer.imageUrl),
                            contentDescription = offer.title,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Aucune image",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        offer.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (showEditButton) {
                        Row {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Modifier"
                                )
                            }
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    offer.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(10.dp))
                // INFOS CLÉS EN CHIPS
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Prix : %.2f €".format(offer.price)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Euro, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "Fin : ${
                                    try {
                                        LocalDateTime.parse(offer.endDate)
                                            .format(dateFormatter)
                                    } catch (e: Exception) {
                                        offer.endDate
                                    }
                                }"
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }

        // SECTION ENCHÈRE
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 0.dp)
                .background(
                    color = Color.Transparent
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isFinished) {
                Text(
                    "Cette enchère est terminée.",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    "Votre enchère",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = bidAmount,
                    onValueChange = { bidAmount = it },
                    label = { Text("Montant (€)") },
                    singleLine = true,
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    leadingIcon = { Text("€") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Button(
                    onClick = {
                        val amount = bidAmount.toDoubleOrNull()
                        if (amount != null && amount > offer.price) {
                            onBid(amount)
                            bidAmount = ""
                        }
                    },
                    enabled = (bidAmount.toDoubleOrNull()?.let { it > offer.price } ?: false),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Enchérir")
                }
            }
            // BOUTON CONTACT GAGNANT POUR LE CREATEUR DE L'OFFRE (ENCHERE TERMINEE)
            if (isFinished && isMyOffer) {
                if (winnerEmail.isNullOrEmpty()) {
                    Text(
                        text = "Aucun gagnant pour cette enchère.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_SENDTO,
                                android.net.Uri.parse("mailto:$winnerEmail")
                            )
                            intent.putExtra(
                                android.content.Intent.EXTRA_SUBJECT,
                                "Félicitations pour votre enchère gagnée : ${offer.title}"
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Contacter le gagnant")
                    }
                }
            }
        }

        // HISTORIQUE ENCHERES
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp
        ) {
            BidsHistorySectionMaterial3(bids = bids, modifier = Modifier.padding(16.dp))
        }
        Spacer(Modifier.height(12.dp))

        // Boîte de dialogue de confirmation de suppression
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDelete()
                        }
                    ) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirm = false }
                    ) { Text("Annuler") }
                },
                title = { Text("Confirmation") },
                text = { Text("Voulez-vous vraiment supprimer cette offre ? Cette action est irréversible.") }
            )
        }
    }
}

@Composable
fun BidsHistorySectionMaterial3(bids: List<Bid>, modifier: Modifier = Modifier) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bid.userName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(7.dp))
                        Text(bid.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text("%.2f €".format(bid.amount), fontWeight = FontWeight.Medium)
                    Text(
                        bid.date.takeIf { it.length > 15 }?.substring(11, 16) ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
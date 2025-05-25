package com.example.ynovente.ui.screens.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ynovente.data.model.Bid
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.repository.FirebaseOfferRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    navController: NavController,
    offerId: String,
    firebaseOfferRepository: FirebaseOfferRepository
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

    // Affiche le snackbar quand une enchère est validée
    LaunchedEffect(lastBidPlaced) {
        lastBidPlaced?.let { amount ->
            snackbarHostState.showSnackbar("Enchère de $amount€ placée !")
            lastBidPlaced = null
        }
    }

    // Après modification, repasse en mode détail et affiche le snackbar
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

        // Bloque l'affichage de "Offre introuvable" si suppression en cours
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
            } else {
                // Optionnel : Affiche rien ou un CircularProgressIndicator()
            }
        } else {
            val currentOffer: Offer = offer!!
            val isMyOffer = userId != null && currentOffer.userId == userId

            if (isEditing && isMyOffer) {
                OfferEditContent(
                    offer = currentOffer,
                    onSave = { updatedTitle, updatedDescription, updatedEndDate ->
                        coroutineScope.launch {
                            try {
                                firebaseOfferRepository.updateOffer(
                                    offerId = currentOffer.id,
                                    title = updatedTitle,
                                    description = updatedDescription,
                                    endDate = updatedEndDate
                                )
                                editSuccess = true // Va déclencher LaunchedEffect pour repasser en mode détail
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Erreur lors de la modification : ${e.message}")
                            }
                        }
                    },
                    onCancel = { isEditing = false },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
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
                        .padding(paddingValues),
                    firebaseOfferRepository = firebaseOfferRepository,
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
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OfferEditContent(
    offer: Offer,
    onSave: (String, String, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(offer.title) }
    var description by remember { mutableStateOf(offer.description) }
    var endDate by remember { mutableStateOf(offer.endDate) }
    var tempPickedDate by remember { mutableStateOf<LocalDateTime?>(try {
        LocalDateTime.parse(offer.endDate)
    } catch (_: Exception) { null }) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm") }
    val displayFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }

    // Pré-remplit le calendar avec la date actuelle
    LaunchedEffect(tempPickedDate) {
        tempPickedDate?.let {
            calendar.set(Calendar.YEAR, it.year)
            calendar.set(Calendar.MONTH, it.monthValue - 1)
            calendar.set(Calendar.DAY_OF_MONTH, it.dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, it.hour)
            calendar.set(Calendar.MINUTE, it.minute)
        }
    }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    tempPickedDate = LocalDateTime.of(year, month + 1, day, tempPickedDate?.hour ?: 12, tempPickedDate?.minute ?: 0)
                    showDatePicker = false
                    showTimePicker = true
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    LaunchedEffect(showTimePicker, tempPickedDate) {
        if (showTimePicker && tempPickedDate != null) {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    tempPickedDate = tempPickedDate!!.withHour(hour).withMinute(minute)
                    endDate = tempPickedDate!!.format(dateFormatter)
                    showTimePicker = false
                },
                tempPickedDate?.hour ?: 12, tempPickedDate?.minute ?: 0, true
            ).show()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Modification de l'offre", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = if (endDate.isNotBlank()) {
                try {
                    LocalDateTime.parse(endDate, dateFormatter)
                        .format(displayFormatter)
                } catch (e: Exception) { endDate }
            } else "",
            onValueChange = {},
            label = { Text("Date de fin") },
            readOnly = true,
            enabled = false,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Choisir une date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            placeholder = { Text("Choisir une date de fin") }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onCancel() }) { Text("Annuler") }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onSave(title, description, endDate)
                },
                enabled = title.isNotBlank() && description.isNotBlank() && endDate.isNotBlank()
            ) {
                Text("Enregistrer")
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
    showEditButton: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var bidAmount by remember { mutableStateOf("") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    val bids by firebaseOfferRepository.getBidsForOfferFlow(offer.id).collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!offer.imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(offer.imageUrl),
                    contentDescription = offer.title,
                    modifier = Modifier.size(160.dp)
                )
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                offer.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (showEditButton) {
                Row {
                    Button(onClick = onEdit) {
                        Text("Modifier")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Supprimer", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }

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
                "Fin : ${
                    try {
                        LocalDateTime.parse(offer.endDate)
                            .format(dateFormatter)
                    } catch (e: Exception) {
                        offer.endDate
                    }
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }

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
                }
            },
            enabled = bidAmount.toDoubleOrNull()?.let { it > offer.price } ?: false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enchérir")
        }

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
                    Text("${bid.userName} :", fontWeight = FontWeight.Bold)
                    Text("${bid.amount} €")
                    Text(bid.date.substring(11,16)) // Heure HH:mm
                }
            }
        }
    }
}
package com.example.ynovente.ui.screens.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Euro
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
                                editSuccess = true
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
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm") }
    val bids by firebaseOfferRepository.getBidsForOfferFlow(offer.id).collectAsState(initial = emptyList())

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
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
                        )
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

        // SECTION ENCHÈRE : PAS DE CARD, JUSTE UN ESPACE AIRÉ
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
                enabled = bidAmount.toDoubleOrNull()?.let { it > offer.price } ?: false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Enchérir")
            }
        }

        // HISTORIQUE ENCHERES : PAS DE CARD, JUSTE UN ENCADRÉ AVEC UN FOND LÉGER
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
                        Text("${bid.userName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text("%.2f €".format(bid.amount), fontWeight = FontWeight.Medium)
                    Text(bid.date.substring(11,16), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
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

    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
}
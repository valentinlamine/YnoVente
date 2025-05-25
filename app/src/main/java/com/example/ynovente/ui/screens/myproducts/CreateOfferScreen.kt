package com.example.ynovente.ui.screens.myproducts

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.repository.FirebaseOfferRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferScreen(
    navController: NavController,
    offerRepository: FirebaseOfferRepository
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var tempPickedDate by remember { mutableStateOf<LocalDateTime?>(null) }

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var localImageUri by remember { mutableStateOf<String?>(null) }
    var imageUploading by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm") }

    // Camera launcher
    val tempImageFile = remember { File(context.cacheDir, "offer_photo_${System.currentTimeMillis()}.jpg") }
    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        tempImageFile
    )

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            localImageUri = imageUri.toString()
            imageUploading = true
            scope.launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("offers/${UUID.randomUUID()}.jpg")
                    val uploadTask = storageRef.putFile(imageUri)
                    uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            imageUrl = uri.toString()
                            imageUploading = false
                        }.addOnFailureListener {
                            error = "Erreur lors de la récupération de l'URL de l'image"
                            imageUploading = false
                        }
                    }.addOnFailureListener {
                        error = "Erreur lors de l'upload de l'image"
                        imageUploading = false
                    }
                } catch (e: Exception) {
                    error = "Erreur lors de l'upload de l'image"
                    imageUploading = false
                }
            }
        }
    }

    // Permissions pour la caméra
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(imageUri)
        } else {
            error = "La permission caméra est requise"
        }
    }

    fun openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            cameraLauncher.launch(imageUri)
        }
    }

    // Gestion DatePicker et TimePicker via effet de bord pour éviter doubles dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    tempPickedDate = LocalDateTime.of(year, month + 1, day, 0, 0)
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
                12, 0, true
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Créer une nouvelle offre", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            )
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            ),
            maxLines = 4
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = price,
            onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Prix de départ (€)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Champ date adapté (text field readonly + icône calendrier)
        OutlinedTextField(
            value = if (endDate.isNotBlank()) {
                try {
                    LocalDateTime.parse(endDate, dateFormatter)
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm"))
                } catch (e: Exception) { "" }
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

        Spacer(Modifier.height(12.dp))

        // Image section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { openCamera() },
                enabled = !imageUploading
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Prendre une photo")
                Spacer(Modifier.width(8.dp))
                Text("Prendre une photo")
            }
            Spacer(Modifier.width(16.dp))
            if (localImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(localImageUri),
                    contentDescription = "Image prise",
                    modifier = Modifier.size(64.dp)
                )
            }
            if (imageUploading) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                when {
                    title.isBlank() -> error = "Le titre est obligatoire"
                    description.isBlank() -> error = "La description est obligatoire"
                    price.isBlank() || price.toDoubleOrNull() == null -> error = "Prix invalide"
                    endDate.isBlank() -> error = "La date de fin est obligatoire"
                    imageUploading -> error = "Attendez la fin de l'envoi de l'image"
                    else -> {
                        error = null
                        isSubmitting = true
                        scope.launch {
                            try {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId == null) {
                                    error = "Utilisateur non connecté"
                                    isSubmitting = false
                                    return@launch
                                }
                                val newOffer = Offer(
                                    id = "",
                                    title = title,
                                    description = description,
                                    price = price.toDouble(),
                                    endDate = endDate,
                                    imageUrl = imageUrl,
                                    userId = userId
                                )
                                offerRepository.addOffer(newOffer)
                                isSubmitting = false
                                navController.popBackStack()
                            } catch (e: Exception) {
                                error = "Erreur lors de la création : ${e.message}"
                                isSubmitting = false
                            }
                        }
                    }
                }
            },
            enabled = !isSubmitting && !imageUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Créer l'offre")
        }
    }
}
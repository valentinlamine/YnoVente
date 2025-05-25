package com.example.ynovente.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: androidx.navigation.NavHostController,
    onLogout: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(user?.displayName ?: "") }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val needsReauth by viewModel.needsReauth.collectAsState()

    // Réauth dialog
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var reauthError by remember { mutableStateOf<String?>(null) }

    // Snackbar for feedback
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error, info) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            error = null
        }
        info?.let {
            snackbarHostState.showSnackbar(it)
            info = null
        }
    }

    // Affiche le dialogue de ré-auth si besoin
    LaunchedEffect(needsReauth) {
        if (needsReauth) showReauthDialog = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profil utilisateur") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Bienvenue sur la page Profil !",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (user != null) {
                        // Avatar rond
                        user?.photoUrl?.let { photoUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(photoUrl),
                                contentDescription = "Photo de profil",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.displayName?.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Modification du nom
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    label = { Text("Nom") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    viewModel.updateDisplayName(
                                        newName,
                                        onSuccess = {
                                            info = "Nom modifié"
                                            isEditingName = false
                                        },
                                        onError = { error = it }
                                    )
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Enregistrer")
                                }
                                IconButton(onClick = {
                                    isEditingName = false
                                    newName = user?.displayName ?: ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Annuler")
                                }
                            } else {
                                Text(
                                    "Nom : ${user?.displayName ?: "Non renseigné"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { isEditingName = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier le nom")
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Email affiché mais non modifiable
                        Text(
                            "Email : ${user?.email ?: "Non renseigné"}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "UID : ${user?.uid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        FilledTonalButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Changer le mot de passe")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                        ) {
                            Text("Supprimer le compte")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.signOut()
                                onLogout()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Se déconnecter")
                        }
                    } else {
                        Text("Aucun utilisateur connecté.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    // Dialogues
    if (showPasswordDialog) {
        var passwordVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Changer le mot de passe") },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nouveau mot de passe") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Cacher" else "Afficher")
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updatePassword(newPassword,
                        onSuccess = {
                            info = "Mot de passe modifié"
                            showPasswordDialog = false
                        },
                        onError = { error = it }
                    )
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le compte") },
            text = { Text("Voulez-vous vraiment supprimer votre compte ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(
                        onSuccess = {
                            showDeleteDialog = false
                            onLogout()
                        },
                        onError = { error = it }
                    )
                }) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showReauthDialog) {
        var reauthVisible by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showReauthDialog = false },
            title = { Text("Ré-authentification requise") },
            text = {
                Column {
                    Text(
                        "Pour cette opération, veuillez entrer votre mot de passe actuel.",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = reauthPassword,
                        onValueChange = { reauthPassword = it },
                        label = { Text("Mot de passe actuel") },
                        singleLine = true,
                        visualTransformation = if (reauthVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (reauthVisible)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { reauthVisible = !reauthVisible }) {
                                Icon(imageVector = image, contentDescription = if (reauthVisible) "Cacher" else "Afficher")
                            }
                        }
                    )
                    if (reauthError != null) {
                        Text(reauthError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    user?.email?.let { email ->
                        viewModel.reauthenticate(
                            email,
                            reauthPassword,
                            onSuccess = {
                                showReauthDialog = false
                                reauthPassword = ""
                                reauthError = null
                                info = "Ré-authentification réussie, veuillez réessayer l'opération"
                            },
                            onError = {
                                reauthError = it
                            }
                        )
                    }
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReauthDialog = false
                    reauthPassword = ""
                    reauthError = null
                }) { Text("Annuler") }
            }
        )
    }
}
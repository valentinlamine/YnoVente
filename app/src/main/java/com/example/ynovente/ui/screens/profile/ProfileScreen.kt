package com.example.ynovente.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.PaddingValues
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: androidx.navigation.NavHostController,
    onLogout: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil utilisateur") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "Bienvenue sur la page Profil !",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            if (user != null) {
                user?.photoUrl?.let { photoUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = "Photo de profil",
                        modifier = Modifier
                            .size(96.dp)
                            .padding(8.dp)
                    )
                }
                Text("Nom : ${user?.displayName ?: "Non renseigné"}", style = MaterialTheme.typography.bodyLarge)
                Text("Email : ${user?.email ?: "Non renseigné"}", style = MaterialTheme.typography.bodyLarge)
                Text("UID : ${user?.uid}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.signOut()
                        onLogout()
                    }
                ) {
                    Text("Se déconnecter")
                }
            } else {
                Text("Aucun utilisateur connecté.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
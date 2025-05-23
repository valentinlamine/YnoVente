package com.example.ynovente

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ynovente.ui.theme.YnoVenteTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser Firebase (optionnel mais bon à avoir)
        FirebaseApp.initializeApp(this)

        if (FirebaseApp.getApps(this).isNotEmpty()) {
            Log.d("FIREBASE", "✅ Connexion Firebase réussie")
        } else {
            Log.e("FIREBASE", "❌ Connexion Firebase échouée")
        }

        setContent {0
            // Ici tu peux appeler ton composable principal
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YnoVenteTheme {
        Greeting("Android")
    }
}
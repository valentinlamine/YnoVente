package com.example.ynovente.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.ynovente.data.repository.FakeOfferRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, innerPadding: PaddingValues) {
    val pagingSourceFactory = { FakeOfferRepository().getOffersPagingSource() }
    val pager = remember { Pager(PagingConfig(pageSize = 20)) { pagingSourceFactory() } }
    val offers = pager.flow.collectAsLazyPagingItems()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ventes en cours") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(paddingValues)
        ) {
            items(count = offers.itemCount) { index ->
                val offer = offers[index]
                if (offer != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(offer.title, style = MaterialTheme.typography.titleMedium)
                            Text("Prix de départ : ${offer.price} €")
                            Text("Fin de l'enchère : ${offer.endDate}")
                        }
                    }
                }
            }
        }
    }
}
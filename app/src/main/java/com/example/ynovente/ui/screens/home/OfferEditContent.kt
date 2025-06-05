package com.example.ynovente.ui.screens.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ynovente.data.model.Offer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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
                    } catch (_: Exception) { endDate }
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
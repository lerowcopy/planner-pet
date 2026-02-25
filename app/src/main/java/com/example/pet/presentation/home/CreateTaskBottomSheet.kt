package com.example.pet.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * BottomSheet для создания новой задачи.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskBottomSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, day: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var titleText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dayFormatter = SimpleDateFormat("EEEE", Locale("ru"))
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Создать задачу",
            style = MaterialTheme.typography.headlineSmall
        )
        
        OutlinedTextField(
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Название задачи") },
            placeholder = { Text("Введите название задачи") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // Поле для отображения выбранной даты с иконкой календаря
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedDateMillis != null) {
                    val date = Date(selectedDateMillis!!)
                    "Дата: ${dateFormatter.format(date)}"
                } else {
                    "Дата не выбрана (опционально)"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedDateMillis != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Выбрать дату",
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
            
            Button(
                onClick = {
                    if (titleText.text.isNotBlank()) {
                        val day = if (selectedDateMillis != null) {
                            val date = Date(selectedDateMillis!!)
                            dayFormatter.format(date)
                                .replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                                    else it.toString() 
                                }
                        } else {
                            // Если дата не выбрана, используем текущий день
                            dayFormatter.format(Date())
                                .replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                                    else it.toString() 
                                }
                        }
                        onSave(titleText.text.trim(), day)
                        onDismiss()
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = titleText.text.isNotBlank()
            ) {
                Text("Сохранить")
            }
        }
    }
    
    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


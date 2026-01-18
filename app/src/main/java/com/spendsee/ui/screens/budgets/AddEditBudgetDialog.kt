package com.spendsee.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import com.spendsee.data.local.entities.Budget
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    budget: Budget?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, month: Int, year: Int, isRecurring: Boolean, dueDate: Long?, notifyDaysBefore: Int) -> Unit
) {
    var name by remember { mutableStateOf(budget?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(budget?.category ?: "Bills") }
    var selectedMonth by remember { mutableStateOf(budget?.month ?: Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(budget?.year ?: Calendar.getInstance().get(Calendar.YEAR)) }
    var isRecurring by remember { mutableStateOf(budget?.isRecurring ?: false) }
    var hasDueDate by remember { mutableStateOf(budget?.dueDate != null) }
    var dueDate by remember { mutableStateOf(budget?.dueDate ?: System.currentTimeMillis()) }
    var notifyDaysBefore by remember { mutableStateOf(budget?.notifyDaysBefore ?: 5) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNotifyDaysPicker by remember { mutableStateOf(false) }

    val isEdit = budget != null

    val categories = listOf(
        "Bills", "Food", "Transport", "Shopping", "Entertainment",
        "Healthcare", "Education", "Personal", "Other"
    )

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val notifyDaysOptions = listOf(1, 2, 3, 5, 7)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFEFFFFF)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Bar
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEdit) "Edit Budget" else "Add Budget",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(FeatherIcons.X, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFDAF4F3)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    // Budget name input
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("Budget Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(nameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = showCategoryPicker,
                        onExpandedChange = { showCategoryPicker = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryPicker) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = showCategoryPicker,
                            onDismissRequest = { showCategoryPicker = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryPicker = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Month and Year pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Month picker
                        ExposedDropdownMenuBox(
                            expanded = showMonthPicker,
                            onExpandedChange = { showMonthPicker = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = months[selectedMonth - 1],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Month") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthPicker) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showMonthPicker,
                                onDismissRequest = { showMonthPicker = false }
                            ) {
                                months.forEachIndexed { index, month ->
                                    DropdownMenuItem(
                                        text = { Text(month) },
                                        onClick = {
                                            selectedMonth = index + 1
                                            showMonthPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        // Year picker
                        ExposedDropdownMenuBox(
                            expanded = showYearPicker,
                            onExpandedChange = { showYearPicker = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedYear.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Year") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearPicker) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showYearPicker,
                                onDismissRequest = { showYearPicker = false }
                            ) {
                                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                (currentYear - 2..currentYear + 5).forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = {
                                            selectedYear = year
                                            showYearPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recurring toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRecurring = !isRecurring }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recurring Budget",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Automatically copy to next month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Due date toggle and picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hasDueDate = !hasDueDate }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Set Due Date",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (hasDueDate) {
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dueDate)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Switch(
                            checked = hasDueDate,
                            onCheckedChange = { hasDueDate = it }
                        )
                    }

                    if (hasDueDate) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(FeatherIcons.Calendar, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Due Date")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Notify days before picker (Premium feature)
                        ExposedDropdownMenuBox(
                            expanded = showNotifyDaysPicker,
                            onExpandedChange = { showNotifyDaysPicker = it }
                        ) {
                            OutlinedTextField(
                                value = "Notify $notifyDaysBefore days before",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Notification") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showNotifyDaysPicker) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showNotifyDaysPicker,
                                onDismissRequest = { showNotifyDaysPicker = false }
                            ) {
                                notifyDaysOptions.forEach { days ->
                                    DropdownMenuItem(
                                        text = { Text("$days ${if (days == 1) "day" else "days"} before") },
                                        onClick = {
                                            notifyDaysBefore = days
                                            showNotifyDaysPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFDAF4F3),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                when {
                                    name.isBlank() -> {
                                        nameError = "Name is required"
                                    }
                                    else -> {
                                        onSave(
                                            name.trim(),
                                            selectedCategory,
                                            selectedMonth,
                                            selectedYear,
                                            isRecurring,
                                            if (hasDueDate) dueDate else null,
                                            notifyDaysBefore
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF418E8C)
                            )
                        ) {
                            Text(if (isEdit) "Save" else "Add")
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

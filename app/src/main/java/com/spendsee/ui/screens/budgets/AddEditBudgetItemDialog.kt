package com.spendsee.ui.screens.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import com.spendsee.data.local.entities.BudgetItem
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.theme.ThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetItemDialog(
    budgetItem: BudgetItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, note: String, type: String) -> Unit
) {
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    var name by remember { mutableStateOf(budgetItem?.name ?: "") }
    var amount by remember { mutableStateOf(budgetItem?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(budgetItem?.note ?: "") }
    var selectedType by remember { mutableStateOf(budgetItem?.type ?: "Expected") }
    var showTypePicker by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val isEdit = budgetItem != null

    val types = listOf("Expected", "Unplanned")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = currentTheme.getBackground(isDarkMode)
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
                            text = if (isEdit) "Edit Budget Item" else "Add Budget Item",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.getText(isDarkMode)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(FeatherIcons.X, contentDescription = "Close", tint = currentTheme.getText(isDarkMode))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = currentTheme.getSurface(isDarkMode)
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
                    // Item name input
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("Item Name") },
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

                    // Amount input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            amountError = null
                        },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = amountError != null,
                        supportingText = {
                            if (amountError != null) {
                                Text(amountError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        prefix = { Text(selectedCurrency.symbol) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Type dropdown
                    ExposedDropdownMenuBox(
                        expanded = showTypePicker,
                        onExpandedChange = { showTypePicker = it }
                    ) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypePicker) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = showTypePicker,
                            onDismissRequest = { showTypePicker = false }
                        ) {
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedType = type
                                        showTypePicker = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Note input
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = currentTheme.getSurface(isDarkMode),
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = currentTheme.getText(isDarkMode)
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                var hasError = false

                                if (name.isBlank()) {
                                    nameError = "Name is required"
                                    hasError = true
                                }

                                if (amount.isBlank()) {
                                    amountError = "Amount is required"
                                    hasError = true
                                } else {
                                    try {
                                        amount.toDouble()
                                    } catch (e: NumberFormatException) {
                                        amountError = "Invalid amount"
                                        hasError = true
                                    }
                                }

                                if (!hasError) {
                                    onSave(
                                        name.trim(),
                                        amount.toDouble(),
                                        note.trim(),
                                        selectedType
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.getAccent(isDarkMode),
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isEdit) "Save" else "Add")
                        }
                    }
                }
            }
        }
    }
}

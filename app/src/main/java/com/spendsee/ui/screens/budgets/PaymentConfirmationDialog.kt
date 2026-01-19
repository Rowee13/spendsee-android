package com.spendsee.ui.screens.budgets

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import compose.icons.feathericons.ChevronRight
import com.spendsee.data.local.entities.Account
import com.spendsee.data.local.entities.Budget
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentConfirmationDialog(
    budget: Budget,
    plannedAmount: Double,
    accounts: List<Account>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Double,
        accountId: String,
        date: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    var actualAmount by remember { mutableStateOf(String.format("%.2f", plannedAmount)) }
    var selectedAccount by remember { mutableStateOf<Account?>(accounts.firstOrNull()) }
    var date by remember { mutableStateOf(budget.dueDate ?: System.currentTimeMillis()) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isFormValid = remember(actualAmount, selectedAccount) {
        val amount = actualAmount.toDoubleOrNull()
        amount != null && amount > 0 && selectedAccount != null
    }

    val balanceAfterPayment = remember(actualAmount, selectedAccount) {
        val amount = actualAmount.toDoubleOrNull() ?: 0.0
        (selectedAccount?.balance ?: 0.0) - amount
    }

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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = currentTheme.getSurface(isDarkMode),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", fontSize = 17.sp, color = currentTheme.getAccent(isDarkMode))
                        }

                        Text(
                            text = "Confirm Payment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getText(isDarkMode)
                        )

                        TextButton(
                            onClick = {
                                val amount = actualAmount.toDoubleOrNull()
                                if (amount != null && amount > 0 && selectedAccount != null) {
                                    onConfirm(amount, selectedAccount!!.id, date)
                                }
                            },
                            enabled = isFormValid
                        ) {
                            Text(
                                "Create Transaction",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFormValid) currentTheme.getAccent(isDarkMode) else currentTheme.getInactive(isDarkMode)
                            )
                        }
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Budget Information Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Budget Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getAccent(isDarkMode)
                        )

                        InfoRow("Budget", budget.name, currentTheme.getInactive(isDarkMode), currentTheme.getText(isDarkMode))
                        InfoRow("Category", budget.category, currentTheme.getInactive(isDarkMode), currentTheme.getText(isDarkMode))
                        InfoRow("Budget Amount", "$currencySymbol${String.format("%.2f", plannedAmount)}", currentTheme.getInactive(isDarkMode), currentTheme.getText(isDarkMode))
                    }

                    Divider()

                    // Actual Amount Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Actual Amount",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getText(isDarkMode)
                        )
                        Text(
                            text = "Edit if the actual payment amount differs from the budget amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getInactive(isDarkMode)
                        )
                        OutlinedTextField(
                            value = actualAmount,
                            onValueChange = { actualAmount = it },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Text(currencySymbol, color = currentTheme.getText(isDarkMode)) },
                            placeholder = { Text("Amount", color = currentTheme.getInactive(isDarkMode)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = currentTheme.getSurface(isDarkMode),
                                focusedContainerColor = currentTheme.getSurface(isDarkMode),
                                unfocusedTextColor = currentTheme.getText(isDarkMode),
                                focusedTextColor = currentTheme.getText(isDarkMode),
                                unfocusedBorderColor = currentTheme.getBorder(isDarkMode),
                                focusedBorderColor = currentTheme.getAccent(isDarkMode)
                            )
                        )
                    }

                    Divider()

                    // Account Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Account",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getText(isDarkMode)
                        )

                        if (accounts.isEmpty()) {
                            Text(
                                text = "No accounts available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = currentTheme.getInactive(isDarkMode)
                            )
                        } else {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAccountPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = currentTheme.getSurface(isDarkMode)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAccount?.name ?: "Select Account",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedAccount != null)
                                            currentTheme.getText(isDarkMode)
                                        else
                                            currentTheme.getInactive(isDarkMode)
                                    )
                                    Icon(
                                        imageVector = FeatherIcons.ChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = currentTheme.getInactive(isDarkMode)
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Date Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Payment Date",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getText(isDarkMode)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = currentTheme.getSurface(isDarkMode),
                                    contentColor = currentTheme.getText(isDarkMode)
                                )
                            ) {
                                Text(
                                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(date)),
                                    fontSize = 15.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = currentTheme.getSurface(isDarkMode),
                                    contentColor = currentTheme.getText(isDarkMode)
                                )
                            ) {
                                Text(
                                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(date)),
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    // Balance After Payment
                    if (selectedAccount != null) {
                        Divider()
                        
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (balanceAfterPayment < 0)
                                    Color.Red.copy(alpha = 0.1f)
                                else
                                    currentTheme.getSurface(isDarkMode)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Account Balance After",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = currentTheme.getText(isDarkMode)
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", balanceAfterPayment)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balanceAfterPayment >= 0)
                                        currentTheme.getText(isDarkMode)
                                    else
                                        Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Account Picker Dialog
    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("Select Account") },
            text = {
                Column {
                    accounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccount = account
                                    showAccountPicker = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", account.balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (selectedAccount?.id == account.id) {
                                Icon(
                                    imageVector = FeatherIcons.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { date = it }
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

    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        date = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, labelColor: Color = Color.Unspecified, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

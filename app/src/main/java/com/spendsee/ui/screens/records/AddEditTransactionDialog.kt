package com.spendsee.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import com.spendsee.data.local.entities.Transaction
import com.spendsee.data.local.entities.Account
import com.spendsee.data.local.entities.Category
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    transaction: Transaction?,
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        notes: String,
        accountId: String?,
        toAccountId: String?
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(transaction?.type ?: "expense") }
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(transaction?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(transaction?.category ?: "") }
    var selectedAccountId by remember { mutableStateOf(transaction?.accountId ?: accounts.firstOrNull()?.id) }
    var selectedToAccountId by remember { mutableStateOf(transaction?.toAccountId) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(transaction?.notes ?: "") }

    var amountError by remember { mutableStateOf<String?>(null) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var accountError by remember { mutableStateOf<String?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showToAccountPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEdit = transaction != null

    // Filter categories by type
    val filteredCategories = categories.filter {
        when (selectedType) {
            "income" -> it.type == "income"
            "expense" -> it.type == "expense"
            "transfer" -> false
            else -> false
        }
    }

    // Auto-select first category when type changes
    LaunchedEffect(selectedType) {
        if (selectedType != "transfer") {
            if (selectedCategory.isEmpty() || !filteredCategories.any { it.name == selectedCategory }) {
                selectedCategory = filteredCategories.firstOrNull()?.name ?: ""
            }
        }
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
            color = MaterialTheme.colorScheme.background
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
                            text = if (isEdit) "Edit Transaction" else "Add Transaction",
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
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    // Transaction Type Selector (iOS segmented control style)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SegmentedButton(
                                text = "Expense",
                                isSelected = selectedType == "expense",
                                onClick = { selectedType = "expense" },
                                modifier = Modifier.weight(1f),
                                color = Color(0xFFEF5350)
                            )
                            SegmentedButton(
                                text = "Income",
                                isSelected = selectedType == "income",
                                onClick = { selectedType = "income" },
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF66BB6A)
                            )
                            SegmentedButton(
                                text = "Transfer",
                                isSelected = selectedType == "transfer",
                                onClick = { selectedType = "transfer" },
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF42A5F5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Amount Card (iOS style - large and prominent)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Amount",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "$",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                    color = when (selectedType) {
                                        "income" -> Color(0xFF66BB6A)
                                        "expense" -> Color(0xFFEF5350)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = {
                                        amount = it
                                        amountError = null
                                    },
                                    modifier = Modifier.width(200.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (selectedType) {
                                            "income" -> Color(0xFF66BB6A)
                                            "expense" -> Color(0xFFEF5350)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        textAlign = TextAlign.Center
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    placeholder = {
                                        Text(
                                            "0.00",
                                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                    }
                                )
                            }
                            if (amountError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    amountError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = null
                        },
                        label = { Text("Description") },
                        placeholder = { Text(if (selectedType == "transfer") "Transfer" else "What did you spend on?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = titleError != null,
                        supportingText = titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category (only for income/expense)
                    if (selectedType != "transfer") {
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
                                    .menuAnchor(),
                                isError = categoryError != null,
                                supportingText = categoryError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                            )

                            ExposedDropdownMenu(
                                expanded = showCategoryPicker,
                                onDismissRequest = { showCategoryPicker = false }
                            ) {
                                filteredCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            Color(android.graphics.Color.parseColor(category.colorHex))
                                                                .copy(alpha = 0.2f)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = getCategoryIcon(category.icon),
                                                        contentDescription = null,
                                                        tint = Color(android.graphics.Color.parseColor(category.colorHex)),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(category.name)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category.name
                                            showCategoryPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Account Picker
                    ExposedDropdownMenuBox(
                        expanded = showAccountPicker,
                        onExpandedChange = { showAccountPicker = it }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (selectedType == "transfer") "From Account" else "Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAccountPicker) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            isError = accountError != null,
                            supportingText = accountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                        )

                        ExposedDropdownMenu(
                            expanded = showAccountPicker,
                            onDismissRequest = { showAccountPicker = false }
                        ) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(account.name)
                                            Text(
                                                text = "$${String.format("%.2f", account.balance)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAccountId = account.id
                                        showAccountPicker = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // To Account (only for transfers)
                    if (selectedType == "transfer") {
                        ExposedDropdownMenuBox(
                            expanded = showToAccountPicker,
                            onExpandedChange = { showToAccountPicker = it }
                        ) {
                            OutlinedTextField(
                                value = accounts.find { it.id == selectedToAccountId }?.name ?: "Select Account",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToAccountPicker) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showToAccountPicker,
                                onDismissRequest = { showToAccountPicker = false }
                            ) {
                                accounts.filter { it.id != selectedAccountId }.forEach { account ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(account.name)
                                                Text(
                                                    text = "$${String.format("%.2f", account.balance)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedToAccountId = account.id
                                            showToAccountPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Date Picker
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(FeatherIcons.Calendar, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(date)))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
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
                                var hasError = false

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

                                if (title.isBlank()) {
                                    titleError = "Description is required"
                                    hasError = true
                                }

                                if (selectedType != "transfer" && selectedCategory.isEmpty()) {
                                    categoryError = "Category is required"
                                    hasError = true
                                }

                                if (selectedAccountId == null) {
                                    accountError = "Account is required"
                                    hasError = true
                                }

                                if (selectedType == "transfer" && selectedToAccountId == null) {
                                    accountError = "To Account is required"
                                    hasError = true
                                }

                                if (!hasError) {
                                    onSave(
                                        title.trim(),
                                        amount.toDouble(),
                                        selectedType,
                                        if (selectedType == "transfer") "" else selectedCategory,
                                        date,
                                        notes.trim(),
                                        selectedAccountId,
                                        selectedToAccountId
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
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
            initialSelectedDateMillis = date
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            date = it
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

@Composable
fun SegmentedButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getCategoryIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.lowercase()) {
        "grid" -> FeatherIcons.Grid
        "briefcase" -> FeatherIcons.Briefcase
        "coffee" -> FeatherIcons.Coffee
        "shoppingcart" -> FeatherIcons.ShoppingCart
        "film" -> FeatherIcons.Film
        "creditcard" -> FeatherIcons.CreditCard
        "heart" -> FeatherIcons.Heart
        "book" -> FeatherIcons.Book
        "mappin" -> FeatherIcons.MapPin
        "home" -> FeatherIcons.Home
        "smartphone" -> FeatherIcons.Smartphone
        "shield" -> FeatherIcons.Shield
        "bell" -> FeatherIcons.Bell
        "droplet" -> FeatherIcons.Droplet
        "gift" -> FeatherIcons.Gift
        "star" -> FeatherIcons.Star
        "truck" -> FeatherIcons.Truck
        "camera" -> FeatherIcons.Camera
        "music" -> FeatherIcons.Music
        "tool" -> FeatherIcons.Tool
        "activity" -> FeatherIcons.Activity
        "dollarsign" -> FeatherIcons.DollarSign
        "trendingup" -> FeatherIcons.TrendingUp
        "package" -> FeatherIcons.Package
        else -> FeatherIcons.Grid
    }
}

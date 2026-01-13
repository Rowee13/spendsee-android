package com.spendsee.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
            "transfer" -> false // Transfers don't use categories
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

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    // Transaction Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TransactionTypeButton(
                            text = "Expense",
                            icon = FeatherIcons.TrendingDown,
                            color = Color(0xFFEF5350),
                            isSelected = selectedType == "expense",
                            onClick = { selectedType = "expense" },
                            modifier = Modifier.weight(1f)
                        )
                        TransactionTypeButton(
                            text = "Income",
                            icon = FeatherIcons.TrendingUp,
                            color = Color(0xFF66BB6A),
                            isSelected = selectedType == "income",
                            onClick = { selectedType = "income" },
                            modifier = Modifier.weight(1f)
                        )
                        TransactionTypeButton(
                            text = "Transfer",
                            icon = FeatherIcons.ArrowRightCircle,
                            color = Color(0xFF42A5F5),
                            isSelected = selectedType == "transfer",
                            onClick = { selectedType = "transfer" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                        prefix = { Text("$", style = MaterialTheme.typography.titleLarge) },
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title input
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = null
                        },
                        label = { Text("Title") },
                        placeholder = { Text(if (selectedType == "transfer") "Transfer" else "Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = titleError != null,
                        supportingText = {
                            if (titleError != null) {
                                Text(titleError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category picker (only for income/expense)
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
                                supportingText = {
                                    if (categoryError != null) {
                                        Text(categoryError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                }
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Account picker (From account)
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
                            supportingText = {
                                if (accountError != null) {
                                    Text(accountError!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
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
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // To Account picker (only for transfers)
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
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Date picker
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(FeatherIcons.Calendar, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(date)))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes input
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
                                    titleError = "Title is required"
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
fun TransactionTypeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

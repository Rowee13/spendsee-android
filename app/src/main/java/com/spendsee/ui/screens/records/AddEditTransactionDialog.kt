package com.spendsee.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var calculatorDisplay by remember {
        mutableStateOf(
            if (transaction?.amount != null && transaction.amount > 0.0) {
                // Format amount: remove ".0" if whole number
                if (transaction.amount % 1.0 == 0.0) {
                    transaction.amount.toInt().toString()
                } else {
                    String.format("%.2f", transaction.amount)
                }
            } else {
                "0"
            }
        )
    }
    var title by remember { mutableStateOf(transaction?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(transaction?.category ?: "") }
    var selectedAccountId by remember { mutableStateOf(transaction?.accountId ?: accounts.firstOrNull()?.id) }
    var selectedToAccountId by remember { mutableStateOf(transaction?.toAccountId) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(transaction?.notes ?: "") }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showToAccountPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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

    // Calculator functions
    fun handleCalculatorInput(input: String) {
        when (input) {
            "C" -> calculatorDisplay = "0"
            "⌫" -> {
                calculatorDisplay = if (calculatorDisplay.length > 1) {
                    calculatorDisplay.dropLast(1)
                } else {
                    "0"
                }
            }
            "=" -> {
                try {
                    val result = evaluateExpression(calculatorDisplay)
                    calculatorDisplay = if (result % 1.0 == 0.0) {
                        result.toInt().toString()
                    } else {
                        String.format("%.2f", result)
                    }
                } catch (e: Exception) {
                    calculatorDisplay = "Error"
                }
            }
            else -> {
                if (calculatorDisplay == "0" && input !in listOf("+", "-", "×", "÷", ".")) {
                    calculatorDisplay = input
                } else {
                    calculatorDisplay += input
                }
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
            color = Color(0xFFEFFFFF)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Navigation Bar (iOS style)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color(0xFFAAD4D3)),
                    color = Color(0xFFDAF4F3),
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
                            Text("Cancel", fontSize = 17.sp)
                        }

                        Text(
                            text = if (isEdit) "Edit Record" else "Add Record",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        TextButton(
                            onClick = {
                                val finalAmount = try {
                                    val evaluated = evaluateExpression(calculatorDisplay)
                                    if (evaluated > 0) evaluated else return@TextButton
                                } catch (e: Exception) {
                                    return@TextButton
                                }

                                if (title.isNotBlank() &&
                                    (selectedType == "transfer" || selectedCategory.isNotBlank()) &&
                                    selectedAccountId != null &&
                                    (selectedType != "transfer" || selectedToAccountId != null)) {
                                    onSave(
                                        title.trim(),
                                        finalAmount,
                                        selectedType,
                                        if (selectedType == "transfer") "" else selectedCategory,
                                        date,
                                        notes.trim(),
                                        selectedAccountId,
                                        selectedToAccountId
                                    )
                                }
                            }
                        ) {
                            Text("Save", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Transaction Type Selector (iOS style - simple text buttons with checkmark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TypeButton(
                            text = "Income",
                            isSelected = selectedType == "income",
                            onClick = { selectedType = "income" }
                        )
                        TypeButton(
                            text = "Expense",
                            isSelected = selectedType == "expense",
                            onClick = { selectedType = "expense" }
                        )
                        TypeButton(
                            text = "Transfer",
                            isSelected = selectedType == "transfer",
                            onClick = { selectedType = "transfer" }
                        )
                    }

                    // Account and Category Row (side by side)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Account
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedType == "transfer") "From Account" else "Account",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedButton(
                                onClick = { showAccountPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account",
                                    fontSize = 15.sp,
                                    color = if (selectedAccountId == null)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Category (hide for transfer)
                        if (selectedType != "transfer") {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Category",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedButton(
                                    onClick = { showCategoryPicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text(
                                        text = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                                        fontSize = 15.sp,
                                        color = if (selectedCategory.isEmpty())
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        } else {
                            // To Account for transfer
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "To Account",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedButton(
                                    onClick = { showToAccountPicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text(
                                        text = accounts.find { it.id == selectedToAccountId }?.name ?: "Select Account",
                                        fontSize = 15.sp,
                                        color = if (selectedToAccountId == null)
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Title Field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Notes Field
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Calculator Display
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = calculatorDisplay,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { handleCalculatorInput("⌫") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = FeatherIcons.X,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Calculator Keypad (4x4 grid)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Row 1: +, 7, 8, 9
                        CalculatorRow(
                            buttons = listOf("+", "7", "8", "9"),
                            onButtonClick = { handleCalculatorInput(it) }
                        )
                        // Row 2: -, 4, 5, 6
                        CalculatorRow(
                            buttons = listOf("-", "4", "5", "6"),
                            onButtonClick = { handleCalculatorInput(it) }
                        )
                        // Row 3: ×, 1, 2, 3
                        CalculatorRow(
                            buttons = listOf("×", "1", "2", "3"),
                            onButtonClick = { handleCalculatorInput(it) }
                        )
                        // Row 4: ÷, 0, ., =
                        CalculatorRow(
                            buttons = listOf("÷", "0", ".", "="),
                            onButtonClick = { handleCalculatorInput(it) }
                        )
                    }

                    // Date and Time Row (side by side)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
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
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(date)),
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Account Picker Dialog
    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text(if (selectedType == "transfer") "Select From Account" else "Select Account") },
            text = {
                Column {
                    accounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccountId = account.id
                                    showAccountPicker = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(account.name)
                            Text(
                                text = "$${String.format("%.2f", account.balance)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

    // To Account Picker Dialog
    if (showToAccountPicker) {
        AlertDialog(
            onDismissRequest = { showToAccountPicker = false },
            title = { Text("Select To Account") },
            text = {
                Column {
                    accounts.filter { it.id != selectedAccountId }.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedToAccountId = account.id
                                    showToAccountPicker = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(account.name)
                            Text(
                                text = "$${String.format("%.2f", account.balance)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showToAccountPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    filteredCategories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = category.name
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Close")
                }
            }
        )
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

    // Time Picker Dialog (simplified - just updates the time portion of the date)
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
fun TypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CalculatorRow(
    buttons: List<String>,
    onButtonClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttons.forEach { button ->
            OutlinedButton(
                onClick = { onButtonClick(button) },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = button,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// Simple expression evaluator for calculator
fun evaluateExpression(expression: String): Double {
    if (expression == "0" || expression.isEmpty()) return 0.0

    try {
        var exp = expression
            .replace("×", "*")
            .replace("÷", "/")

        // Simple evaluation - handle one operation at a time
        val tokens = mutableListOf<String>()
        var currentNumber = ""

        for (char in exp) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else if (char in listOf('+', '-', '*', '/')) {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber)
                    currentNumber = ""
                }
                tokens.add(char.toString())
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber)
        }

        if (tokens.size == 1) {
            return tokens[0].toDoubleOrNull() ?: 0.0
        }

        // Evaluate multiplication and division first
        var i = 1
        while (i < tokens.size) {
            if (tokens[i] == "*" || tokens[i] == "/") {
                val left = tokens[i - 1].toDouble()
                val right = tokens[i + 1].toDouble()
                val result = if (tokens[i] == "*") left * right else left / right
                tokens[i - 1] = result.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
            } else {
                i += 2
            }
        }

        // Evaluate addition and subtraction
        i = 1
        while (i < tokens.size) {
            if (tokens[i] == "+" || tokens[i] == "-") {
                val left = tokens[i - 1].toDouble()
                val right = tokens[i + 1].toDouble()
                val result = if (tokens[i] == "+") left + right else left - right
                tokens[i - 1] = result.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
            } else {
                i += 2
            }
        }

        return tokens[0].toDouble()
    } catch (e: Exception) {
        return 0.0
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

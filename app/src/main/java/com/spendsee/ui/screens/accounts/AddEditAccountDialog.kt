package com.spendsee.ui.screens.accounts

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
import com.spendsee.data.local.entities.Account
import androidx.compose.ui.platform.LocalContext
import com.spendsee.managers.CurrencyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, balance: Double, icon: String, color: String) -> Unit
) {
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()

    var name by remember { mutableStateOf(account?.name ?: "") }
    var selectedType by remember { mutableStateOf(account?.type ?: "cash") }
    var balance by remember { mutableStateOf(account?.balance?.toString() ?: "") }
    var selectedIcon by remember { mutableStateOf(account?.icon ?: "dollarsign") }
    var selectedColor by remember { mutableStateOf(account?.colorHex ?: "#007AFF") }
    var expanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var balanceError by remember { mutableStateOf<String?>(null) }

    val isEdit = account != null

    val accountTypes = listOf(
        "cash" to "Cash",
        "bank" to "Bank",
        "creditCard" to "Credit Card",
        "ewallet" to "E-Wallet",
        "savings" to "Savings",
        "investment" to "Investment",
        "other" to "Other"
    )

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
                            text = if (isEdit) "Edit Account" else "Add Account",
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
                    // Account name input
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = { Text("Account Name") },
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

                    // Account type dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = accountTypes.find { it.first == selectedType }?.second ?: "Cash",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            accountTypes.forEach { (type, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Balance input
                    OutlinedTextField(
                        value = balance,
                        onValueChange = {
                            balance = it
                            balanceError = null
                        },
                        label = { Text("Initial Balance") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = balanceError != null,
                        supportingText = {
                            if (balanceError != null) {
                                Text(balanceError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        prefix = { Text(selectedCurrency.symbol) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Preview
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Color(android.graphics.Color.parseColor(selectedColor))
                                            .copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getAccountIcon(selectedIcon),
                                    contentDescription = null,
                                    tint = Color(android.graphics.Color.parseColor(selectedColor)),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = name.ifEmpty { "Account Name" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (name.isEmpty())
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedCurrency.symbol}${balance.ifEmpty { "0.00" }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon picker
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AccountIconPicker(
                        selectedIcon = selectedIcon,
                        onIconSelected = { selectedIcon = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color picker
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AccountColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )

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
                                var hasError = false

                                if (name.isBlank()) {
                                    nameError = "Name is required"
                                    hasError = true
                                }

                                if (balance.isBlank()) {
                                    balanceError = "Balance is required"
                                    hasError = true
                                } else {
                                    try {
                                        balance.toDouble()
                                    } catch (e: NumberFormatException) {
                                        balanceError = "Invalid amount"
                                        hasError = true
                                    }
                                }

                                if (!hasError) {
                                    onSave(
                                        name.trim(),
                                        selectedType,
                                        balance.toDouble(),
                                        selectedIcon,
                                        selectedColor
                                    )
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountIconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    val icons = listOf(
        "dollarsign" to FeatherIcons.DollarSign,
        "creditcard" to FeatherIcons.CreditCard,
        "briefcase" to FeatherIcons.Briefcase,
        "home" to FeatherIcons.Home,
        "smartphone" to FeatherIcons.Smartphone,
        "shield" to FeatherIcons.Shield,
        "trendingup" to FeatherIcons.TrendingUp,
        "package" to FeatherIcons.Package,
        "gift" to FeatherIcons.Gift,
        "star" to FeatherIcons.Star,
        "heart" to FeatherIcons.Heart,
        "droplet" to FeatherIcons.Droplet,
        "truck" to FeatherIcons.Truck,
        "shoppingcart" to FeatherIcons.ShoppingCart,
        "coffee" to FeatherIcons.Coffee,
        "camera" to FeatherIcons.Camera
    )

    // Use FlowRow for better wrapping and distribution
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 6
    ) {
        icons.forEach { (iconName, iconVector) ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedIcon == iconName)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        width = if (selectedIcon == iconName) 2.dp else 0.dp,
                        color = if (selectedIcon == iconName)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(iconName) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = iconName,
                    tint = if (selectedIcon == iconName)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#007AFF", "#34C759", "#FF9500", "#FF3B30",
        "#AF52DE", "#FF2D55", "#5AC8FA", "#FFCC00",
        "#FF6482", "#32ADE6", "#BF5AF2", "#00C7BE"
    )

    // Use FlowRow for better distribution
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 6
    ) {
        colors.forEach { colorHex ->
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .border(
                        width = if (selectedColor == colorHex) 3.dp else 0.dp,
                        color = if (selectedColor == colorHex)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == colorHex) {
                    Icon(
                        imageVector = FeatherIcons.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

private fun getAccountIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.lowercase()) {
        "dollarsign" -> FeatherIcons.DollarSign
        "creditcard" -> FeatherIcons.CreditCard
        "briefcase" -> FeatherIcons.Briefcase
        "home" -> FeatherIcons.Home
        "smartphone" -> FeatherIcons.Smartphone
        "shield" -> FeatherIcons.Shield
        "trendingup" -> FeatherIcons.TrendingUp
        "package" -> FeatherIcons.Package
        "gift" -> FeatherIcons.Gift
        "star" -> FeatherIcons.Star
        "heart" -> FeatherIcons.Heart
        "droplet" -> FeatherIcons.Droplet
        "truck" -> FeatherIcons.Truck
        "shoppingcart" -> FeatherIcons.ShoppingCart
        "coffee" -> FeatherIcons.Coffee
        "camera" -> FeatherIcons.Camera
        else -> FeatherIcons.DollarSign
    }
}

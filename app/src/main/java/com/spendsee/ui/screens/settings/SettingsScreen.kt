package com.spendsee.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendsee.R
import com.spendsee.managers.BackupManager
import com.spendsee.managers.PremiumManager
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.ThemeManager
import com.spendsee.utils.Currency
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.ui.screens.categories.CategoriesScreen
import com.spendsee.ui.theme.AppColorScheme
import com.spendsee.ui.theme.AppColorSchemes
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val backupManager = remember { BackupManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val selectedTheme by themeManager.selectedTheme.collectAsState()
    var isDeveloperMode by remember { mutableStateOf(premiumManager.isDeveloperModeEnabled()) }
    var showDeveloperMode by remember { mutableStateOf(premiumManager.isDeveloperModeEnabled()) }
    var showPremiumPaywall by remember { mutableStateOf(false) }
    var showCategoriesScreen by remember { mutableStateOf(false) }
    var showCurrencySelector by remember { mutableStateOf(false) }
    var showThemeSelector by remember { mutableStateOf(false) }
    var showPurchaseDetails by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableStateOf(0) }

    // Export backup launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    isExporting = true
                    val (_, jsonContent) = backupManager.exportBackup()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonContent.toByteArray())
                    }
                    Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isExporting = false
                }
            }
        }
    }

    // Import backup launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    isImporting = true
                    val result = backupManager.importBackup(uri)
                    if (result.success) {
                        val message = buildString {
                            append("Import successful!\n")
                            append("Transactions: ${result.transactionsImported}\n")
                            append("Accounts: ${result.accountsImported}\n")
                            append("Budgets: ${result.budgetsImported}\n")
                            append("Categories: ${result.categoriesImported}")
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Import failed: ${result.errors.firstOrNull()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isImporting = false
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        SettingsHeader()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Preferences Section
            item {
                SettingsSection(title = "Preferences")
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Droplet,
                    title = "Theme",
                    subtitle = AppColorSchemes.themeById(selectedTheme).name,
                    onClick = { showThemeSelector = true }
                )
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.DollarSign,
                    title = "Currency",
                    subtitle = "${selectedCurrency.name} (${selectedCurrency.symbol})",
                    onClick = { showCurrencySelector = true }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = FeatherIcons.Moon,
                    title = "Dark Mode",
                    subtitle = if (isDarkMode) "Enabled" else "Disabled",
                    checked = isDarkMode,
                    onCheckedChange = { themeManager.setDarkMode(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Data Management Section
            item {
                SettingsSection(title = "Data Management")
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Upload,
                    title = "Backup",
                    subtitle = if (isExporting) "Exporting..." else "Export your data",
                    onClick = {
                        if (!isExporting) {
                            scope.launch {
                                val (filename, _) = backupManager.exportBackup()
                                exportLauncher.launch(filename)
                            }
                        }
                    }
                )
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Download,
                    title = "Restore",
                    subtitle = if (isImporting) "Importing..." else "Import your data",
                    onClick = {
                        if (!isImporting) {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // App Section
            item {
                SettingsSection(title = "App")
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Grid,
                    title = "Categories",
                    subtitle = "Manage your categories",
                    onClick = { showCategoriesScreen = true }
                )
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.HelpCircle,
                    title = "Help & Support",
                    subtitle = "Get help and documentation",
                    onClick = { /* TODO: Implement help */ }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Premium Section
            item {
                SettingsSection(title = "Premium")
            }

            item {
                if (!isPremium) {
                    SettingsItem(
                        icon = FeatherIcons.Star,
                        title = "Unlock Premium",
                        subtitle = "Get all features",
                        onClick = { showPremiumPaywall = true },
                        isPremiumFeature = true
                    )
                } else {
                    SettingsItem(
                        icon = FeatherIcons.CheckCircle,
                        title = "Premium Active",
                        subtitle = "Tap to view purchase details",
                        onClick = { showPurchaseDetails = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Security Section (Premium)
            item {
                SettingsSection(title = "Security")
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Lock,
                    title = "Passcode",
                    subtitle = "Protect your data",
                    onClick = { /* TODO: Implement passcode setup */ },
                    isPremiumFeature = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Notifications Section (Premium)
            item {
                SettingsSection(title = "Notifications")
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Bell,
                    title = "Budget Reminders",
                    subtitle = "Get notified about upcoming payments",
                    onClick = { /* TODO: Implement notifications toggle */ },
                    isPremiumFeature = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // About Section
            item {
                SettingsSection(title = "About")
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Info,
                    title = "Version",
                    subtitle = if (showDeveloperMode) "1.0.0 (Developer Mode)" else "1.0.0",
                    onClick = {
                        if (!showDeveloperMode) {
                            versionTapCount++
                            if (versionTapCount >= 7) {
                                showDeveloperMode = true
                                Toast.makeText(context, "Developer mode unlocked!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Mail,
                    title = "Support",
                    subtitle = "Contact us",
                    onClick = { /* TODO: Open email */ }
                )
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.FileText,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = { /* TODO: Open privacy policy */ }
                )
            }

            // Developer Mode Section (Debug builds only)
            if (showDeveloperMode || isDeveloperMode) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    SettingsSection(title = "Developer")
                }

                item {
                    SettingsSwitchItem(
                        icon = FeatherIcons.Code,
                        title = "Premium Override",
                        subtitle = "Enable all premium features (debug only)",
                        checked = isDeveloperMode,
                        onCheckedChange = { enabled ->
                            premiumManager.setDeveloperMode(enabled)
                            isDeveloperMode = enabled
                            Toast.makeText(
                                context,
                                if (enabled) "Premium features unlocked" else "Premium features locked",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }

    // Show Premium Paywall
    if (showPremiumPaywall) {
        PremiumPaywallScreen(
            onDismiss = { showPremiumPaywall = false },
            onPurchaseSuccess = {
                showPremiumPaywall = false
                // Premium status will be updated automatically via StateFlow
            }
        )
    }

    // Show Categories Screen
    if (showCategoriesScreen) {
        CategoriesScreen(
            onBackClick = { showCategoriesScreen = false }
        )
    }

    // Show Currency Selector
    if (showCurrencySelector) {
        CurrencySelectorDialog(
            currentCurrency = selectedCurrency,
            onCurrencySelected = { currency ->
                currencyManager.setCurrency(currency)
                showCurrencySelector = false
            },
            onDismiss = { showCurrencySelector = false }
        )
    }

    // Show Theme Selector
    if (showThemeSelector) {
        ThemeSelectorDialog(
            currentTheme = selectedTheme,
            isPremium = isPremium,
            onThemeSelected = { theme ->
                if (!theme.isPremium || isPremium) {
                    themeManager.setTheme(theme.id)
                    showThemeSelector = false
                } else {
                    showThemeSelector = false
                    showPremiumPaywall = true
                }
            },
            onDismiss = { showThemeSelector = false }
        )
    }

    // Show Purchase Details
    if (showPurchaseDetails) {
        val purchaseDetails by premiumManager.purchaseDetails.collectAsState()
        PurchaseDetailsDialog(
            purchaseDetails = purchaseDetails,
            isDeveloperMode = isDeveloperMode,
            onRestorePurchases = {
                premiumManager.restorePurchases()
                Toast.makeText(context, "Checking for purchases...", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPurchaseDetails = false }
        )
    }
}

@Composable
fun SettingsHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Settings",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isPremiumFeature: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (isPremiumFeature) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = FeatherIcons.Star,
                                contentDescription = "Premium",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = FeatherIcons.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun CurrencySelectorDialog(
    currentCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(Currency.ALL_CURRENCIES.size) { index ->
                    val currency = Currency.ALL_CURRENCIES[index]
                    val isSelected = currency.code == currentCurrency.code

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(currency) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currency.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(40.dp)
                            )
                            Column {
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = FeatherIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (index < Currency.ALL_CURRENCIES.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ThemeSelectorDialog(
    currentTheme: String,
    isPremium: Boolean,
    onThemeSelected: (AppColorScheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AppColorSchemes.allThemes.size) { index ->
                    val theme = AppColorSchemes.allThemes[index]
                    val isSelected = theme.id == currentTheme
                    val isLocked = theme.isPremium && !isPremium

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isLocked) { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Color preview circles
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryLight)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentLight)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = theme.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (theme.isPremium) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = FeatherIcons.Star,
                                            contentDescription = "Premium",
                                            tint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        when {
                            isLocked -> Icon(
                                imageVector = FeatherIcons.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            isSelected -> Icon(
                                imageVector = FeatherIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (index < AppColorSchemes.allThemes.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PurchaseDetailsDialog(
    purchaseDetails: PremiumManager.PurchaseDetails?,
    isDeveloperMode: Boolean,
    onRestorePurchases: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FeatherIcons.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Premium Active",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDeveloperMode && purchaseDetails == null) {
                    // Developer mode message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Developer Mode Enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All premium features are unlocked for testing. This is not a real purchase.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (purchaseDetails != null) {
                    // Purchase details
                    PurchaseDetailItem(
                        label = "Status",
                        value = "Active",
                        icon = FeatherIcons.CheckCircle
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PurchaseDetailItem(
                        label = "Product",
                        value = "SpendSee Premium",
                        icon = FeatherIcons.Star
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PurchaseDetailItem(
                        label = "Purchase Date",
                        value = formatPurchaseDate(purchaseDetails.purchaseTime),
                        icon = FeatherIcons.Calendar
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PurchaseDetailItem(
                        label = "Order ID",
                        value = purchaseDetails.orderId,
                        icon = FeatherIcons.FileText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Restore purchases button
                    OutlinedButton(
                        onClick = onRestorePurchases,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = FeatherIcons.RefreshCw,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore Purchases")
                    }
                } else {
                    // No purchase found (shouldn't happen if isPremium is true)
                    Text(
                        text = "No purchase information available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PurchaseDetailItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatPurchaseDate(timestamp: Long): String {
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timestamp))
}

package com.spendsee.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsee.R
import com.spendsee.managers.BackupManager
import com.spendsee.managers.BudgetNotificationManager
import com.spendsee.managers.PremiumManager
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.ThemeManager
import com.spendsee.managers.PasscodeManager
import com.spendsee.utils.Currency
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.ui.screens.categories.CategoriesScreen
import com.spendsee.ui.screens.security.PasscodeLockScreen
import com.spendsee.ui.screens.security.PasscodeMode
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
    val notificationManager = remember { BudgetNotificationManager.getInstance(context) }
    val passcodeManager = remember { PasscodeManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
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

    // Notification settings
    var notificationsEnabled by remember { mutableStateOf(notificationManager.areNotificationsEnabled()) }
    var showDaysBeforeDialog by remember { mutableStateOf(false) }
    var showNotificationTimeDialog by remember { mutableStateOf(false) }

    // Passcode settings
    var passcodeEnabled by remember { mutableStateOf(passcodeManager.isPasscodeEnabled()) }
    var biometricEnabled by remember { mutableStateOf(passcodeManager.isBiometricEnabled()) }
    var showPasscodeSetup by remember { mutableStateOf(false) }
    var showPasscodeChange by remember { mutableStateOf(false) }
    var showPasscodeOptions by remember { mutableStateOf(false) }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, enable notifications
            notificationsEnabled = true
            notificationManager.setNotificationsEnabled(true)
            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied
            notificationsEnabled = false
            notificationManager.setNotificationsEnabled(false)
            Toast.makeText(
                context,
                "Notification permission denied. Enable it in system settings to receive reminders.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
            .background(androidx.compose.ui.graphics.Color(0xFFEFFFFF))
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
                    subtitle = currentTheme.name,
                    onClick = { showThemeSelector = true }
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
                SettingsItem(
                    icon = FeatherIcons.DollarSign,
                    title = "Currency",
                    subtitle = "${selectedCurrency.name} (${selectedCurrency.symbol})",
                    onClick = { showCurrencySelector = true }
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
                if (!isPremium) {
                    SettingsItem(
                        icon = FeatherIcons.Lock,
                        title = "Passcode Protection",
                        subtitle = "Protect your data with a passcode",
                        onClick = { showPremiumPaywall = true },
                        isPremiumFeature = true
                    )
                } else if (!passcodeEnabled) {
                    SettingsItem(
                        icon = FeatherIcons.Lock,
                        title = "Passcode Protection",
                        subtitle = "Not enabled",
                        onClick = { showPasscodeSetup = true }
                    )
                } else {
                    SettingsItem(
                        icon = FeatherIcons.Lock,
                        title = "Passcode Protection",
                        subtitle = "Enabled",
                        onClick = { showPasscodeOptions = true }
                    )
                }
            }

            // Biometric toggle (only if passcode is enabled)
            if (isPremium && passcodeEnabled && passcodeManager.isBiometricAvailable()) {
                item {
                    SettingsSwitchItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Authentication",
                        subtitle = if (biometricEnabled) "Fingerprint or Face Recognition" else "Disabled",
                        checked = biometricEnabled,
                        onCheckedChange = {
                            biometricEnabled = it
                            passcodeManager.setBiometricEnabled(it)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Notifications Section (Premium)
            item {
                SettingsSection(title = "Notifications")
            }

            item {
                if (!isPremium) {
                    SettingsItem(
                        icon = FeatherIcons.Bell,
                        title = "Budget Reminders",
                        subtitle = "Get notified about upcoming payments",
                        onClick = { showPremiumPaywall = true },
                        isPremiumFeature = true
                    )
                } else {
                    SettingsSwitchItem(
                        icon = FeatherIcons.Bell,
                        title = "Budget Reminders",
                        subtitle = if (notificationsEnabled) "Enabled" else "Disabled",
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Check if we need to request permission (Android 13+)
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    // Check if permission is already granted
                                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.POST_NOTIFICATIONS
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    ) {
                                        // Permission already granted
                                        notificationsEnabled = true
                                        notificationManager.setNotificationsEnabled(true)
                                        Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Request permission
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    // No permission needed for Android 12 and below
                                    notificationsEnabled = true
                                    notificationManager.setNotificationsEnabled(true)
                                    Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Disabling notifications - no permission needed
                                notificationsEnabled = false
                                notificationManager.setNotificationsEnabled(false)
                                Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Show additional notification settings only if premium and enabled
            if (isPremium && notificationsEnabled) {
                item {
                    SettingsItem(
                        icon = FeatherIcons.Calendar,
                        title = "Days Before Due Date",
                        subtitle = "${notificationManager.getDaysBeforeDueDate()} days",
                        onClick = { showDaysBeforeDialog = true }
                    )
                }

                item {
                    val hour = notificationManager.getNotificationHour()
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                    SettingsItem(
                        icon = FeatherIcons.Clock,
                        title = "Notification Time",
                        subtitle = String.format("%d:00 %s", displayHour, amPm),
                        onClick = { showNotificationTimeDialog = true }
                    )
                }
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
            currentTheme = currentTheme.id,
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

    // Show Passcode Setup
    if (showPasscodeSetup) {
        PasscodeLockScreen(
            mode = PasscodeMode.SET,
            onUnlocked = {
                showPasscodeSetup = false
                passcodeEnabled = true
                Toast.makeText(context, "Passcode enabled successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPasscodeSetup = false }
        )
    }

    // Show Passcode Change
    if (showPasscodeChange) {
        PasscodeLockScreen(
            mode = PasscodeMode.CHANGE,
            onUnlocked = {
                showPasscodeChange = false
                Toast.makeText(context, "Passcode changed successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPasscodeChange = false }
        )
    }

    // Show Passcode Options
    if (showPasscodeOptions) {
        PasscodeOptionsDialog(
            onChangePasscode = {
                showPasscodeOptions = false
                showPasscodeChange = true
            },
            onDisablePasscode = {
                passcodeManager.deletePasscode()
                passcodeEnabled = false
                biometricEnabled = false
                showPasscodeOptions = false
                Toast.makeText(context, "Passcode disabled", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPasscodeOptions = false }
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

    // Show Days Before Dialog
    if (showDaysBeforeDialog) {
        DaysBeforeDialog(
            currentDays = notificationManager.getDaysBeforeDueDate(),
            onDaysSelected = { days ->
                notificationManager.setDaysBeforeDueDate(days)
                showDaysBeforeDialog = false
                Toast.makeText(context, "Reminder set to $days days before", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showDaysBeforeDialog = false }
        )
    }

    // Show Notification Time Dialog
    if (showNotificationTimeDialog) {
        NotificationTimeDialog(
            currentHour = notificationManager.getNotificationHour(),
            onTimeSelected = { hour ->
                notificationManager.setNotificationHour(hour)
                showNotificationTimeDialog = false
                val amPm = if (hour >= 12) "PM" else "AM"
                val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                Toast.makeText(context, "Notification time set to $displayHour:00 $amPm", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showNotificationTimeDialog = false }
        )
    }
}

@Composable
fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "SpendSee Logo",
            modifier = Modifier.size(28.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "SpendSee",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
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
            .border(1.dp, Color(0xFFAAD4D3), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDAF4F3)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFAAD4D3), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDAF4F3)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
    onThemeSelected: (com.spendsee.ui.theme.ThemeColors) -> Unit,
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
                items(com.spendsee.ui.theme.ThemeColorSchemes.allThemes.size) { index ->
                    val theme = com.spendsee.ui.theme.ThemeColorSchemes.allThemes[index]
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
                                    .background(theme.surface)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(theme.accent)
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

                    if (index < com.spendsee.ui.theme.ThemeColorSchemes.allThemes.size - 1) {
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

@Composable
fun DaysBeforeDialog(
    currentDays: Int,
    onDaysSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val daysOptions = listOf(1, 2, 3, 5, 7)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Days Before Due Date",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysOptions.size) { index ->
                    val days = daysOptions[index]
                    val isSelected = days == currentDays

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDaysSelected(days) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$days day${if (days > 1) "s" else ""} before",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = FeatherIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (index < daysOptions.size - 1) {
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
fun NotificationTimeDialog(
    currentHour: Int,
    onTimeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timeOptions = listOf(6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Notification Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(timeOptions.size) { index ->
                    val hour = timeOptions[index]
                    val isSelected = hour == currentHour
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTimeSelected(hour) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%d:00 %s", displayHour, amPm),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = FeatherIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (index < timeOptions.size - 1) {
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
private fun PasscodeOptionsDialog(
    onChangePasscode: () -> Unit,
    onDisablePasscode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = FeatherIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Passcode Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Change Passcode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChangePasscode() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FeatherIcons.Edit2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Change Passcode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Set a new 6-digit passcode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Disable Passcode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDisablePasscode() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FeatherIcons.Trash2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Disable Passcode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Turn off passcode protection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

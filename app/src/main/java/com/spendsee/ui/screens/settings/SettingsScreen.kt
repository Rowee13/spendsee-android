package com.spendsee.ui.screens.settings

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
import com.spendsee.managers.PremiumManager
import com.spendsee.managers.CurrencyManager
import com.spendsee.utils.Currency
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.ui.screens.categories.CategoriesScreen

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val isDeveloperMode by remember { mutableStateOf(premiumManager.isDeveloperModeEnabled()) }
    var showDeveloperMode by remember { mutableStateOf(false) }
    var showPremiumPaywall by remember { mutableStateOf(false) }
    var showCategoriesScreen by remember { mutableStateOf(false) }
    var showCurrencySelector by remember { mutableStateOf(false) }
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
                    subtitle = "Default",
                    onClick = { /* TODO: Implement theme selector */ }
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
                SettingsItem(
                    icon = FeatherIcons.Moon,
                    title = "Dark Mode",
                    subtitle = "System default",
                    onClick = { /* TODO: Implement dark mode toggle */ }
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
                    subtitle = "Export your data",
                    onClick = { /* TODO: Implement backup */ }
                )
            }

            item {
                SettingsItem(
                    icon = FeatherIcons.Download,
                    title = "Restore",
                    subtitle = "Import your data",
                    onClick = { /* TODO: Implement restore */ }
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
                        subtitle = "All features unlocked",
                        onClick = {}
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
                    subtitle = "1.0.0",
                    onClick = {}
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

    // Show developer mode on version tap (tap 7 times)
    LaunchedEffect(Unit) {
        // This is a placeholder - you can implement tap counter on Version item
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
                modifier = Modifier.size(40.dp)
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

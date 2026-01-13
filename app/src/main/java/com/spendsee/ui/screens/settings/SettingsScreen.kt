package com.spendsee.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
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
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "Default",
                    onClick = { /* TODO: Implement theme selector */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Currency",
                    subtitle = "USD ($)",
                    onClick = { /* TODO: Implement currency selector */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
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
                    icon = Icons.Default.CloudUpload,
                    title = "Backup",
                    subtitle = "Export your data",
                    onClick = { /* TODO: Implement backup */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.CloudDownload,
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
                    icon = Icons.Default.Category,
                    title = "Categories",
                    subtitle = "Manage your categories",
                    onClick = { /* TODO: Implement categories management */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Help,
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
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Unlock Premium",
                    subtitle = "Get all features",
                    onClick = { /* TODO: Show paywall */ },
                    isPremiumFeature = true
                )
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
                    icon = Icons.Default.Lock,
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
                    icon = Icons.Default.Notifications,
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
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = {}
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Support",
                    subtitle = "Contact us",
                    onClick = { /* TODO: Open email */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = { /* TODO: Open privacy policy */ }
                )
            }
        }
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
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
                                imageVector = Icons.Default.Star,
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
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

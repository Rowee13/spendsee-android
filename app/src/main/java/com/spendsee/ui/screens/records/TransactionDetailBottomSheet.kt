//
//  TransactionDetailBottomSheet.kt
//  SpendSee
//
//  Created on 2026-02-03.
//

package com.spendsee.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendsee.data.local.entities.Account
import com.spendsee.data.local.entities.Budget
import com.spendsee.data.local.entities.Category
import com.spendsee.data.local.entities.Transaction
import com.spendsee.data.models.TransactionType
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.ThemeManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailBottomSheet(
    transaction: Transaction,
    account: Account?,
    toAccount: Account?,
    budget: Budget?,
    category: Category?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    val transactionType = try {
        TransactionType.valueOf(transaction.type.uppercase())
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    val typeColor = when (transactionType) {
        TransactionType.INCOME -> Color(0xFF2AB14F)
        TransactionType.EXPENSE -> Color(0xFFFF3B30)
        TransactionType.TRANSFER -> Color(0xFF007AFF)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = currentTheme.getBackground(isDarkMode)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.getText(isDarkMode)
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = currentTheme.getText(isDarkMode)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }

            // Type Badge and Amount Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(typeColor.copy(alpha = 0.1f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Type badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val typeIcon = when (transactionType) {
                        TransactionType.INCOME -> Icons.Default.ArrowDownward
                        TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                    }

                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = transactionType.name.lowercase().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = typeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Amount
                val formattedAmount = currencyManager.formatAmount(transaction.amount)
                val sign = when (transactionType) {
                    TransactionType.INCOME -> "+"
                    TransactionType.EXPENSE -> "-"
                    TransactionType.TRANSFER -> ""
                }
                Text(
                    text = "$sign$formattedAmount",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.getText(isDarkMode)
                )

                Spacer(Modifier.height(8.dp))

                // Title
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = currentTheme.getText(isDarkMode),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Category
                category?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = currentTheme.getText(isDarkMode).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Detail Sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Date & Time Section
                DetailSection(title = "Date & Time") {
                    DetailRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date",
                        value = formatDate(transaction.date)
                    )
                    DetailRow(
                        icon = Icons.Default.AccessTime,
                        label = "Time",
                        value = formatTime(transaction.date)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Account Information Section
                DetailSection(title = "Account Information") {
                    account?.let {
                        DetailRow(
                            icon = Icons.Default.AccountBalance,
                            label = if (transactionType == TransactionType.INCOME) "To" else "From",
                            value = it.name
                        )
                    }

                    toAccount?.let {
                        DetailRow(
                            icon = Icons.Default.AccountBalance,
                            label = "To",
                            value = it.name
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Category & Budget Section
                DetailSection(title = "Details") {
                    category?.let {
                        DetailRow(
                            icon = Icons.Default.Label,
                            label = "Category",
                            value = it.name
                        )
                    }

                    budget?.let {
                        DetailRow(
                            icon = Icons.Default.PieChart,
                            label = "Budget",
                            value = it.name
                        )
                    }
                }

                // Notes Section (only if notes exist)
                if (transaction.notes.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))

                    DetailSection(title = "Notes") {
                        Text(
                            text = transaction.notes,
                            style = MaterialTheme.typography.bodyLarge,
                            color = currentTheme.getText(isDarkMode),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = currentTheme.getAccent(isDarkMode),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        content()
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = currentTheme.getText(isDarkMode).copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = currentTheme.getText(isDarkMode).copy(alpha = 0.7f),
            modifier = Modifier.weight(0.4f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = currentTheme.getText(isDarkMode),
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return timeFormat.format(Date(timestamp))
}

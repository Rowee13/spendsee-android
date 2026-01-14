package com.spendsee.ui.screens.accounts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendsee.R
import com.spendsee.data.local.entities.Account
import com.spendsee.data.repository.AccountRepository
import com.spendsee.data.repository.TransactionRepository
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModelFactory(
            AccountRepository.getInstance(LocalContext.current),
            TransactionRepository.getInstance(LocalContext.current)
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAccount by remember { mutableStateOf(false) }
    var showEditAccount by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccount = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(FeatherIcons.Plus, contentDescription = "Add Account")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Unified Header Section (iOS style)
            UnifiedAccountsHeaderSection(
                totalBalance = uiState.totalBalance,
                totalExpenses = uiState.totalExpenses,
                totalIncome = uiState.totalIncome
            )

            // Accounts List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.accounts.isEmpty()) {
                EmptyState()
            } else {
                AccountsList(
                    accounts = uiState.accounts,
                    onEditAccount = {
                        accountToEdit = it
                        showEditAccount = true
                    },
                    onDeleteAccount = { viewModel.deleteAccount(it) }
                )
            }

            // Error Message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.loadAccounts() }) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Add Account Dialog
    if (showAddAccount) {
        AddEditAccountDialog(
            account = null,
            onDismiss = { showAddAccount = false },
            onSave = { name, type, balance, icon, color ->
                viewModel.addAccount(name, type, balance, icon, color)
                showAddAccount = false
            }
        )
    }

    // Edit Account Dialog
    if (showEditAccount && accountToEdit != null) {
        AddEditAccountDialog(
            account = accountToEdit,
            onDismiss = {
                showEditAccount = false
                accountToEdit = null
            },
            onSave = { name, type, balance, icon, color ->
                accountToEdit?.let { account ->
                    viewModel.updateAccount(
                        account.copy(
                            name = name,
                            type = type,
                            balance = balance,
                            icon = icon,
                            colorHex = color
                        )
                    )
                }
                showEditAccount = false
                accountToEdit = null
            }
        )
    }
}

@Composable
fun UnifiedAccountsHeaderSection(
    totalBalance: Double,
    totalExpenses: Double,
    totalIncome: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // App Logo and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "SpendSee Logo",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SpendSee",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // All Accounts subtitle
            Text(
                text = "All Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Total Balance (large display)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatCurrency(totalBalance),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Expenses and Income Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BalanceStatColumn(
                        label = "Expenses",
                        amount = totalExpenses,
                        color = Color(0xFFEF5350)
                    )
                    BalanceStatColumn(
                        label = "Income",
                        amount = totalIncome,
                        color = Color(0xFF66BB6A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BalanceStatColumn(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun AccountsList(
    accounts: List<Account>,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(accounts) { account ->
            AccountCard(
                account = account,
                onEdit = { onEditAccount(account) },
                onDelete = { onDeleteAccount(account) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Icon and account info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Account Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(account.colorHex)).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(account.icon),
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(account.colorHex)),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = account.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right side: Balance
            Text(
                text = formatCurrency(account.balance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (account.balance >= 0) MaterialTheme.colorScheme.onSurface else Color(0xFFEF5350)
            )
        }

        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(FeatherIcons.Edit2, contentDescription = "Edit")
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = Color.Red) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(FeatherIcons.Trash2, contentDescription = "Delete", tint = Color.Red)
                }
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = FeatherIcons.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Accounts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to create your first account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

private fun getAccountIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.lowercase()) {
        "account_balance" -> FeatherIcons.Briefcase
        "credit_card" -> FeatherIcons.CreditCard
        "wallet" -> FeatherIcons.Package
        "savings" -> FeatherIcons.TrendingUp
        "payments" -> FeatherIcons.DollarSign
        else -> FeatherIcons.Briefcase
    }
}

package com.spendsee.ui.screens.accounts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ColorFilter
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
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.theme.ThemeColors

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
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showAddAccount by remember { mutableStateOf(false) }
    var showEditAccount by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccount = true },
                containerColor = currentTheme.getAccent(isDarkMode),
                contentColor = Color.White
            ) {
                Icon(FeatherIcons.Plus, contentDescription = "Add Account")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.getBackground(isDarkMode))
        ) {
                // Unified Header Section (iOS style)
                UnifiedAccountsHeaderSection(
                totalBalance = uiState.totalBalance,
                totalExpenses = uiState.totalExpenses,
                totalIncome = uiState.totalIncome,
                currencySymbol = selectedCurrency.symbol,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
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
                EmptyState(
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode
                )
            } else {
                AccountsList(
                    accounts = uiState.accounts,
                    onEditAccount = {
                        accountToEdit = it
                        showEditAccount = true
                    },
                    onDeleteAccount = { viewModel.deleteAccount(it) },
                    currencySymbol = selectedCurrency.symbol,
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode
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
    totalIncome: Double,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Logo and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "SpendSee Logo",
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color(0xFF1A1A1A))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SpendSee",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
            )
        }

        // All Accounts subtitle
        Text(
            text = "All accounts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = currentTheme.getText(isDarkMode).copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Total Balance (large display)
        Text(
            text = formatCurrency(totalBalance, currencySymbol),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
            fontWeight = FontWeight.Bold,
            color = currentTheme.getText(isDarkMode),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Expenses and Income Stats (2-column cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Expenses Card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = currentTheme.getSurface(isDarkMode),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%.2f", totalExpenses)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3B30)
                    )
                }
            }

            // Income Card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = currentTheme.getSurface(isDarkMode),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%.2f", totalIncome)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34C759)
                    )
                }
            }
        }

        // "Accounts" section header with divider
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                thickness = 1.dp,
                color = currentTheme.getBorder(isDarkMode)
            )
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = currentTheme.getText(isDarkMode)
            )
        }
    }
}

@Composable
fun BalanceStatColumn(label: String, amount: Double, color: Color, currencySymbol: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount, currencySymbol),
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
    onDeleteAccount: (Account) -> Unit,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(accounts) { account ->
            AccountCard(
                account = account,
                onEdit = { onEditAccount(account) },
                onDelete = { onDeleteAccount(account) },
                currencySymbol = currencySymbol,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
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
    onDelete: () -> Unit,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp))
            .clickable { showMenu = true },
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.getSurface(isDarkMode)
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
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.getText(isDarkMode)
                    )
                    Text(
                        text = account.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                }
            }

            // Right side: Balance and smile icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatCurrency(account.balance, currencySymbol),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance >= 0) currentTheme.getText(isDarkMode) else Color(0xFFEF5350)
                )
                Text(
                    text = "☺",
                    style = MaterialTheme.typography.titleMedium,
                    color = currentTheme.getInactive(isDarkMode).copy(alpha = 0.3f)
                )
            }
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
fun EmptyState(
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
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
                tint = currentTheme.getInactive(isDarkMode)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Accounts",
                style = MaterialTheme.typography.titleMedium,
                color = currentTheme.getText(isDarkMode),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to create your first account",
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.getInactive(isDarkMode),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCurrency(amount: Double, currencySymbol: String): String {
    val formatter = java.text.DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
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

package com.spendsee.ui.screens.accounts

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.WindowInsets
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
import com.spendsee.data.local.entities.Transaction
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
    var showAccountDetail by remember { mutableStateOf(false) }
    var accountToShowDetail by remember { mutableStateOf<Account?>(null) }
    var fabVisible by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    var previousScrollIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }

    // FAB scroll detection
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentIndex = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset
        if (currentIndex > previousScrollIndex || (currentIndex == previousScrollIndex && currentOffset > previousScrollOffset)) {
            fabVisible = false
        } else if (currentIndex < previousScrollIndex || currentOffset < previousScrollOffset) {
            fabVisible = true
        }
        previousScrollIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    // Recent transactions (5 most recent across all accounts)
    val transactionRepository = remember { TransactionRepository.getInstance(context) }
    val allTransactions by transactionRepository.getAllTransactions().collectAsState(initial = emptyList())
    val recentTransactions = remember(allTransactions) {
        allTransactions.sortedByDescending { it.date }.take(5)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                    FloatingActionButton(
                        onClick = { showAddAccount = true },
                        containerColor = currentTheme.getAccent(isDarkMode),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add Account")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.getBackground(isDarkMode))
        ) {
            // FIXED: Logo + "Accounts" title
            AccountsFixedHeader(currentTheme = currentTheme, isDarkMode = isDarkMode)

            // SCROLLABLE: balance + account cards + recent movements
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    // "All accounts" label + total balance
                    item {
                        Text(
                            text = "All accounts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.getAccent(isDarkMode)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(uiState.totalBalance, selectedCurrency.symbol),
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.totalBalance >= 0) currentTheme.getText(isDarkMode) else Color(0xFFFF3B30)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Account cards or empty state
                    if (uiState.accounts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = currentTheme.getInactive(isDarkMode)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No Accounts", style = MaterialTheme.typography.titleMedium, color = currentTheme.getText(isDarkMode), fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tap the + button to create your first account", style = MaterialTheme.typography.bodyMedium, color = currentTheme.getInactive(isDarkMode), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    } else {
                        items(uiState.accounts) { account ->
                            AccountCard(
                                account = account,
                                onClick = { accountToShowDetail = account; showAccountDetail = true },
                                onEdit = { accountToEdit = account; showEditAccount = true },
                                onDelete = { viewModel.deleteAccount(account) },
                                currencySymbol = selectedCurrency.symbol,
                                currentTheme = currentTheme,
                                isDarkMode = isDarkMode
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Recent Movements section
                        if (recentTransactions.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recent Movements",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = currentTheme.getAccent(isDarkMode)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = currentTheme.getSurface(isDarkMode),
                                    border = BorderStroke(1.dp, currentTheme.getBorder(isDarkMode))
                                ) {
                                    Column {
                                        recentTransactions.forEachIndexed { index, transaction ->
                                            TransactionRow(
                                                transaction = transaction,
                                                currencySymbol = selectedCurrency.symbol,
                                                currentTheme = currentTheme,
                                                isDarkMode = isDarkMode,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                            if (index < recentTransactions.size - 1) {
                                                Divider(
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    thickness = 1.dp,
                                                    color = currentTheme.getBorder(isDarkMode)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }

            // Error Message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { viewModel.loadAccounts() }) { Text("Retry") } }
                ) { Text(error) }
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

    // Account Detail Sheet
    if (showAccountDetail && accountToShowDetail != null) {
        AccountDetailSheet(
            account = accountToShowDetail!!,
            onDismiss = {
                showAccountDetail = false
                accountToShowDetail = null
            }
        )
    }
}

@Composable
fun AccountsFixedHeader(
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(currentTheme.getBackground(isDarkMode))
            .padding(horizontal = 16.dp)
    ) {
        // Logo + notification bell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = currentTheme.getInactive(isDarkMode),
                modifier = Modifier.size(22.dp)
            )
        }

        // "Accounts" large title
        Text(
            text = "Accounts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = currentTheme.getText(isDarkMode),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
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
    onAccountClick: (Account) -> Unit,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean,
    onScrollChanged: (Boolean) -> Unit = {}
) {
    val listState = rememberLazyListState()
    var previousScrollOffset by remember { mutableStateOf(0) }

    // Detect scroll direction
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        val currentOffset = listState.firstVisibleItemScrollOffset
        val isScrollingDown = currentOffset > previousScrollOffset

        if (currentOffset != previousScrollOffset) {
            onScrollChanged(isScrollingDown)
            previousScrollOffset = currentOffset
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(accounts) { account ->
            AccountCard(
                account = account,
                onClick = { onAccountClick(account) },
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
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    val accountColor = Color(android.graphics.Color.parseColor(account.colorHex))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.getSurface(isDarkMode)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: colored square icon (left) + "⋯" menu (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accountColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(account.icon),
                        contentDescription = null,
                        tint = accountColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                            tint = currentTheme.getInactive(isDarkMode),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "Edit") }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account name
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = currentTheme.getText(isDarkMode),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Account type
            Text(
                text = account.type,
                style = MaterialTheme.typography.bodySmall,
                color = currentTheme.getInactive(isDarkMode)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "CURRENT BALANCE" caption
            Text(
                text = "CURRENT BALANCE",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = currentTheme.getInactive(isDarkMode),
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Balance amount
            Text(
                text = formatCurrency(account.balance, currencySymbol),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = if (account.balance >= 0) currentTheme.getText(isDarkMode) else Color(0xFFFF3B30)
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
                imageVector = Icons.Outlined.CreditCard,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailSheet(
    account: Account,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    val transactionRepository = remember { TransactionRepository.getInstance(context) }
    val allTransactions by transactionRepository.getAllTransactions().collectAsState(initial = emptyList())

    val accountTransactions = remember(allTransactions, account.id) {
        allTransactions.filter { transaction ->
            transaction.accountId == account.id || transaction.toAccountId == account.id
        }.sortedByDescending { it.date }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = currentTheme.getBackground(isDarkMode)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Account Info Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Account Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(account.colorHex)).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(account.icon),
                        contentDescription = null,
                        tint = Color(android.graphics.Color.parseColor(account.colorHex)),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = account.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = currentTheme.getText(isDarkMode)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatCurrency(account.balance, selectedCurrency.symbol),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance >= 0) currentTheme.getText(isDarkMode) else Color(0xFFEF5350)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = account.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = currentTheme.getInactive(isDarkMode)
                )
            }

            Divider(
                thickness = 1.dp,
                color = currentTheme.getBorder(isDarkMode)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction History
            if (accountTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = currentTheme.getInactive(isDarkMode)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            color = currentTheme.getText(isDarkMode)
                        )
                        Text(
                            text = "No transactions for this account yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.getInactive(isDarkMode),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.getText(isDarkMode),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(accountTransactions) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            currencySymbol = selectedCurrency.symbol,
                            currentTheme = currentTheme,
                            isDarkMode = isDarkMode
                        )
                        if (transaction != accountTransactions.last()) {
                            Divider(
                                thickness = 1.dp,
                                color = currentTheme.getBorder(isDarkMode).copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: com.spendsee.data.local.entities.Transaction,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = currentTheme.getText(isDarkMode)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(transaction.date)),
                style = MaterialTheme.typography.bodySmall,
                color = currentTheme.getInactive(isDarkMode)
            )
            if (transaction.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = currentTheme.getInactive(isDarkMode),
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = formatCurrency(transaction.amount, currencySymbol),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = when (transaction.type) {
                "income" -> Color(0xFF34C759)
                "expense" -> Color(0xFFFF3B30)
                "transfer" -> Color(0xFF007AFF)
                else -> currentTheme.getText(isDarkMode)
            }
        )
    }
}

private fun formatCurrency(amount: Double, currencySymbol: String): String {
    val formatter = java.text.DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
}

private fun getAccountIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.lowercase()) {
        // Financial Icons
        "accountbalance" -> Icons.Outlined.AccountBalance
        "creditcard" -> Icons.Outlined.CreditCard
        "payments" -> Icons.Outlined.Payments
        "accountbalancewallet" -> Icons.Outlined.AccountBalanceWallet
        "savings" -> Icons.Outlined.Savings
        "trendingup" -> Icons.Outlined.TrendingUp
        "showchart" -> Icons.Outlined.ShowChart
        "paid" -> Icons.Outlined.Paid
        "localatm" -> Icons.Outlined.LocalAtm
        "currencyexchange" -> Icons.Outlined.CurrencyExchange
        "account" -> Icons.Outlined.AccountCircle
        "monetizationon" -> Icons.Outlined.MonetizationOn

        // Lifestyle & General
        "home" -> Icons.Outlined.Home
        "work" -> Icons.Outlined.Work
        "shoppingbag" -> Icons.Outlined.ShoppingBag
        "cardgiftcard" -> Icons.Outlined.CardGiftcard
        "star" -> Icons.Outlined.Star
        "favorite" -> Icons.Outlined.Favorite
        "school" -> Icons.Outlined.School
        "localhospital" -> Icons.Outlined.LocalHospital
        "flight" -> Icons.Outlined.Flight
        "directionscar" -> Icons.Outlined.DirectionsCar
        "restaurant" -> Icons.Outlined.Restaurant
        "lock" -> Icons.Outlined.Lock

        // Legacy Feather icon names (backward compatibility)
        "dollarsign" -> Icons.Outlined.Payments
        "briefcase" -> Icons.Outlined.Work
        "smartphone" -> Icons.Outlined.AccountBalanceWallet
        "shield" -> Icons.Outlined.Lock
        "package" -> Icons.Outlined.Inventory
        "gift" -> Icons.Outlined.CardGiftcard
        "heart" -> Icons.Outlined.Favorite
        "droplet" -> Icons.Outlined.WaterDrop
        "truck" -> Icons.Outlined.LocalShipping
        "shoppingcart" -> Icons.Outlined.ShoppingCart
        "coffee" -> Icons.Outlined.LocalCafe
        "camera" -> Icons.Outlined.CameraAlt

        else -> Icons.Outlined.AccountBalance
    }
}

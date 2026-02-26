package com.spendsee.ui.screens.records

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.foundation.layout.WindowInsets
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.spendsee.data.local.entities.Transaction
import com.spendsee.data.local.entities.Account
import com.spendsee.data.local.entities.Category
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.data.repository.AccountRepository
import com.spendsee.data.repository.CategoryRepository
import com.spendsee.data.repository.AppNotificationRepository
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.PremiumManager
import com.spendsee.managers.ReceiptParser
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.screens.camera.CameraScreen
import com.spendsee.ui.screens.notifications.NotificationCenterScreen
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.ui.theme.ThemeColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel = viewModel(
        factory = RecordsViewModelFactory(
            TransactionRepository.getInstance(LocalContext.current),
            AccountRepository.getInstance(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val receiptParser = remember { ReceiptParser() }
    val scope = rememberCoroutineScope()

    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isPremium by premiumManager.isPremium.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()

    var showAddTransaction by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var isProcessingReceipt by remember { mutableStateOf(false) }
    var showPremiumPaywall by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    var fabVisible by remember { mutableStateOf(true) }
    var showNotificationCenter by remember { mutableStateOf(false) }
    var showTransactionDetail by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        }
    }

    // Load accounts and categories
    val accounts by AccountRepository.getInstance(context).getAllAccounts().collectAsState(initial = emptyList())
    val categories by CategoryRepository.getInstance(context).getAllCategories().collectAsState(initial = emptyList())

    // Load unread notification count with error handling
    val unreadCount by remember {
        try {
            AppNotificationRepository.getInstance(context).getUnreadCount()
        } catch (e: Exception) {
            // If notification table doesn't exist yet (migration pending), return empty flow
            kotlinx.coroutines.flow.flowOf(0)
        }
    }.collectAsState(initial = 0)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                // Scan Receipt FAB (shown when expanded)
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(8.dp)),
                            color = currentTheme.getSurface(isDarkMode),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = "Scan Receipt",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = currentTheme.getText(isDarkMode)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                if (isPremium) {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    fabExpanded = false
                                } else {
                                    showPremiumPaywall = true
                                    fabExpanded = false
                                }
                            },
                            containerColor = currentTheme.getAccent(isDarkMode),
                            contentColor = Color.White
                        ) {
                            Icon(FeatherIcons.Camera, contentDescription = "Scan Receipt")
                        }
                    }
                }

                // Manual Add FAB (shown when expanded)
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(8.dp)),
                            color = currentTheme.getSurface(isDarkMode),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = "Add Manually",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = currentTheme.getText(isDarkMode)
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = {
                                showAddTransaction = true
                                fabExpanded = false
                            },
                            containerColor = currentTheme.getAccent(isDarkMode),
                            contentColor = Color.White
                        ) {
                            Icon(FeatherIcons.Edit, contentDescription = "Add Manually")
                        }
                    }
                }

                    // Main FAB (always visible) - Updated color from mockup
                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = currentTheme.getAccent(isDarkMode),  // Exact color from mockup
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = if (fabExpanded) FeatherIcons.X else Icons.Default.Add,
                            contentDescription = if (fabExpanded) "Close menu" else "Add transaction"
                        )
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
            // Unified Header Section (iOS style)
            UnifiedHeaderSection(
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                expenses = uiState.totalExpenses,
                income = uiState.totalIncome,
                net = uiState.netTotal,
                currencySymbol = selectedCurrency.symbol,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode,
                unreadNotificationCount = unreadCount,
                onNotificationClick = { showNotificationCenter = true }
            )

            // Transaction List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                EmptyState(
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode
                )
            } else {
                TransactionList(
                    groupedTransactions = uiState.groupedTransactions,
                    onTransactionClick = { transaction ->
                        selectedTransaction = transaction
                        showTransactionDetail = true
                    },
                    onEditTransaction = { transaction ->
                        transactionToEdit = transaction
                        showAddTransaction = true
                    },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    currencySymbol = selectedCurrency.symbol,
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode,
                    onScrollChanged = { isScrollingDown ->
                        fabVisible = !isScrollingDown
                    }
                )
            }

            // Error Message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.loadTransactions() }) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Add/Edit Transaction Dialog
    if (showAddTransaction) {
        AddEditTransactionDialog(
            transaction = transactionToEdit,
            accounts = accounts,
            categories = categories,
            onDismiss = {
                showAddTransaction = false
                transactionToEdit = null
            },
            onSave = { title, amount, type, category, date, notes, accountId, toAccountId ->
                if (transactionToEdit == null) {
                    viewModel.addTransaction(title, amount, type, category, date, notes, accountId, toAccountId)
                } else {
                    viewModel.updateTransaction(
                        transactionToEdit!!,
                        title,
                        amount,
                        type,
                        category,
                        date,
                        notes,
                        accountId,
                        toAccountId
                    )
                }
                showAddTransaction = false
                transactionToEdit = null
            }
        )
    }

    // Camera Screen for Receipt Scanning
    if (showCamera) {
        CameraScreen(
            onImageCaptured = { bitmap ->
                showCamera = false
                isProcessingReceipt = true

                scope.launch {
                    try {
                        val receiptData = receiptParser.parseReceipt(bitmap)

                        // Create a temporary transaction with scanned data
                        // Build clean notes with only items (amount, date already in fields)
                        val notes = buildString {
                            if (receiptData.items.isNotEmpty()) {
                                append("Items:\n")
                                append(receiptData.items.joinToString("\n"))
                            } else if (receiptData.merchantName?.isNotBlank() == true) {
                                append("From: ${receiptData.merchantName}")
                            }
                        }

                        transactionToEdit = Transaction(
                            id = "",
                            title = receiptData.merchantName ?: "Receipt",
                            amount = receiptData.amount ?: 0.0,
                            type = "expense",
                            category = "", // Will be auto-selected in dialog
                            date = receiptData.date ?: System.currentTimeMillis(),
                            notes = notes.ifBlank { "Scanned from receipt" },
                            accountId = accounts.firstOrNull()?.id,
                            toAccountId = null,
                            budgetId = null,
                            createdAt = System.currentTimeMillis()
                        )

                        showAddTransaction = true
                        isProcessingReceipt = false
                    } catch (e: Exception) {
                        isProcessingReceipt = false
                        // Show error - could add a Snackbar here
                    }
                }
            },
            onDismiss = {
                showCamera = false
            }
        )
    }

    // Processing Indicator
    if (isProcessingReceipt) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "Processing receipt...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    // Premium Paywall
    if (showPremiumPaywall) {
        PremiumPaywallScreen(
            onDismiss = { showPremiumPaywall = false },
            onPurchaseSuccess = {
                showPremiumPaywall = false
                // Premium status will be updated automatically via StateFlow
            }
        )
    }

    // Notification Center
    if (showNotificationCenter) {
        val notificationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNotificationCenter = false },
            sheetState = notificationSheetState,
            containerColor = currentTheme.getBackground(isDarkMode)
        ) {
            NotificationCenterScreen(
                onNavigateToBudget = { budgetId ->
                    showNotificationCenter = false
                    // TODO: Navigate to budget screen with budgetId
                },
                onClose = { showNotificationCenter = false }
            )
        }
    }

    // Transaction Detail Bottom Sheet
    selectedTransaction?.let { transaction ->
        if (showTransactionDetail) {
            val account = accounts.firstOrNull { it.id == transaction.accountId }
            val toAccount = accounts.firstOrNull { it.id == transaction.toAccountId }
            val category = categories.firstOrNull { it.name == transaction.category }

            TransactionDetailBottomSheet(
                transaction = transaction,
                account = account,
                toAccount = toAccount,
                budget = null, // TODO: Fetch budget if needed
                category = category,
                onDismiss = {
                    showTransactionDetail = false
                    selectedTransaction = null
                },
                onEdit = {
                    showTransactionDetail = false
                    transactionToEdit = transaction
                    showAddTransaction = true
                },
                onDelete = {
                    showTransactionDetail = false
                    selectedTransaction = null
                    viewModel.deleteTransaction(transaction)
                }
            )
        }
    }
}

@Composable
fun UnifiedHeaderSection(
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    expenses: Double,
    income: Double,
    net: Double,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.YEAR, selectedYear)
    }
    val monthYearText = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    val monthText = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
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

            // Notification Bell Button
            NotificationBellButton(
                unreadCount = unreadNotificationCount,
                onClick = onNotificationClick,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )
        }

        // Month Navigation Pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(25.dp)),
            shape = RoundedCornerShape(25.dp),
            color = currentTheme.getSurface(isDarkMode),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous month button with background
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(currentTheme.getBorder(isDarkMode).copy(alpha = 0.7f))
                        .clickable { onPreviousMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        FeatherIcons.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = currentTheme.getText(isDarkMode),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = currentTheme.getText(isDarkMode)
                )

                // Next month button with background
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(currentTheme.getBorder(isDarkMode).copy(alpha = 0.7f))
                        .clickable { onNextMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        FeatherIcons.ChevronRight,
                        contentDescription = "Next Month",
                        tint = currentTheme.getText(isDarkMode),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Stats Cards (2 columns: Expenses | Income)
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
                        text = "$currencySymbol${String.format("%.2f", expenses)}",
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
                        text = "$currencySymbol${String.format("%.2f", income)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34C759)
                    )
                }
            }
        }

        // Monthly Earnings Card (Net) - with gradient background
        val earningsGradient = Brush.horizontalGradient(
            colors = listOf(
                currentTheme.getGradientStart(isDarkMode),
                currentTheme.getGradientEnd(isDarkMode)
            )
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(earningsGradient)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isMonochrome = currentTheme.id == "monochrome"
                val isMonochromeDark = isMonochrome && isDarkMode

                Text(
                    text = "$monthText Profit",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isMonochromeDark -> Color(0xFF1A1A1A)  // Black for dark mode gradient
                        isMonochrome -> Color.White  // White for light mode gradient
                        else -> Color(0xFF1A1A1A)  // Default dark text
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val netFormatted = if (net < 0) {
                        "-$currencySymbol${String.format("%.2f", kotlin.math.abs(net))}"
                    } else {
                        "$currencySymbol${String.format("%.2f", net)}"
                    }
                    val earningsColor = when {
                        isMonochromeDark -> if (net >= 0) Color(0xFF2E7D32) else Color(0xFFFF5252)
                        isMonochrome -> if (net >= 0) Color(0xFFB2FF59) else Color(0xFFFF5252)
                        else -> if (net >= 0) Color(0xFF1E7E34) else Color(0xFFD32F2F)
                    }
                    Text(
                        text = netFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = earningsColor
                    )
                    Icon(
                        imageVector = if (net >= 0) FeatherIcons.TrendingUp else FeatherIcons.TrendingDown,
                        contentDescription = if (net >= 0) "Positive earnings" else "Negative earnings",
                        tint = earningsColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    amount: Double,
    color: Color,
    currencySymbol: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 18.sp
        )
    }
}

@Composable
fun TransactionList(
    groupedTransactions: Map<Long, List<Transaction>>,
    onTransactionClick: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
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
        groupedTransactions.forEach { (date, transactions) ->
            item {
                DateHeader(
                    date = date,
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode
                )
            }

            items(transactions) { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction) },
                    onEdit = { onEditTransaction(transaction) },
                    onDelete = { onDeleteTransaction(transaction) },
                    currencySymbol = currencySymbol,
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DateHeader(
    date: Long,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val dateText = dateFormat.format(Date(date))

    Text(
        text = dateText,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = currentTheme.getInactive(isDarkMode),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
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
            .padding(vertical = 4.dp)
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp))
            .clickable { onClick() },
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
            // Left side: Icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getTransactionColor(transaction.type).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getTransactionIcon(transaction.type),
                        contentDescription = null,
                        tint = getTransactionColor(transaction.type),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and category
                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.getText(isDarkMode)
                    )
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                }
            }

            // Right side: Amount and smile icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatCurrency(transaction.amount, currencySymbol),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = getTransactionColor(transaction.type)
                )
                Text(
                    text = "☺",
                    style = MaterialTheme.typography.titleMedium,
                    color = currentTheme.getInactive(isDarkMode).copy(alpha = 0.3f)
                )
            }
        }

        // Dropdown menu for edit/delete
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
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = Color.Red) },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
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
                imageVector = FeatherIcons.FileText,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = currentTheme.getInactive(isDarkMode)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Transactions",
                style = MaterialTheme.typography.titleMedium,
                color = currentTheme.getText(isDarkMode),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to add your first transaction",
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.getInactive(isDarkMode),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper Functions
private fun formatCurrency(amount: Double, currencySymbol: String): String {
    val formatter = java.text.DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
}

private fun getTransactionColor(type: String): Color {
    return when (type) {
        "income" -> Color(0xFF66BB6A)
        "expense" -> Color(0xFFEF5350)
        "transfer" -> Color(0xFF42A5F5)
        else -> Color.Gray
    }
}

private fun getTransactionIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        "income" -> FeatherIcons.ArrowUpCircle
        "expense" -> FeatherIcons.ArrowDownCircle
        "transfer" -> FeatherIcons.RefreshCw
        else -> FeatherIcons.DollarSign
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = currentTheme.getText(isDarkMode)
            )
        }
    }
}

package com.spendsee.ui.screens.budgets

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.spendsee.data.repository.AccountRepository
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.PremiumManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.screens.premium.PremiumPaywallScreen
import com.spendsee.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = run {
        val context = LocalContext.current
        viewModel(
            factory = BudgetsViewModelFactory(
                BudgetRepository.getInstance(context),
                TransactionRepository.getInstance(context),
                AccountRepository.getInstance(context),
                context
            )
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    val accountRepository = remember { AccountRepository.getInstance(context) }
    val accounts by accountRepository.getAllAccounts().collectAsState(initial = emptyList())
    var showAddBudget by remember { mutableStateOf(false) }
    var showEditBudget by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<BudgetWithDetails?>(null) }
    var showAddBudgetItem by remember { mutableStateOf(false) }
    var showEditBudgetItem by remember { mutableStateOf(false) }
    var budgetItemToEdit by remember { mutableStateOf<com.spendsee.data.local.entities.BudgetItem?>(null) }
    var selectedBudgetId by remember { mutableStateOf<String?>(null) }
    var showPremiumPaywall by remember { mutableStateOf(false) }
    var showPaymentConfirmation by remember { mutableStateOf(false) }
    var budgetToPayFor by remember { mutableStateOf<BudgetWithDetails?>(null) }
    var fabVisible by remember { mutableStateOf(true) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showAddBudget = true },
                        containerColor = currentTheme.getAccent(isDarkMode),
                        contentColor = Color.White
                    ) {
                        Icon(FeatherIcons.Plus, contentDescription = "Add Budget")
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
            UnifiedBudgetHeaderSection(
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                allocated = uiState.totalAllocated,
                spent = uiState.totalSpent,
                remaining = uiState.totalRemaining,
                currencySymbol = selectedCurrency.symbol,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )

            // View Mode TabRow - Aligned with other elements
            TabRow(
                selectedTabIndex = if (uiState.displayMode == BudgetDisplayMode.LIST) 0 else 1,
                containerColor = currentTheme.getSurface(isDarkMode),
                contentColor = currentTheme.getAccent(isDarkMode),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = currentTheme.getBorder(isDarkMode),
                        shape = RoundedCornerShape(12.dp)
                    ),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.displayMode == BudgetDisplayMode.LIST) 0 else 1]),
                        height = 3.dp,
                        color = currentTheme.getAccent(isDarkMode)
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = uiState.displayMode == BudgetDisplayMode.LIST,
                    onClick = { viewModel.setDisplayMode(BudgetDisplayMode.LIST) },
                    text = {
                        Text(
                            text = "List",
                            fontWeight = if (uiState.displayMode == BudgetDisplayMode.LIST) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = uiState.displayMode == BudgetDisplayMode.CALENDAR,
                    onClick = { viewModel.setDisplayMode(BudgetDisplayMode.CALENDAR) },
                    text = {
                        Text(
                            text = "Calendar",
                            fontWeight = if (uiState.displayMode == BudgetDisplayMode.CALENDAR) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Content - Switch between List and Calendar
            when (uiState.displayMode) {
                BudgetDisplayMode.LIST -> {
                    // Budgets List
                    if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                    } else {
                        val filteredBudgets = if (uiState.selectedCalendarDate != null) {
                            viewModel.getFilteredBudgets()
                        } else {
                            uiState.budgetsWithDetails
                        }

                        if (filteredBudgets.isEmpty() && uiState.selectedCalendarDate != null) {
                            // Filtered empty state
                            EmptyFilteredState(
                                onClearFilter = { viewModel.selectCalendarDate(null) },
                                currentTheme = currentTheme,
                                isDarkMode = isDarkMode
                            )
                        } else if (uiState.budgetsWithDetails.isEmpty()) {
                            EmptyState(
                                onCopyFromPrevious = { viewModel.copyFromPreviousMonth() },
                                currentTheme = currentTheme,
                                isDarkMode = isDarkMode
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                    // Show "Copy Missing Budgets" button if there are missing budgets
                    if (uiState.missingBudgetsCount > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${uiState.missingBudgetsCount} budget${if (uiState.missingBudgetsCount > 1) "s" else ""} not copied",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "From previous month",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Button(
                                    onClick = { viewModel.copyMissingBudgets() },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = FeatherIcons.Copy,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Copy")
                                }
                            }
                        }
                    }

                                BudgetsList(
                        budgets = filteredBudgets,
                        onEditBudget = {
                            budgetToEdit = it
                            showEditBudget = true
                        },
                        onDeleteBudget = { viewModel.deleteBudget(it.budget) },
                        onAddBudgetItem = { budgetId ->
                            selectedBudgetId = budgetId
                            showAddBudgetItem = true
                        },
                        onEditBudgetItem = { item ->
                            budgetItemToEdit = item
                            showEditBudgetItem = true
                        },
                        onDeleteBudgetItem = { viewModel.deleteBudgetItem(it) },
                        onMarkAsPaid = { budgetWithDetails ->
                            if (isPremium) {
                                budgetToPayFor = budgetWithDetails
                                showPaymentConfirmation = true
                            } else {
                                showPremiumPaywall = true
                            }
                        },
                        currencySymbol = selectedCurrency.symbol,
                        isPremium = isPremium,
                        onShowPremiumPaywall = { showPremiumPaywall = true },
                        currentTheme = currentTheme,
                        isDarkMode = isDarkMode,
                        onScrollChanged = { isScrollingDown ->
                            fabVisible = !isScrollingDown
                        }
                    )
                            }
                        }
                    }
                }

                BudgetDisplayMode.CALENDAR -> {
                    BudgetCalendarView(
                        selectedDate = Pair(uiState.selectedMonth, uiState.selectedYear),
                        budgets = uiState.budgetsWithDetails,
                        selectedCalendarDate = uiState.selectedCalendarDate,
                        onDateSelected = { viewModel.selectCalendarDate(it) },
                        currentTheme = currentTheme,
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Error Message
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.loadBudgets() }) {
                            Text("Retry")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Add Budget Dialog
    if (showAddBudget) {
        AddEditBudgetDialog(
            budget = null,
            onDismiss = { showAddBudget = false },
            onSave = { name, category, month, year, isRecurring, dueDate, notifyDaysBefore ->
                viewModel.addBudget(name, category, month, year, isRecurring, dueDate, notifyDaysBefore)
                showAddBudget = false
            }
        )
    }

    // Edit Budget Dialog
    if (showEditBudget && budgetToEdit != null) {
        AddEditBudgetDialog(
            budget = budgetToEdit?.budget,
            onDismiss = {
                showEditBudget = false
                budgetToEdit = null
            },
            onSave = { name, category, month, year, isRecurring, dueDate, notifyDaysBefore ->
                budgetToEdit?.budget?.let { budget ->
                    viewModel.updateBudget(
                        budget.copy(
                            name = name,
                            category = category,
                            month = month,
                            year = year,
                            isRecurring = isRecurring,
                            dueDate = dueDate,
                            notifyDaysBefore = notifyDaysBefore
                        )
                    )
                }
                showEditBudget = false
                budgetToEdit = null
            }
        )
    }

    // Add Budget Item Dialog
    if (showAddBudgetItem && selectedBudgetId != null) {
        AddEditBudgetItemDialog(
            budgetItem = null,
            onDismiss = {
                showAddBudgetItem = false
                selectedBudgetId = null
            },
            onSave = { name, amount, note, type ->
                selectedBudgetId?.let { budgetId ->
                    viewModel.addBudgetItem(budgetId, name, amount, note, type)
                }
                showAddBudgetItem = false
                selectedBudgetId = null
            }
        )
    }

    // Edit Budget Item Dialog
    if (showEditBudgetItem && budgetItemToEdit != null) {
        AddEditBudgetItemDialog(
            budgetItem = budgetItemToEdit,
            onDismiss = {
                showEditBudgetItem = false
                budgetItemToEdit = null
            },
            onSave = { name, amount, note, type ->
                budgetItemToEdit?.let { item ->
                    viewModel.updateBudgetItem(
                        item.copy(
                            name = name,
                            amount = amount,
                            note = note,
                            type = type
                        )
                    )
                }
                showEditBudgetItem = false
                budgetItemToEdit = null
            }
        )
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

    // Payment Confirmation Dialog
    if (showPaymentConfirmation && budgetToPayFor != null) {
        PaymentConfirmationDialog(
            budget = budgetToPayFor!!.budget,
            plannedAmount = budgetToPayFor!!.planned,
            accounts = accounts,
            currencySymbol = selectedCurrency.symbol,
            onDismiss = {
                showPaymentConfirmation = false
                budgetToPayFor = null
            },
            onConfirm = { amount, accountId, date ->
                viewModel.createPaymentTransaction(
                    budget = budgetToPayFor!!.budget,
                    amount = amount,
                    accountId = accountId,
                    date = date
                )
                showPaymentConfirmation = false
                budgetToPayFor = null
            }
        )
    }
}

@Composable
fun UnifiedBudgetHeaderSection(
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    allocated: Double,
    spent: Double,
    remaining: Double,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
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

        // Stats Cards (2 columns: Allocated | Remaining)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Allocated Card
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
                        text = "Allocated",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%.2f", allocated)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.getAccent(isDarkMode)
                    )
                }
            }

            // Remaining Card
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
                        text = "Remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%.2f", remaining)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining >= 0) Color(0xFF34C759) else Color(0xFFFF3B30)
                    )
                }
            }
        }

        // Monthly Spent Card - with gradient background
        val spentGradient = Brush.horizontalGradient(
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
                    .background(spentGradient)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isMonochrome = currentTheme.id == "monochrome"
                val isMonochromeDark = isMonochrome && isDarkMode

                Text(
                    text = "$monthText Spent",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isMonochromeDark -> Color(0xFF1A1A1A)  // Black for dark mode gradient
                        isMonochrome -> Color.White  // White for light mode gradient
                        else -> Color(0xFF1A1A1A)  // Dark text for colored gradients
                    }
                )
                Text(
                    text = "$currencySymbol${String.format("%.2f", spent)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isMonochromeDark -> Color(0xFF2E7D32)  // Darker green for dark mode
                        isMonochrome -> Color(0xFFB2FF59)  // Bright lime for light mode
                        else -> Color(0xFFFF3B30)  // Default red
                    }
                )
            }
        }
    }
}

@Composable
fun StatColumn(label: String, amount: Double, color: Color, currencySymbol: String) {
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
fun BudgetsList(
    budgets: List<BudgetWithDetails>,
    onEditBudget: (BudgetWithDetails) -> Unit,
    onDeleteBudget: (BudgetWithDetails) -> Unit,
    onAddBudgetItem: (String) -> Unit,
    onEditBudgetItem: (com.spendsee.data.local.entities.BudgetItem) -> Unit,
    onDeleteBudgetItem: (com.spendsee.data.local.entities.BudgetItem) -> Unit,
    onMarkAsPaid: (BudgetWithDetails) -> Unit,
    currencySymbol: String,
    isPremium: Boolean,
    onShowPremiumPaywall: () -> Unit,
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
        items(budgets) { budgetWithDetails ->
            BudgetCard(
                budgetWithDetails = budgetWithDetails,
                onEdit = { onEditBudget(budgetWithDetails) },
                onDelete = { onDeleteBudget(budgetWithDetails) },
                onAddItem = { onAddBudgetItem(budgetWithDetails.budget.id) },
                onEditItem = onEditBudgetItem,
                onDeleteItem = onDeleteBudgetItem,
                onMarkAsPaid = { onMarkAsPaid(budgetWithDetails) },
                currencySymbol = currencySymbol,
                isPremium = isPremium,
                onShowPremiumPaywall = onShowPremiumPaywall,
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCard(
    budgetWithDetails: BudgetWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (com.spendsee.data.local.entities.BudgetItem) -> Unit,
    onDeleteItem: (com.spendsee.data.local.entities.BudgetItem) -> Unit,
    onMarkAsPaid: () -> Unit,
    currencySymbol: String,
    isPremium: Boolean,
    onShowPremiumPaywall: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showItemMenu by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<com.spendsee.data.local.entities.BudgetItem?>(null) }
    val isOverBudget = budgetWithDetails.spent > budgetWithDetails.planned

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = currentTheme.getSurface(isDarkMode)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budgetWithDetails.budget.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.getText(isDarkMode)
                    )
                    Text(
                        text = budgetWithDetails.budget.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (expanded) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = currentTheme.getInactive(isDarkMode)
                    )

                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            FeatherIcons.MoreVertical,
                            contentDescription = "More Options",
                            tint = currentTheme.getInactive(isDarkMode)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = (budgetWithDetails.percentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isOverBudget) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary,
                trackColor = if (isOverBudget) Color(0xFFEF5350).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Planned",
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Text(
                        text = formatCurrency(budgetWithDetails.planned, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = currentTheme.getText(isDarkMode)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Text(
                        text = formatCurrency(budgetWithDetails.spent, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverBudget) Color(0xFFEF5350) else currentTheme.getText(isDarkMode)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = currentTheme.getInactive(isDarkMode)
                    )
                    Text(
                        text = formatCurrency(budgetWithDetails.remaining, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (budgetWithDetails.remaining < 0) Color(0xFFEF5350) else Color(0xFF66BB6A)
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mark as Paid Button (only show if has due date and not yet paid)
                    if (budgetWithDetails.budget.dueDate != null && !budgetWithDetails.budget.isPaid) {
                        Button(
                            onClick = {
                                if (isPremium) {
                                    onMarkAsPaid()
                                } else {
                                    onShowPremiumPaywall()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = FeatherIcons.CheckCircle,
                                        contentDescription = "Mark as Paid",
                                        tint = Color.White
                                    )
                                    Text(
                                        text = "Mark as Paid",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (!isPremium) {
                                        Icon(
                                            imageVector = FeatherIcons.Award,
                                            contentDescription = "Premium",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFFFD700)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = FeatherIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Budget Items",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getText(isDarkMode)
                        )

                        IconButton(onClick = onAddItem) {
                            Icon(
                                FeatherIcons.Plus,
                                contentDescription = "Add Budget Item",
                                tint = currentTheme.getAccent(isDarkMode)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (budgetWithDetails.items.isEmpty()) {
                        Text(
                            text = "No budget items yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getInactive(isDarkMode),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        budgetWithDetails.items.forEach { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = currentTheme.getBackground(isDarkMode)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedItem = item
                                            showItemMenu = true
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = currentTheme.getText(isDarkMode)
                                        )
                                        if (item.note.isNotEmpty()) {
                                            Text(
                                                text = item.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = currentTheme.getInactive(isDarkMode)
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatCurrency(item.amount, currencySymbol),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = currentTheme.getText(isDarkMode)
                                        )
                                        if (item.type == "Unplanned") {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                FeatherIcons.AlertCircle,
                                                contentDescription = "Unplanned",
                                                tint = Color(0xFFFF9500),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Budget Item Context Menu
                    DropdownMenu(
                        expanded = showItemMenu,
                        onDismissRequest = { showItemMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                selectedItem?.let { onEditItem(it) }
                                showItemMenu = false
                            },
                            leadingIcon = {
                                Icon(FeatherIcons.Edit2, contentDescription = "Edit")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                selectedItem?.let { onDeleteItem(it) }
                                showItemMenu = false
                            },
                            leadingIcon = {
                                Icon(FeatherIcons.Trash2, contentDescription = "Delete", tint = Color.Red)
                            }
                        )
                    }
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
                        Icon(FeatherIcons.Edit, contentDescription = "Edit")
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
}

@Composable
fun EmptyState(
    onCopyFromPrevious: () -> Unit,
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
                imageVector = FeatherIcons.DollarSign,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = currentTheme.getInactive(isDarkMode)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Budgets",
                style = MaterialTheme.typography.titleMedium,
                color = currentTheme.getText(isDarkMode),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to create your first budget",
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.getInactive(isDarkMode),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onCopyFromPrevious,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = FeatherIcons.Copy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy from Previous Month")
            }
        }
    }
}

@Composable
fun EmptyFilteredState(
    onClearFilter: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.getBackground(isDarkMode)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = FeatherIcons.Calendar,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = currentTheme.getText(isDarkMode).copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Budgets Due",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = currentTheme.getText(isDarkMode)
            )
            Text(
                text = "No budgets are due on this date",
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.getText(isDarkMode).copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClearFilter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentTheme.getAccent(isDarkMode)
                )
            ) {
                Text("Clear Filter")
            }
        }
    }
}

private fun formatCurrency(amount: Double, currencySymbol: String): String {
    val formatter = java.text.DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
}

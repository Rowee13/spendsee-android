package com.spendsee.ui.screens.budgets

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spendsee.R
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.managers.CurrencyManager
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
                context
            )
        )
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val selectedCurrency by currencyManager.selectedCurrency.collectAsState()
    var showAddBudget by remember { mutableStateOf(false) }
    var showEditBudget by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<BudgetWithDetails?>(null) }
    var showAddBudgetItem by remember { mutableStateOf(false) }
    var showEditBudgetItem by remember { mutableStateOf(false) }
    var budgetItemToEdit by remember { mutableStateOf<com.spendsee.data.local.entities.BudgetItem?>(null) }
    var selectedBudgetId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBudget = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(FeatherIcons.Plus, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                currencySymbol = selectedCurrency.symbol
            )

            // Budgets List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.budgetsWithDetails.isEmpty()) {
                EmptyState()
            } else {
                BudgetsList(
                    budgets = uiState.budgetsWithDetails,
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
                    onMarkAsPaid = { budget, isPaid ->
                        viewModel.markBudgetAsPaid(budget, isPaid)
                    },
                    currencySymbol = selectedCurrency.symbol
                )
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
    currencySymbol: String
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.YEAR, selectedYear)
    }
    val monthYearText = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
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
                    modifier = Modifier.size(32.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "SpendSee",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Month Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        FeatherIcons.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        FeatherIcons.ChevronRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(
                    label = "Allocated",
                    amount = allocated,
                    color = MaterialTheme.colorScheme.primary,
                    currencySymbol = currencySymbol
                )

                StatColumn(
                    label = "Spent",
                    amount = spent,
                    color = Color(0xFFEF5350),
                    currencySymbol = currencySymbol
                )

                StatColumn(
                    label = "Remaining",
                    amount = remaining,
                    color = if (remaining >= 0) Color(0xFF66BB6A) else Color(0xFFEF5350),
                    currencySymbol = currencySymbol
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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
    onMarkAsPaid: (com.spendsee.data.local.entities.Budget, Boolean) -> Unit,
    currencySymbol: String
) {
    LazyColumn(
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
                onMarkAsPaid = { isPaid -> onMarkAsPaid(budgetWithDetails.budget, isPaid) },
                currencySymbol = currencySymbol
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
    onMarkAsPaid: (Boolean) -> Unit,
    currencySymbol: String
) {
    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showItemMenu by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<com.spendsee.data.local.entities.BudgetItem?>(null) }
    val isOverBudget = budgetWithDetails.spent > budgetWithDetails.planned

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = budgetWithDetails.budget.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (expanded) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            FeatherIcons.MoreVertical,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budgetWithDetails.planned, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budgetWithDetails.spent, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverBudget) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Budget Items",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(onClick = onAddItem) {
                            Icon(
                                FeatherIcons.Plus,
                                contentDescription = "Add Budget Item",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (budgetWithDetails.items.isEmpty()) {
                        Text(
                            text = "No budget items yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        budgetWithDetails.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedItem = item
                                        showItemMenu = true
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (item.note.isNotEmpty()) {
                                        Text(
                                            text = item.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatCurrency(item.amount, currencySymbol),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
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
                if (budgetWithDetails.budget.dueDate != null) {
                    DropdownMenuItem(
                        text = { Text(if (budgetWithDetails.budget.isPaid) "Mark as Unpaid" else "Mark as Paid") },
                        onClick = {
                            showMenu = false
                            onMarkAsPaid(!budgetWithDetails.budget.isPaid)
                        },
                        leadingIcon = {
                            Icon(
                                if (budgetWithDetails.budget.isPaid) FeatherIcons.X else FeatherIcons.Check,
                                contentDescription = if (budgetWithDetails.budget.isPaid) "Mark as Unpaid" else "Mark as Paid"
                            )
                        }
                    )
                }
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
                imageVector = FeatherIcons.DollarSign,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Budgets",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to create your first budget",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCurrency(amount: Double, currencySymbol: String): String {
    return "$currencySymbol${String.format("%.2f", amount)}"
}

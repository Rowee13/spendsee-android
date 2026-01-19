package com.spendsee.ui.screens.analysis

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
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.managers.CurrencyManager
import com.spendsee.managers.ThemeManager
import com.spendsee.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = viewModel(
        factory = AnalysisViewModelFactory(
            TransactionRepository.getInstance(LocalContext.current),
            BudgetRepository.getInstance(LocalContext.current)
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
            isDarkMode = isDarkMode
        )

        // View Type Selector
        ViewTypeSelector(
            selectedViewType = uiState.selectedViewType,
            onViewTypeSelected = { viewModel.setViewType(it) }
        )

        // Content based on selected view type
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.totalExpenses == 0.0 && uiState.totalIncome == 0.0) {
            EmptyState(
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )
        } else {
            when (uiState.selectedViewType) {
                AnalysisViewType.SPENDING_ANALYTICS -> {
                    SpendingAnalytics(
                        categoryBreakdowns = uiState.categoryBreakdowns,
                        currencySymbol = selectedCurrency.symbol
                    )
                }
                AnalysisViewType.BUDGET_PERFORMANCE -> {
                    BudgetPerformance(
                        budgetPerformances = uiState.budgetPerformances,
                        currencySymbol = selectedCurrency.symbol
                    )
                }
                AnalysisViewType.CASH_FLOW -> {
                    CashFlow(
                        dailyCashFlows = uiState.dailyCashFlows,
                        currencySymbol = selectedCurrency.symbol
                    )
                }
            }
        }

        // Error Message
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.loadData() }) {
                        Text("Retry")
                    }
                }
            ) {
                Text(error)
            }
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

        // Total Net Card - with gradient background
        val netGradient = Brush.horizontalGradient(
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
                    .background(netGradient)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isMonochrome = currentTheme.id == "monochrome"
                val isMonochromeDark = isMonochrome && isDarkMode

                Text(
                    text = "$monthText Net",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isMonochromeDark -> Color(0xFF1A1A1A)  // Black for dark mode gradient
                        isMonochrome -> Color.White  // White for light mode gradient
                        else -> Color(0xFF1A1A1A)  // Default dark text
                    }
                )
                Text(
                    text = "$currencySymbol${String.format("%.2f", net)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isMonochromeDark -> if (net >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)  // Darker colors for dark mode
                        isMonochrome -> if (net >= 0) Color(0xFFB2FF59) else Color(0xFFFF6B6B)  // Bright colors for light mode
                        else -> if (net >= 0) Color(0xFF1E7E34) else Color(0xFFFF3B30)  // Default
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTypeSelector(
    selectedViewType: AnalysisViewType,
    onViewTypeSelected: (AnalysisViewType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedViewType.displayName(),
                onValueChange = {},
                readOnly = true,
                label = { Text("View Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                AnalysisViewType.values().forEach { viewType ->
                    DropdownMenuItem(
                        text = { Text(viewType.displayName()) },
                        onClick = {
                            onViewTypeSelected(viewType)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingAnalytics(categoryBreakdowns: List<CategoryBreakdown>, currencySymbol: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Expense by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(categoryBreakdowns) { breakdown ->
            CategoryBreakdownCard(breakdown, currencySymbol)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryBreakdownCard(breakdown: CategoryBreakdown, currencySymbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(breakdown.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = breakdown.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${breakdown.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = breakdown.percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(breakdown.color),
                trackColor = Color(breakdown.color).copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatCurrency(breakdown.amount, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BudgetPerformance(budgetPerformances: List<BudgetPerformance>, currencySymbol: String) {
    // Note: EmptyState requires theme parameters, but this composable doesn't have access to them
    // This is acceptable as this check should rarely trigger - the parent already checks for empty data
    if (budgetPerformances.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No budgets for this month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Budget vs Actual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(budgetPerformances) { performance ->
                BudgetPerformanceCard(performance, currencySymbol)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BudgetPerformanceCard(performance: BudgetPerformance, currencySymbol: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = performance.budgetName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                        text = formatCurrency(performance.planned, currencySymbol),
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
                        text = formatCurrency(performance.spent, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (performance.spent > performance.planned) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (performance.percentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (performance.spent > performance.planned) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CashFlow(dailyCashFlows: List<DailyCashFlow>, currencySymbol: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Daily Cash Flow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(dailyCashFlows) { cashFlow ->
            DailyCashFlowCard(cashFlow, currencySymbol)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DailyCashFlowCard(cashFlow: DailyCashFlow, currencySymbol: String) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFormat.format(Date(cashFlow.date)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(cashFlow.income, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF66BB6A),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatCurrency(cashFlow.expense, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = formatCurrency(cashFlow.net, currencySymbol),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (cashFlow.net >= 0) Color(0xFF66BB6A) else Color(0xFFEF5350)
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String = "No data available",
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
                imageVector = FeatherIcons.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = currentTheme.getInactive(isDarkMode)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = currentTheme.getText(isDarkMode),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCurrency(amount: Double, currencySymbol: String): String {
    val formatter = java.text.DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
}

private fun AnalysisViewType.displayName(): String {
    return when (this) {
        AnalysisViewType.BUDGET_PERFORMANCE -> "Budget Performance"
        AnalysisViewType.SPENDING_ANALYTICS -> "Spending Analytics"
        AnalysisViewType.CASH_FLOW -> "Cash Flow"
    }
}

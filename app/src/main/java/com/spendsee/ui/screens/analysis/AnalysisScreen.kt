package com.spendsee.ui.screens.analysis

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        // FIXED: Logo + month navigation header
        AnalysisFixedHeader(
            selectedMonth = uiState.selectedMonth,
            selectedYear = uiState.selectedYear,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            currentTheme = currentTheme,
            isDarkMode = isDarkMode
        )

        // SCROLLABLE: AI card + selector + analytics content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // AI Assessment placeholder card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) currentTheme.getSurface(isDarkMode) else Color(0xFFFFF5F0))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI assessment on spending and saving patterns",
                    style = MaterialTheme.typography.bodyMedium,
                    color = currentTheme.getAccent(isDarkMode),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Analytics type selector (left-aligned dropdown)
            AnalyticsTypeDropdown(
                selectedViewType = uiState.selectedViewType,
                onViewTypeSelected = { viewModel.setViewType(it) },
                currentTheme = currentTheme,
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Analytics content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.totalExpenses == 0.0 && uiState.totalIncome == 0.0) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = FeatherIcons.BarChart, contentDescription = null, modifier = Modifier.size(64.dp), tint = currentTheme.getInactive(isDarkMode))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No data available", style = MaterialTheme.typography.titleMedium, color = currentTheme.getText(isDarkMode), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    }
                }
            } else {
                when (uiState.selectedViewType) {
                    AnalysisViewType.SPENDING_ANALYTICS -> SpendingAnalyticsContent(uiState.categoryBreakdowns, selectedCurrency.symbol, currentTheme, isDarkMode)
                    AnalysisViewType.BUDGET_PERFORMANCE -> BudgetPerformanceContent(uiState.budgetPerformances, selectedCurrency.symbol, currentTheme, isDarkMode)
                    AnalysisViewType.CASH_FLOW -> CashFlowContent(uiState.dailyCashFlows, selectedCurrency.symbol, currentTheme, isDarkMode)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Error Message
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { viewModel.loadData() }) { Text("Retry") } }
            ) { Text(error) }
        }
    }
}

@Composable
fun AnalysisFixedHeader(
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.YEAR, selectedYear)
    }
    val monthYearText = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

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
                imageVector = FeatherIcons.Bell,
                contentDescription = "Notifications",
                tint = currentTheme.getInactive(isDarkMode),
                modifier = Modifier.size(22.dp)
            )
        }

        // Month navigation: large title left, chevrons right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYearText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(currentTheme.getBorder(isDarkMode).copy(alpha = 0.5f))
                        .clickable { onPreviousMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FeatherIcons.ChevronLeft, contentDescription = "Previous Month", tint = currentTheme.getAccent(isDarkMode), modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(currentTheme.getBorder(isDarkMode).copy(alpha = 0.5f))
                        .clickable { onNextMonth() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(FeatherIcons.ChevronRight, contentDescription = "Next Month", tint = currentTheme.getAccent(isDarkMode), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsTypeDropdown(
    selectedViewType: AnalysisViewType,
    onViewTypeSelected: (AnalysisViewType) -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedViewType.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = currentTheme.getText(isDarkMode)
            )
            Icon(
                imageVector = if (expanded) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown,
                contentDescription = null,
                tint = currentTheme.getText(isDarkMode),
                modifier = Modifier.size(20.dp).padding(start = 4.dp)
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(currentTheme.getSurface(isDarkMode))
        ) {
            AnalysisViewType.values().forEach { viewType ->
                DropdownMenuItem(
                    text = { Text(viewType.displayName(), color = currentTheme.getText(isDarkMode)) },
                    onClick = { onViewTypeSelected(viewType); expanded = false }
                )
            }
        }
    }
}

@Composable
fun SpendingAnalyticsContent(categoryBreakdowns: List<CategoryBreakdown>, currencySymbol: String, currentTheme: ThemeColors, isDarkMode: Boolean) {
    Column {
        Text(
            text = "Expense by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = currentTheme.getText(isDarkMode),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        categoryBreakdowns.forEach { breakdown ->
            CategoryBreakdownCard(breakdown, currencySymbol, currentTheme, isDarkMode)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BudgetPerformanceContent(budgetPerformances: List<BudgetPerformance>, currencySymbol: String, currentTheme: ThemeColors, isDarkMode: Boolean) {
    if (budgetPerformances.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
            Text(text = "No budgets for this month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = currentTheme.getText(isDarkMode), textAlign = TextAlign.Center)
        }
    } else {
        Column {
            Text(
                text = "Budget vs Actual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = currentTheme.getText(isDarkMode),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            budgetPerformances.forEach { performance ->
                BudgetPerformanceCard(performance, currencySymbol, currentTheme, isDarkMode)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CashFlowContent(dailyCashFlows: List<DailyCashFlow>, currencySymbol: String, currentTheme: ThemeColors, isDarkMode: Boolean) {
    Column {
        Text(
            text = "Daily Cash Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = currentTheme.getText(isDarkMode),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        dailyCashFlows.forEach { cashFlow ->
            DailyCashFlowCard(cashFlow, currencySymbol, currentTheme, isDarkMode)
            Spacer(modifier = Modifier.height(8.dp))
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
    onViewTypeSelected: (AnalysisViewType) -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = currentTheme.getSurface(isDarkMode),
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = currentTheme.getText(isDarkMode),
                    unfocusedTextColor = currentTheme.getText(isDarkMode),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = currentTheme.getAccent(isDarkMode),
                    unfocusedBorderColor = currentTheme.getBorder(isDarkMode),
                    focusedLabelColor = currentTheme.getAccent(isDarkMode),
                    unfocusedLabelColor = currentTheme.getInactive(isDarkMode)
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(currentTheme.getSurface(isDarkMode))
            ) {
                AnalysisViewType.values().forEach { viewType ->
                    DropdownMenuItem(
                        text = { Text(viewType.displayName(), color = currentTheme.getText(isDarkMode)) },
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
fun CategoryBreakdownCard(breakdown: CategoryBreakdown, currencySymbol: String, currentTheme: ThemeColors, isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = currentTheme.getSurface(isDarkMode)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(breakdown.color)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = breakdown.category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = currentTheme.getText(isDarkMode))
                }
                Text(text = "${breakdown.percentage.toInt()}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = currentTheme.getAccent(isDarkMode))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = breakdown.percentage / 100f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(breakdown.color),
                trackColor = Color(breakdown.color).copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = formatCurrency(breakdown.amount, currencySymbol), style = MaterialTheme.typography.bodyMedium, color = currentTheme.getInactive(isDarkMode))
        }
    }
}


@Composable
fun BudgetPerformanceCard(performance: BudgetPerformance, currencySymbol: String, currentTheme: ThemeColors, isDarkMode: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = currentTheme.getSurface(isDarkMode)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = performance.budgetName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = currentTheme.getText(isDarkMode))

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Planned", style = MaterialTheme.typography.bodySmall, color = currentTheme.getInactive(isDarkMode))
                    Text(text = formatCurrency(performance.planned, currencySymbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = currentTheme.getText(isDarkMode))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Spent", style = MaterialTheme.typography.bodySmall, color = currentTheme.getInactive(isDarkMode))
                    Text(
                        text = formatCurrency(performance.spent, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (performance.spent > performance.planned) Color(0xFFEF5350) else currentTheme.getText(isDarkMode)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = (performance.percentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (performance.spent > performance.planned) Color(0xFFEF5350) else currentTheme.getAccent(isDarkMode),
                trackColor = if (performance.spent > performance.planned) Color(0xFFEF5350).copy(alpha = 0.2f) else currentTheme.getAccent(isDarkMode).copy(alpha = 0.2f)
            )
        }
    }
}


@Composable
fun DailyCashFlowCard(cashFlow: DailyCashFlow, currencySymbol: String, currentTheme: ThemeColors, isDarkMode: Boolean) {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, currentTheme.getBorder(isDarkMode), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = currentTheme.getSurface(isDarkMode)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                fontWeight = FontWeight.Medium,
                color = currentTheme.getText(isDarkMode)
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

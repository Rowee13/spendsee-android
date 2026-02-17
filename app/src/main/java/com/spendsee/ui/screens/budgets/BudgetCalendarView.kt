package com.spendsee.ui.screens.budgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendsee.ui.theme.ThemeColors
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetCalendarView(
    selectedDate: Pair<Int, Int>, // month, year
    budgets: List<BudgetWithDetails>,
    selectedCalendarDate: Long?,
    onDateSelected: (Long?) -> Unit,
    onEditBudget: (BudgetWithDetails) -> Unit,
    onDeleteBudget: (BudgetWithDetails) -> Unit,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val calendarDays = remember(selectedDate, budgets) {
        generateCalendarDays(selectedDate.first, selectedDate.second, budgets)
    }

    val selectedDayBudgets = remember(selectedCalendarDate, calendarDays) {
        selectedCalendarDate?.let { date ->
            calendarDays.find { it.timestamp == date }?.budgets ?: emptyList()
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(currentTheme.getBackground(isDarkMode))
    ) {
        // Selected Date Banner (if date is selected)
        AnimatedVisibility(
            visible = selectedCalendarDate != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            selectedCalendarDate?.let { date ->
                SelectedDateBanner(
                    date = date,
                    budgetCount = selectedDayBudgets.size,
                    onClear = { onDateSelected(null) },
                    currentTheme = currentTheme,
                    isDarkMode = isDarkMode
                )
            }
        }

        // Calendar Grid Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = currentTheme.getSurface(isDarkMode)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Weekday Headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = currentTheme.getInactive(isDarkMode),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid (7 columns × 6 rows)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false  // Fixed height, no scrolling
                ) {
                    items(calendarDays) { day ->
                        CalendarDayCell(
                            day = day,
                            isSelected = selectedCalendarDate == day.timestamp,
                            onTap = {
                                if (day.isCurrentMonth) {
                                    onDateSelected(
                                        if (selectedCalendarDate == day.timestamp) null else day.timestamp
                                    )
                                }
                            },
                            currentTheme = currentTheme,
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem("Has budgets due", Color.Blue, currentTheme, isDarkMode)
            LegendItem("Overdue unpaid", Color.Red, currentTheme, isDarkMode)
        }

        // Helper text or Budget List
        if (selectedCalendarDate == null) {
            Text(
                text = "Tap a date to see budgets due on that day",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.getText(isDarkMode).copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        } else {
            // Budget list for selected date
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Budgets Due",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.getText(isDarkMode),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (selectedDayBudgets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No budgets due on this date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.getInactive(isDarkMode),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    selectedDayBudgets.forEach { budget ->
                        CalendarBudgetCard(
                            budgetWithDetails = budget,
                            onEdit = { onEditBudget(budget) },
                            onDelete = { onDeleteBudget(budget) },
                            currencySymbol = currencySymbol,
                            currentTheme = currentTheme,
                            isDarkMode = isDarkMode
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Bottom padding for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarBudgetCard(
    budgetWithDetails: BudgetWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    currencySymbol: String,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
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
                .padding(16.dp)
        ) {
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

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More Options",
                            tint = currentTheme.getInactive(isDarkMode)
                        )
                    }
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
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
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

            // Planned / Spent / Remaining
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
                        text = formatCalendarCurrency(budgetWithDetails.planned, currencySymbol),
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
                        text = formatCalendarCurrency(budgetWithDetails.spent, currencySymbol),
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
                        text = formatCalendarCurrency(budgetWithDetails.remaining, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (budgetWithDetails.remaining < 0) Color(0xFFEF5350) else Color(0xFF66BB6A)
                    )
                }
            }

            // Paid status badge
            if (budgetWithDetails.budget.isPaid) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Data class for each calendar day
data class CalendarDayData(
    val day: Int,
    val timestamp: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val budgets: List<BudgetWithDetails>
) {
    val hasOverdueUnpaid: Boolean
        get() {
            val today = System.currentTimeMillis()
            return budgets.any { budget ->
                budget.budget.dueDate?.let { it < today && !budget.budget.isPaid } ?: false
            }
        }
}

@Composable
fun CalendarDayCell(
    day: CalendarDayData,
    isSelected: Boolean,
    onTap: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val textColor = when {
        !day.isCurrentMonth -> currentTheme.getInactive(isDarkMode).copy(alpha = 0.3f)
        day.isToday -> currentTheme.getAccent(isDarkMode)
        else -> currentTheme.getText(isDarkMode)
    }

    val backgroundColor = when {
        isSelected -> currentTheme.getAccent(isDarkMode).copy(alpha = 0.15f)
        day.isToday -> currentTheme.getAccent(isDarkMode).copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> currentTheme.getAccent(isDarkMode)
        day.isToday -> currentTheme.getAccent(isDarkMode).copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val indicatorColor = if (day.hasOverdueUnpaid) Color.Red else Color.Blue

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(enabled = day.isCurrentMonth) { onTap() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Day number
            Text(
                text = day.day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Indicator dots
            if (day.budgets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )

                    if (day.budgets.size > 1) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = day.budgets.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f,
                            color = indicatorColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Empty space to maintain consistent cell height
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SelectedDateBanner(
    date: Long,
    budgetCount: Int,
    onClear: () -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val dateText = dateFormat.format(Date(date))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(currentTheme.getAccent(isDarkMode).copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.getText(isDarkMode)
                )
                Text(
                    text = "$budgetCount budget${if (budgetCount != 1) "s" else ""} due",
                    style = MaterialTheme.typography.bodySmall,
                    color = currentTheme.getText(isDarkMode).copy(alpha = 0.7f)
                )
            }

            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentTheme.getBorder(isDarkMode).copy(alpha = 0.3f),
                    contentColor = currentTheme.getText(isDarkMode)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Bottom accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(currentTheme.getAccent(isDarkMode))
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun LegendItem(
    text: String,
    color: Color,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.7f))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = currentTheme.getText(isDarkMode).copy(alpha = 0.7f)
        )
    }
}

// Generate calendar days for the month
fun generateCalendarDays(
    month: Int, // 1-12
    year: Int,
    budgets: List<BudgetWithDetails>
): List<CalendarDayData> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1) // Calendar months are 0-indexed
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    // Get first day of month (1 = Sunday, 7 = Saturday)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // Days from previous month to show
    val daysFromPreviousMonth = firstDayOfWeek - 1

    // Days in current month
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Total cells (always 42 = 6 weeks × 7 days)
    val totalCells = 42
    val daysFromNextMonth = totalCells - daysInMonth - daysFromPreviousMonth

    val days = mutableListOf<CalendarDayData>()
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)
    val todayTimestamp = today.timeInMillis

    // Helper function to check if a budget is due on a specific day
    fun isBudgetDueOnDay(budget: BudgetWithDetails, dayTimestamp: Long): Boolean {
        val budgetDueDate = budget.budget.dueDate ?: return false
        val budgetCal = Calendar.getInstance().apply { timeInMillis = budgetDueDate }
        val dayCal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }

        return budgetCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
               budgetCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
    }

    // Previous month days (grayed out)
    if (daysFromPreviousMonth > 0) {
        val prevMonthCalendar = calendar.clone() as Calendar
        prevMonthCalendar.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in (daysInPrevMonth - daysFromPreviousMonth + 1)..daysInPrevMonth) {
            prevMonthCalendar.set(Calendar.DAY_OF_MONTH, i)
            prevMonthCalendar.set(Calendar.HOUR_OF_DAY, 0)
            prevMonthCalendar.set(Calendar.MINUTE, 0)
            prevMonthCalendar.set(Calendar.SECOND, 0)
            prevMonthCalendar.set(Calendar.MILLISECOND, 0)

            days.add(
                CalendarDayData(
                    day = i,
                    timestamp = prevMonthCalendar.timeInMillis,
                    isCurrentMonth = false,
                    isToday = false,
                    budgets = emptyList()
                )
            )
        }
    }

    // Current month days
    for (i in 1..daysInMonth) {
        calendar.set(Calendar.DAY_OF_MONTH, i)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayTimestamp = calendar.timeInMillis
        val isToday = dayTimestamp == todayTimestamp

        // Filter budgets for this specific date
        val budgetsForDay = budgets.filter { budget ->
            isBudgetDueOnDay(budget, dayTimestamp)
        }

        days.add(
            CalendarDayData(
                day = i,
                timestamp = dayTimestamp,
                isCurrentMonth = true,
                isToday = isToday,
                budgets = budgetsForDay
            )
        )
    }

    // Next month days (grayed out)
    if (daysFromNextMonth > 0) {
        val nextMonthCalendar = calendar.clone() as Calendar
        nextMonthCalendar.add(Calendar.MONTH, 1)

        for (i in 1..daysFromNextMonth) {
            nextMonthCalendar.set(Calendar.DAY_OF_MONTH, i)
            nextMonthCalendar.set(Calendar.HOUR_OF_DAY, 0)
            nextMonthCalendar.set(Calendar.MINUTE, 0)
            nextMonthCalendar.set(Calendar.SECOND, 0)
            nextMonthCalendar.set(Calendar.MILLISECOND, 0)

            days.add(
                CalendarDayData(
                    day = i,
                    timestamp = nextMonthCalendar.timeInMillis,
                    isCurrentMonth = false,
                    isToday = false,
                    budgets = emptyList()
                )
            )
        }
    }

    return days
}

private fun formatCalendarCurrency(amount: Double, currencySymbol: String): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$currencySymbol${formatter.format(amount)}"
}

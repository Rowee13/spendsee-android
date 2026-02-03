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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.X
import com.spendsee.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetCalendarView(
    selectedDate: Pair<Int, Int>, // month, year
    budgets: List<BudgetWithDetails>,
    selectedCalendarDate: Long?,
    onDateSelected: (Long?) -> Unit,
    currentTheme: ThemeColors,
    isDarkMode: Boolean
) {
    val calendarDays = remember(selectedDate, budgets) {
        generateCalendarDays(selectedDate.first, selectedDate.second, budgets)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    budgetCount = calendarDays
                        .find { it.timestamp == date }
                        ?.budgets?.size ?: 0,
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

        // Helper text
        if (selectedCalendarDate == null) {
            Text(
                text = "Tap a date to filter budgets by due date",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = currentTheme.getText(isDarkMode).copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
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
                    imageVector = FeatherIcons.X,
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

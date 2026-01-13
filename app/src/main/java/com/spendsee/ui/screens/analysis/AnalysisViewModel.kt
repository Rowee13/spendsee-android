package com.spendsee.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AnalysisViewType {
    BUDGET_PERFORMANCE,
    SPENDING_ANALYTICS,
    CASH_FLOW
}

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Int
)

data class BudgetPerformance(
    val budgetName: String,
    val planned: Double,
    val spent: Double,
    val percentage: Float
)

data class DailyCashFlow(
    val date: Long,
    val income: Double,
    val expense: Double,
    val net: Double
)

data class AnalysisUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedViewType: AnalysisViewType = AnalysisViewType.SPENDING_ANALYTICS,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netTotal: Double = 0.0,
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val budgetPerformances: List<BudgetPerformance> = emptyList(),
    val dailyCashFlows: List<DailyCashFlow> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AnalysisViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val startOfMonth = getStartOfMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear)
                val endOfMonth = getEndOfMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear)

                // Get transactions once
                val transactions = transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth).first()
                val expenses = transactions.filter { it.type == "expense" }
                val income = transactions.filter { it.type == "income" }

                val totalExpenses = expenses.sumOf { it.amount }
                val totalIncome = income.sumOf { it.amount }

                // Calculate category breakdowns
                val categoryBreakdowns = expenses
                    .groupBy { it.category }
                    .map { (category, trans) ->
                        val amount = trans.sumOf { it.amount }
                        CategoryBreakdown(
                            category = category,
                            amount = amount,
                            percentage = if (totalExpenses > 0) (amount / totalExpenses * 100).toFloat() else 0f,
                            color = getCategoryColor(category)
                        )
                    }
                    .sortedByDescending { it.amount }

                // Calculate budget performances
                val budgetPerformances = mutableListOf<BudgetPerformance>()
                val budgets = budgetRepository.getBudgetsByMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                budgets.forEach { budget ->
                    val items = budgetRepository.getBudgetItems(budget.id).first()
                    val budgetTotal = items.sumOf { it.amount }
                    val spent = expenses
                        .filter { it.budgetId == budget.id }
                        .sumOf { it.amount }

                    budgetPerformances.add(
                        BudgetPerformance(
                            budgetName = budget.name,
                            planned = budgetTotal,
                            spent = spent,
                            percentage = if (budgetTotal > 0) (spent / budgetTotal * 100).toFloat() else 0f
                        )
                    )
                }

                // Calculate daily cash flows
                val dailyCashFlows = transactions
                    .groupBy { getDayTimestamp(it.date) }
                    .map { (day, trans) ->
                        val dayIncome = trans.filter { it.type == "income" }.sumOf { it.amount }
                        val dayExpense = trans.filter { it.type == "expense" }.sumOf { it.amount }
                        DailyCashFlow(
                            date = day,
                            income = dayIncome,
                            expense = dayExpense,
                            net = dayIncome - dayExpense
                        )
                    }
                    .sortedBy { it.date }

                _uiState.update {
                    it.copy(
                        totalExpenses = totalExpenses,
                        totalIncome = totalIncome,
                        netTotal = totalIncome - totalExpenses,
                        categoryBreakdowns = categoryBreakdowns,
                        budgetPerformances = budgetPerformances,
                        dailyCashFlows = dailyCashFlows,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun previousMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, _uiState.value.selectedYear)
        calendar.set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
        calendar.add(Calendar.MONTH, -1)

        _uiState.update {
            it.copy(
                selectedMonth = calendar.get(Calendar.MONTH) + 1,
                selectedYear = calendar.get(Calendar.YEAR)
            )
        }
        loadData()
    }

    fun nextMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, _uiState.value.selectedYear)
        calendar.set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
        calendar.add(Calendar.MONTH, 1)

        _uiState.update {
            it.copy(
                selectedMonth = calendar.get(Calendar.MONTH) + 1,
                selectedYear = calendar.get(Calendar.YEAR)
            )
        }
        loadData()
    }

    fun setViewType(viewType: AnalysisViewType) {
        _uiState.update { it.copy(selectedViewType = viewType) }
    }

    private fun getStartOfMonth(month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(month: Int, year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getDayTimestamp(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getCategoryColor(category: String): Int {
        return when (category.lowercase()) {
            "food" -> 0xFFFF9500.toInt()
            "transport" -> 0xFF007AFF.toInt()
            "shopping" -> 0xFFAF52DE.toInt()
            "entertainment" -> 0xFFFF2D55.toInt()
            "bills" -> 0xFFFF3B30.toInt()
            "healthcare" -> 0xFF34C759.toInt()
            else -> 0xFF8E8E93.toInt()
        }
    }
}

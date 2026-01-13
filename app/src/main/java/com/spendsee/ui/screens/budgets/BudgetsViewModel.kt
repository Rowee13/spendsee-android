package com.spendsee.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.Budget
import com.spendsee.data.local.entities.BudgetItem
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetWithDetails(
    val budget: Budget,
    val items: List<BudgetItem>,
    val spent: Double,
    val planned: Double,
    val remaining: Double,
    val percentage: Float
)

data class BudgetsUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val budgetsWithDetails: List<BudgetWithDetails> = emptyList(),
    val totalAllocated: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState: StateFlow<BudgetsUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val startOfMonth = getStartOfMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear)
                val endOfMonth = getEndOfMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear)

                combine(
                    budgetRepository.getBudgetsByMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear),
                    transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth)
                ) { budgets, transactions ->
                    val budgetsWithDetails = mutableListOf<BudgetWithDetails>()

                    budgets.forEach { budget ->
                        budgetRepository.getBudgetItems(budget.id).first().let { items ->
                            val planned = items.sumOf { it.amount }
                            val spent = transactions
                                .filter { it.budgetId == budget.id && it.type == "expense" }
                                .sumOf { it.amount }
                            val remaining = planned - spent
                            val percentage = if (planned > 0) (spent / planned * 100).toFloat() else 0f

                            budgetsWithDetails.add(
                                BudgetWithDetails(
                                    budget = budget,
                                    items = items,
                                    spent = spent,
                                    planned = planned,
                                    remaining = remaining,
                                    percentage = percentage
                                )
                            )
                        }
                    }

                    val totalAllocated = budgetsWithDetails.sumOf { it.planned }
                    val totalSpent = budgetsWithDetails.sumOf { it.spent }
                    val totalRemaining = totalAllocated - totalSpent

                    _uiState.update {
                        it.copy(
                            budgetsWithDetails = budgetsWithDetails,
                            totalAllocated = totalAllocated,
                            totalSpent = totalSpent,
                            totalRemaining = totalRemaining,
                            isLoading = false,
                            error = null
                        )
                    }
                }.catch { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }.collect()
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
        loadBudgets()
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
        loadBudgets()
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudget(budget)
                budgetRepository.deleteBudgetItemsByBudgetId(budget.id)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
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
}

package com.spendsee.ui.screens.budgets

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.Budget
import com.spendsee.data.local.entities.BudgetItem
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.managers.BudgetNotificationManager
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
    val missingBudgetsCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val context: Context
) : ViewModel() {

    private val notificationManager = BudgetNotificationManager.getInstance(context)
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

                    // Check for missing budgets from previous month
                    checkMissingBudgets()
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

    fun addBudget(
        name: String,
        category: String,
        month: Int,
        year: Int,
        isRecurring: Boolean,
        dueDate: Long?,
        notifyDaysBefore: Int
    ) {
        viewModelScope.launch {
            try {
                val budget = Budget(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    category = category,
                    month = month,
                    year = year,
                    isRecurring = isRecurring,
                    dueDate = dueDate,
                    isPaid = false,
                    paidDate = null,
                    notificationId = null,
                    notifyDaysBefore = notifyDaysBefore,
                    createdAt = System.currentTimeMillis()
                )
                budgetRepository.insertBudget(budget)

                // Schedule notification if budget has due date
                if (dueDate != null) {
                    notificationManager.scheduleBudgetNotification(budget)
                }

                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                budgetRepository.updateBudget(budget)

                // Reschedule notification if budget has due date
                // If budget is marked as paid, cancel notification
                if (budget.isPaid) {
                    budget.notificationId?.let { notificationManager.cancelNotification(it) }
                } else if (budget.dueDate != null) {
                    notificationManager.scheduleBudgetNotification(budget)
                }

                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                // Cancel notification if it exists
                budget.notificationId?.let { notificationManager.cancelNotification(it) }

                budgetRepository.deleteBudget(budget)
                budgetRepository.deleteBudgetItemsByBudgetId(budget.id)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addBudgetItem(
        budgetId: String,
        name: String,
        amount: Double,
        note: String,
        type: String
    ) {
        viewModelScope.launch {
            try {
                val budgetItem = BudgetItem(
                    id = java.util.UUID.randomUUID().toString(),
                    budgetId = budgetId,
                    name = name,
                    amount = amount,
                    note = note,
                    type = type,
                    createdAt = System.currentTimeMillis()
                )
                budgetRepository.insertBudgetItem(budgetItem)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateBudgetItem(budgetItem: BudgetItem) {
        viewModelScope.launch {
            try {
                budgetRepository.updateBudgetItem(budgetItem)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteBudgetItem(budgetItem: BudgetItem) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudgetItem(budgetItem)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun markBudgetAsPaid(budget: Budget, isPaid: Boolean) {
        viewModelScope.launch {
            try {
                val updatedBudget = budget.copy(
                    isPaid = isPaid,
                    paidDate = if (isPaid) System.currentTimeMillis() else null
                )
                budgetRepository.updateBudget(updatedBudget)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun copyFromPreviousMonth() {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, _uiState.value.selectedYear)
                calendar.set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
                calendar.add(Calendar.MONTH, -1)

                val previousMonth = calendar.get(Calendar.MONTH) + 1
                val previousYear = calendar.get(Calendar.YEAR)

                // Get budgets from previous month
                budgetRepository.getBudgetsByMonth(previousMonth, previousYear).first().let { previousBudgets ->
                    previousBudgets.forEach { previousBudget ->
                        // Create new budget for current month
                        val newBudget = Budget(
                            id = java.util.UUID.randomUUID().toString(),
                            name = previousBudget.name,
                            category = previousBudget.category,
                            month = _uiState.value.selectedMonth,
                            year = _uiState.value.selectedYear,
                            isRecurring = previousBudget.isRecurring,
                            dueDate = previousBudget.dueDate,
                            isPaid = false,
                            paidDate = null,
                            notificationId = null,
                            notifyDaysBefore = previousBudget.notifyDaysBefore,
                            createdAt = System.currentTimeMillis()
                        )
                        budgetRepository.insertBudget(newBudget)

                        // Copy budget items
                        budgetRepository.getBudgetItems(previousBudget.id).first().let { previousItems ->
                            previousItems.forEach { previousItem ->
                                val newItem = BudgetItem(
                                    id = java.util.UUID.randomUUID().toString(),
                                    budgetId = newBudget.id,
                                    name = previousItem.name,
                                    amount = previousItem.amount,
                                    note = previousItem.note,
                                    type = previousItem.type,
                                    createdAt = System.currentTimeMillis()
                                )
                                budgetRepository.insertBudgetItem(newItem)
                            }
                        }
                    }
                }

                loadBudgets()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private suspend fun checkMissingBudgets() {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, _uiState.value.selectedYear)
            calendar.set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
            calendar.add(Calendar.MONTH, -1)

            val previousMonth = calendar.get(Calendar.MONTH) + 1
            val previousYear = calendar.get(Calendar.YEAR)

            // Get budgets from previous and current month
            val previousBudgets = budgetRepository.getBudgetsByMonth(previousMonth, previousYear).first()
            val currentBudgets = _uiState.value.budgetsWithDetails.map { it.budget }

            // Find missing budgets (budgets that exist in previous month but not in current month)
            // Compare by name and category
            val missingCount = previousBudgets.count { previousBudget ->
                !currentBudgets.any { currentBudget ->
                    currentBudget.name == previousBudget.name &&
                    currentBudget.category == previousBudget.category
                }
            }

            _uiState.update { it.copy(missingBudgetsCount = missingCount) }
        } catch (e: Exception) {
            // Silent fail - missing budgets is a nice-to-have feature
        }
    }

    fun copyMissingBudgets() {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, _uiState.value.selectedYear)
                calendar.set(Calendar.MONTH, _uiState.value.selectedMonth - 1)
                calendar.add(Calendar.MONTH, -1)

                val previousMonth = calendar.get(Calendar.MONTH) + 1
                val previousYear = calendar.get(Calendar.YEAR)

                // Get budgets from previous and current month
                budgetRepository.getBudgetsByMonth(previousMonth, previousYear).first().let { previousBudgets ->
                    val currentBudgets = _uiState.value.budgetsWithDetails.map { it.budget }

                    // Find and copy only missing budgets
                    previousBudgets.forEach { previousBudget ->
                        val exists = currentBudgets.any { currentBudget ->
                            currentBudget.name == previousBudget.name &&
                            currentBudget.category == previousBudget.category
                        }

                        if (!exists) {
                            // Create new budget for current month
                            val newBudget = Budget(
                                id = java.util.UUID.randomUUID().toString(),
                                name = previousBudget.name,
                                category = previousBudget.category,
                                month = _uiState.value.selectedMonth,
                                year = _uiState.value.selectedYear,
                                isRecurring = previousBudget.isRecurring,
                                dueDate = previousBudget.dueDate,
                                isPaid = false,
                                paidDate = null,
                                notificationId = null,
                                notifyDaysBefore = previousBudget.notifyDaysBefore,
                                createdAt = System.currentTimeMillis()
                            )
                            budgetRepository.insertBudget(newBudget)

                            // Copy budget items
                            budgetRepository.getBudgetItems(previousBudget.id).first().let { previousItems ->
                                previousItems.forEach { previousItem ->
                                    val newItem = BudgetItem(
                                        id = java.util.UUID.randomUUID().toString(),
                                        budgetId = newBudget.id,
                                        name = previousItem.name,
                                        amount = previousItem.amount,
                                        note = previousItem.note,
                                        type = previousItem.type,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    budgetRepository.insertBudgetItem(newItem)
                                }
                            }
                        }
                    }
                }

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

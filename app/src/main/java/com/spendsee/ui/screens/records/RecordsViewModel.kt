package com.spendsee.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.Transaction
import com.spendsee.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class RecordsUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1, // 1-12
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val totalExpenses: Double
        get() = transactions.filter { it.type == "expense" }.sumOf { it.amount }

    val totalIncome: Double
        get() = transactions.filter { it.type == "income" }.sumOf { it.amount }

    val netTotal: Double
        get() = totalIncome - totalExpenses

    val groupedTransactions: Map<Long, List<Transaction>>
        get() {
            val calendar = Calendar.getInstance()
            return transactions.groupBy { transaction ->
                calendar.timeInMillis = transaction.date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }.toSortedMap(reverseOrder())
        }
}

class RecordsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val startOfMonth = getStartOfMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear)
                val endOfMonth = getEndOfMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear)

                transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth)
                    .catch { error ->
                        _uiState.update { it.copy(error = error.message, isLoading = false) }
                    }
                    .collect { transactions ->
                        _uiState.update { it.copy(transactions = transactions, isLoading = false, error = null) }
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
        loadTransactions()
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
        loadTransactions()
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)
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

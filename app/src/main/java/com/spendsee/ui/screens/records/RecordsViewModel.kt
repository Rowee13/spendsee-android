package com.spendsee.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.Transaction
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.data.repository.AccountRepository
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
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
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

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        notes: String,
        accountId: String?,
        toAccountId: String?
    ) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    notes = notes,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    budgetId = null,
                    createdAt = System.currentTimeMillis()
                )

                transactionRepository.insertTransaction(transaction)

                // Update account balances
                updateAccountBalances(transaction, null)

                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTransaction(
        oldTransaction: Transaction,
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        notes: String,
        accountId: String?,
        toAccountId: String?
    ) {
        viewModelScope.launch {
            try {
                val newTransaction = oldTransaction.copy(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    notes = notes,
                    accountId = accountId,
                    toAccountId = toAccountId
                )

                transactionRepository.updateTransaction(newTransaction)

                // Update account balances (reverse old, apply new)
                updateAccountBalances(newTransaction, oldTransaction)

                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(transaction)

                // Reverse the transaction's effect on account balances
                reverseAccountBalances(transaction)

                loadTransactions()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private suspend fun updateAccountBalances(newTransaction: Transaction, oldTransaction: Transaction?) {
        // If updating, first reverse the old transaction
        if (oldTransaction != null) {
            reverseAccountBalances(oldTransaction)
        }

        // Apply the new transaction
        when (newTransaction.type) {
            "income" -> {
                newTransaction.accountId?.let { accountId ->
                    accountRepository.getAccountById(accountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance + newTransaction.amount)
                        )
                    }
                }
            }
            "expense" -> {
                newTransaction.accountId?.let { accountId ->
                    accountRepository.getAccountById(accountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance - newTransaction.amount)
                        )
                    }
                }
            }
            "transfer" -> {
                // Deduct from source account
                newTransaction.accountId?.let { fromAccountId ->
                    accountRepository.getAccountById(fromAccountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance - newTransaction.amount)
                        )
                    }
                }
                // Add to destination account
                newTransaction.toAccountId?.let { toAccountId ->
                    accountRepository.getAccountById(toAccountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance + newTransaction.amount)
                        )
                    }
                }
            }
        }
    }

    private suspend fun reverseAccountBalances(transaction: Transaction) {
        when (transaction.type) {
            "income" -> {
                transaction.accountId?.let { accountId ->
                    accountRepository.getAccountById(accountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance - transaction.amount)
                        )
                    }
                }
            }
            "expense" -> {
                transaction.accountId?.let { accountId ->
                    accountRepository.getAccountById(accountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance + transaction.amount)
                        )
                    }
                }
            }
            "transfer" -> {
                // Reverse: add back to source account
                transaction.accountId?.let { fromAccountId ->
                    accountRepository.getAccountById(fromAccountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance + transaction.amount)
                        )
                    }
                }
                // Reverse: deduct from destination account
                transaction.toAccountId?.let { toAccountId ->
                    accountRepository.getAccountById(toAccountId).first()?.let { account ->
                        accountRepository.updateAccount(
                            account.copy(balance = account.balance - transaction.amount)
                        )
                    }
                }
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

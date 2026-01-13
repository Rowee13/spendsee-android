package com.spendsee.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.Account
import com.spendsee.data.repository.AccountRepository
import com.spendsee.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val startOfMonth = getStartOfMonth()
                val endOfMonth = getEndOfMonth()

                combine(
                    accountRepository.getAllAccounts(),
                    transactionRepository.getTransactionsByDateRange(startOfMonth, endOfMonth)
                ) { accounts, transactions ->
                    val totalBalance = accounts.sumOf { it.balance }
                    val totalExpenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
                    val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            accounts = accounts,
                            totalBalance = totalBalance,
                            totalExpenses = totalExpenses,
                            totalIncome = totalIncome,
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

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(account)
                loadAccounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}

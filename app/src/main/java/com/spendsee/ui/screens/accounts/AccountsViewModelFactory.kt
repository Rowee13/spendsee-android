package com.spendsee.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spendsee.data.repository.AccountRepository
import com.spendsee.data.repository.TransactionRepository

class AccountsViewModelFactory(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountsViewModel::class.java)) {
            return AccountsViewModel(accountRepository, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

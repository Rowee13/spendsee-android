package com.spendsee.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spendsee.data.repository.TransactionRepository
import com.spendsee.data.repository.AccountRepository

class RecordsViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordsViewModel::class.java)) {
            return RecordsViewModel(transactionRepository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

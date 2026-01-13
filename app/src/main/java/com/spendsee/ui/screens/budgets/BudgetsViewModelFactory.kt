package com.spendsee.ui.screens.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spendsee.data.repository.BudgetRepository
import com.spendsee.data.repository.TransactionRepository

class BudgetsViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetsViewModel::class.java)) {
            return BudgetsViewModel(budgetRepository, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

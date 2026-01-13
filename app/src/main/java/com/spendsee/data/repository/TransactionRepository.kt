package com.spendsee.data.repository

import android.content.Context
import com.spendsee.data.local.SpendSeeDatabase
import com.spendsee.data.local.dao.TransactionDao
import com.spendsee.data.local.entities.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    companion object {
        @Volatile
        private var INSTANCE: TransactionRepository? = null

        fun getInstance(context: Context? = null): TransactionRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TransactionRepository(
                    SpendSeeDatabase.getInstance(context!!).transactionDao()
                )
                INSTANCE = instance
                instance
            }
        }
    }

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllFlow()

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getByMonthFlow(startDate, endDate)

    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> =
        transactionDao.getByAccountFlow(accountId)

    fun getTransactionsByBudget(budgetId: String): Flow<List<Transaction>> =
        transactionDao.getByBudgetFlow(budgetId)

    fun getTransactionsByCategory(category: String): Flow<List<Transaction>> =
        transactionDao.getByCategoryFlow(category)

    fun getTransactionsByType(type: String): Flow<List<Transaction>> =
        transactionDao.getByTypeFlow(type)

    fun getTransactionById(id: String): Flow<Transaction?> =
        transactionDao.getByIdFlow(id)

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun getTransactionCountByMonth(startOfMonth: Long, endOfMonth: Long): Int =
        transactionDao.getCountByMonth(startOfMonth, endOfMonth)
}

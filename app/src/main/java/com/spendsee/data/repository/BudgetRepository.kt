package com.spendsee.data.repository

import com.spendsee.data.local.dao.BudgetDao
import com.spendsee.data.local.dao.BudgetItemDao
import com.spendsee.data.local.entities.Budget
import com.spendsee.data.local.entities.BudgetItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val budgetItemDao: BudgetItemDao
) {
    fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAllFlow()

    fun getBudgetsByMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getByMonthFlow(month, year)

    fun getBudgetsByCategory(category: String): Flow<List<Budget>> =
        budgetDao.getByCategoryFlow(category)

    fun getRecurringBudgets(): Flow<List<Budget>> =
        budgetDao.getRecurringFlow()

    fun getUpcomingBudgets(endDate: Long): Flow<List<Budget>> =
        budgetDao.getUpcomingFlow(endDate)

    fun getBudgetById(id: String): Flow<Budget?> =
        budgetDao.getByIdFlow(id)

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insert(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget)
    }

    suspend fun getBudgetCountByMonth(month: Int, year: Int): Int =
        budgetDao.getCountByMonth(month, year)

    // Budget Items
    fun getBudgetItems(budgetId: String): Flow<List<BudgetItem>> =
        budgetItemDao.getByBudgetFlow(budgetId)

    fun getBudgetItemsByType(budgetId: String, type: String): Flow<List<BudgetItem>> =
        budgetItemDao.getByBudgetAndTypeFlow(budgetId, type)

    fun getTotalAmountByBudget(budgetId: String): Flow<Double?> =
        budgetItemDao.getTotalAmountByBudgetFlow(budgetId)

    suspend fun insertBudgetItem(budgetItem: BudgetItem) {
        budgetItemDao.insert(budgetItem)
    }

    suspend fun updateBudgetItem(budgetItem: BudgetItem) {
        budgetItemDao.update(budgetItem)
    }

    suspend fun deleteBudgetItem(budgetItem: BudgetItem) {
        budgetItemDao.delete(budgetItem)
    }

    suspend fun deleteBudgetItemsByBudgetId(budgetId: String) {
        budgetItemDao.deleteByBudgetId(budgetId)
    }
}

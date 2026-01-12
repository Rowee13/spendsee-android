package com.spendsee.data.local.dao

import androidx.room.*
import com.spendsee.data.local.entities.BudgetItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetItemDao {
    @Query("SELECT * FROM budget_items ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<BudgetItem>>

    @Query("SELECT * FROM budget_items ORDER BY createdAt DESC")
    suspend fun getAll(): List<BudgetItem>

    @Query("SELECT * FROM budget_items WHERE id = :id")
    suspend fun getById(id: String): BudgetItem?

    @Query("SELECT * FROM budget_items WHERE id = :id")
    fun getByIdFlow(id: String): Flow<BudgetItem?>

    @Query("""
        SELECT * FROM budget_items
        WHERE budgetId = :budgetId
        ORDER BY createdAt ASC
    """)
    fun getByBudgetFlow(budgetId: String): Flow<List<BudgetItem>>

    @Query("""
        SELECT * FROM budget_items
        WHERE budgetId = :budgetId
        ORDER BY createdAt ASC
    """)
    suspend fun getByBudget(budgetId: String): List<BudgetItem>

    @Query("""
        SELECT * FROM budget_items
        WHERE budgetId = :budgetId AND type = :type
        ORDER BY createdAt ASC
    """)
    fun getByBudgetAndTypeFlow(budgetId: String, type: String): Flow<List<BudgetItem>>

    @Query("""
        SELECT SUM(amount) FROM budget_items
        WHERE budgetId = :budgetId
    """)
    suspend fun getTotalAmountByBudget(budgetId: String): Double?

    @Query("""
        SELECT SUM(amount) FROM budget_items
        WHERE budgetId = :budgetId
    """)
    fun getTotalAmountByBudgetFlow(budgetId: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budgetItem: BudgetItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgetItems: List<BudgetItem>)

    @Update
    suspend fun update(budgetItem: BudgetItem)

    @Delete
    suspend fun delete(budgetItem: BudgetItem)

    @Query("DELETE FROM budget_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM budget_items WHERE budgetId = :budgetId")
    suspend fun deleteByBudgetId(budgetId: String)

    @Query("DELETE FROM budget_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM budget_items WHERE budgetId = :budgetId")
    suspend fun getCountByBudget(budgetId: String): Int
}

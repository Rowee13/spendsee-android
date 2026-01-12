package com.spendsee.data.local.dao

import androidx.room.*
import com.spendsee.data.local.entities.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets ORDER BY createdAt DESC")
    suspend fun getAll(): List<Budget>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: String): Budget?

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Budget?>

    @Query("""
        SELECT * FROM budgets
        WHERE month = :month AND year = :year
        ORDER BY createdAt DESC
    """)
    fun getByMonthFlow(month: Int, year: Int): Flow<List<Budget>>

    @Query("""
        SELECT * FROM budgets
        WHERE month = :month AND year = :year
        ORDER BY createdAt DESC
    """)
    suspend fun getByMonth(month: Int, year: Int): List<Budget>

    @Query("""
        SELECT * FROM budgets
        WHERE category = :category
        ORDER BY createdAt DESC
    """)
    fun getByCategoryFlow(category: String): Flow<List<Budget>>

    @Query("""
        SELECT * FROM budgets
        WHERE isRecurring = 1
        ORDER BY createdAt DESC
    """)
    fun getRecurringFlow(): Flow<List<Budget>>

    @Query("""
        SELECT * FROM budgets
        WHERE isPaid = 0 AND dueDate IS NOT NULL AND dueDate <= :endDate
        ORDER BY dueDate ASC
    """)
    fun getUpcomingFlow(endDate: Long): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<Budget>)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM budgets WHERE month = :month AND year = :year")
    suspend fun getCountByMonth(month: Int, year: Int): Int
}

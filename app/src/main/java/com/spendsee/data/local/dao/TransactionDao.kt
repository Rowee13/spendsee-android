package com.spendsee.data.local.dao

import androidx.room.*
import com.spendsee.data.local.entities.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): Transaction?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Transaction?>

    @Query("""
        SELECT * FROM transactions
        WHERE date >= :startOfMonth AND date < :endOfMonth
        ORDER BY date DESC
    """)
    fun getByMonthFlow(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE date >= :startOfMonth AND date < :endOfMonth
        ORDER BY date DESC
    """)
    suspend fun getByMonth(startOfMonth: Long, endOfMonth: Long): List<Transaction>

    @Query("""
        SELECT * FROM transactions
        WHERE accountId = :accountId
        ORDER BY date DESC
    """)
    fun getByAccountFlow(accountId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE budgetId = :budgetId
        ORDER BY date DESC
    """)
    fun getByBudgetFlow(budgetId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions
        WHERE category = :category
        ORDER BY date DESC
    """)
    fun getByCategoryFlow(category: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByTypeFlow(type: String): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getCountByMonth(startOfMonth: Long, endOfMonth: Long): Int
}

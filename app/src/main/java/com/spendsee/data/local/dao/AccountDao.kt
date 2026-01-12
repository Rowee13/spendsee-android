package com.spendsee.data.local.dao

import androidx.room.*
import com.spendsee.data.local.entities.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    fun getAllFlow(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    suspend fun getAll(): List<Account>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): Account?

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Account?>

    @Query("SELECT * FROM accounts WHERE type = :type ORDER BY createdAt ASC")
    fun getByTypeFlow(type: String): Flow<List<Account>>

    @Query("SELECT SUM(balance) FROM accounts")
    suspend fun getTotalBalance(): Double?

    @Query("SELECT SUM(balance) FROM accounts")
    fun getTotalBalanceFlow(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()

    @Query("""
        UPDATE accounts
        SET balance = balance + :amount
        WHERE id = :accountId
    """)
    suspend fun updateBalance(accountId: String, amount: Double)
}

package com.spendsee.data.repository

import android.content.Context
import com.spendsee.data.local.SpendSeeDatabase
import com.spendsee.data.local.dao.AccountDao
import com.spendsee.data.local.entities.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
    companion object {
        @Volatile
        private var INSTANCE: AccountRepository? = null

        fun getInstance(context: Context? = null): AccountRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AccountRepository(
                    SpendSeeDatabase.getInstance(context!!).accountDao()
                )
                INSTANCE = instance
                instance
            }
        }
    }
    fun getAllAccounts(): Flow<List<Account>> =
        accountDao.getAllFlow()

    fun getAccountsByType(type: String): Flow<List<Account>> =
        accountDao.getByTypeFlow(type)

    fun getAccountById(id: String): Flow<Account?> =
        accountDao.getByIdFlow(id)

    fun getTotalBalance(): Flow<Double?> =
        accountDao.getTotalBalanceFlow()

    suspend fun insertAccount(account: Account) {
        accountDao.insert(account)
    }

    suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }

    suspend fun deleteAccount(account: Account) {
        accountDao.delete(account)
    }

    suspend fun updateAccountBalance(accountId: String, amount: Double) {
        accountDao.updateBalance(accountId, amount)
    }

    suspend fun getAccountCount(): Int =
        accountDao.getCount()
}

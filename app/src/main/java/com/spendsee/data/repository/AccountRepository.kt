package com.spendsee.data.repository

import com.spendsee.data.local.dao.AccountDao
import com.spendsee.data.local.entities.Account
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao
) {
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

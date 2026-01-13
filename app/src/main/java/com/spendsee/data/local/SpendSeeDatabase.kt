package com.spendsee.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendsee.data.local.dao.*
import com.spendsee.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        Account::class,
        Budget::class,
        BudgetItem::class,
        Category::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpendSeeDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun budgetItemDao(): BudgetItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: SpendSeeDatabase? = null

        fun getDatabase(context: Context): SpendSeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSeeDatabase::class.java,
                    "spendsee_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): SpendSeeDatabase = getDatabase(context)
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        suspend fun seedDatabase(database: SpendSeeDatabase) {
            val categoryDao = database.categoryDao()

            // Check if categories already exist
            val existingCount = categoryDao.getCount()
            if (existingCount == 0) {
                // Insert all default categories from DefaultCategories object
                categoryDao.insertAll(DefaultCategories.getAllDefaultCategories())
            }
        }
    }
}

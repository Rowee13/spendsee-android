package com.spendsee.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
        Category::class,
        AppNotification::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SpendSeeDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun budgetItemDao(): BudgetItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun appNotificationDao(): AppNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: SpendSeeDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create notifications table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        type TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        readAt INTEGER,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        actionType TEXT,
                        relatedBudgetId TEXT
                    )
                """.trimIndent())

                // Create indexes for performance
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_notifications_createdAt
                    ON notifications(createdAt DESC)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_notifications_isRead
                    ON notifications(isRead)
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): SpendSeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSeeDatabase::class.java,
                    "spendsee_database"
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_1_2)
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

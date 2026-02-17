package com.spendsee.managers

import android.content.Context
import android.net.Uri
import com.spendsee.data.local.SpendSeeDatabase
import com.spendsee.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(
    private val context: Context,
    private val database: SpendSeeDatabase
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportBackup(): Pair<String, String> = withContext(Dispatchers.IO) {
        // Fetch all data
        val transactions = database.transactionDao().getAll()
        val accounts = database.accountDao().getAll()
        val budgets = database.budgetDao().getAll()
        val budgetItems = database.budgetItemDao().getAll()
        val categories = database.categoryDao().getAll()

        // Get settings
        val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val selectedCurrency = prefs.getString("selected_currency_code", "USD") ?: "USD"

        // Create backup data structure
        val backup = BackupData(
            metadata = BackupMetadata(
                exportDate = System.currentTimeMillis(),
                appVersion = "1.0.0",
                platform = "android"
            ),
            transactions = transactions,
            accounts = accounts,
            budgets = budgets,
            budgetItems = budgetItems,
            categories = categories, // Include all categories so customizations are preserved on restore
            settings = BackupSettings(selectedCurrency = selectedCurrency)
        )

        // Serialize to JSON
        val jsonContent = json.encodeToString(backup)

        // Generate filename
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US)
        val filename = "SpendSee_Backup_${dateFormat.format(Date())}.json"

        return@withContext Pair(filename, jsonContent)
    }

    suspend fun importBackup(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val result = ImportResult()

        try {
            // Read JSON from URI
            val jsonContent = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Cannot read file")

            // Deserialize from JSON
            val backup = json.decodeFromString<BackupData>(jsonContent)

            // Import categories — update in-place if same name exists (avoids duplicates
            // when restoring to a fresh install that already seeded default categories)
            backup.categories.forEach { category ->
                try {
                    val existing = database.categoryDao().getByName(category.name)
                    if (existing != null) {
                        // A category with this name already exists — update its customizable
                        // fields while preserving the local UUID and isDefault status
                        database.categoryDao().update(
                            existing.copy(
                                icon = category.icon,
                                colorHex = category.colorHex,
                                sortOrder = category.sortOrder
                            )
                        )
                    } else {
                        database.categoryDao().insert(category)
                    }
                    result.categoriesImported++
                } catch (e: Exception) {
                    result.categoriesSkipped++
                }
            }

            // Import accounts
            backup.accounts.forEach { account ->
                try {
                    database.accountDao().insert(account)
                    result.accountsImported++
                } catch (e: Exception) {
                    result.errors.add("Failed to import account: ${account.name}")
                }
            }

            // Import budgets
            backup.budgets.forEach { budget ->
                try {
                    database.budgetDao().insert(budget)
                    result.budgetsImported++
                } catch (e: Exception) {
                    result.errors.add("Failed to import budget: ${budget.name}")
                }
            }

            // Import budget items
            backup.budgetItems.forEach { item ->
                try {
                    database.budgetItemDao().insert(item)
                    result.budgetItemsImported++
                } catch (e: Exception) {
                    // Skip if parent budget doesn't exist
                }
            }

            // Import transactions
            backup.transactions.forEach { transaction ->
                try {
                    database.transactionDao().insert(transaction)
                    result.transactionsImported++
                } catch (e: Exception) {
                    result.errors.add("Failed to import transaction: ${transaction.title}")
                }
            }

            // Import settings
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            prefs.edit().putString("selected_currency_code", backup.settings.selectedCurrency).apply()

            result.success = true

        } catch (e: Exception) {
            result.success = false
            result.errors.add("Import failed: ${e.message}")
        }

        return@withContext result
    }

    companion object {
        @Volatile
        private var instance: BackupManager? = null

        fun getInstance(context: Context): BackupManager {
            return instance ?: synchronized(this) {
                instance ?: BackupManager(
                    context.applicationContext,
                    SpendSeeDatabase.getInstance(context.applicationContext)
                ).also { instance = it }
            }
        }
    }
}

@Serializable
data class BackupData(
    val metadata: BackupMetadata,
    val transactions: List<Transaction>,
    val accounts: List<Account>,
    val budgets: List<Budget>,
    val budgetItems: List<BudgetItem>,
    val categories: List<Category>,
    val settings: BackupSettings
)

@Serializable
data class BackupMetadata(
    val exportDate: Long,
    val appVersion: String,
    val platform: String
)

@Serializable
data class BackupSettings(
    val selectedCurrency: String
)

data class ImportResult(
    var success: Boolean = false,
    var transactionsImported: Int = 0,
    var accountsImported: Int = 0,
    var budgetsImported: Int = 0,
    var budgetItemsImported: Int = 0,
    var categoriesImported: Int = 0,
    var categoriesSkipped: Int = 0,
    var errors: MutableList<String> = mutableListOf()
)

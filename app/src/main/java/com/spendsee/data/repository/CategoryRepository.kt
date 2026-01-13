package com.spendsee.data.repository

import android.content.Context
import com.spendsee.data.local.SpendSeeDatabase
import com.spendsee.data.local.dao.CategoryDao
import com.spendsee.data.local.entities.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllFlow()

    fun getCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getByTypeFlow(type)

    fun getDefaultCategories(): Flow<List<Category>> =
        categoryDao.getDefaultFlow()

    fun getCustomCategories(): Flow<List<Category>> =
        categoryDao.getCustomFlow()

    fun getCategoryById(id: String): Flow<Category?> =
        categoryDao.getByIdFlow(id)

    suspend fun getCategoryByName(name: String): Category? =
        categoryDao.getByName(name)

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun deleteAllCustomCategories() {
        categoryDao.deleteAllCustom()
    }

    suspend fun getCustomCategoryCount(): Int =
        categoryDao.getCustomCount()

    companion object {
        @Volatile
        private var INSTANCE: CategoryRepository? = null

        fun getInstance(context: Context? = null): CategoryRepository {
            return INSTANCE ?: synchronized(this) {
                val db = SpendSeeDatabase.getInstance(context!!)
                val instance = CategoryRepository(db.categoryDao())
                INSTANCE = instance
                instance
            }
        }
    }
}

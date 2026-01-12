package com.spendsee.data.local.dao

import androidx.room.*
import com.spendsee.data.local.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllFlow(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAll(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): Category?

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getByIdFlow(id: String): Flow<Category?>

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getByName(name: String): Category?

    @Query("""
        SELECT * FROM categories
        WHERE type = :type
        ORDER BY sortOrder ASC, createdAt ASC
    """)
    fun getByTypeFlow(type: String): Flow<List<Category>>

    @Query("""
        SELECT * FROM categories
        WHERE type = :type
        ORDER BY sortOrder ASC, createdAt ASC
    """)
    suspend fun getByType(type: String): List<Category>

    @Query("""
        SELECT * FROM categories
        WHERE isDefault = 1
        ORDER BY sortOrder ASC, createdAt ASC
    """)
    fun getDefaultFlow(): Flow<List<Category>>

    @Query("""
        SELECT * FROM categories
        WHERE isDefault = 1
        ORDER BY sortOrder ASC, createdAt ASC
    """)
    suspend fun getDefault(): List<Category>

    @Query("""
        SELECT * FROM categories
        WHERE isDefault = 0
        ORDER BY sortOrder ASC, createdAt ASC
    """)
    fun getCustomFlow(): Flow<List<Category>>

    @Query("""
        SELECT * FROM categories
        WHERE isDefault = 0
        ORDER BY sortOrder ASC, createdAt ASC
    """)
    suspend fun getCustom(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM categories WHERE isDefault = 0")
    suspend fun deleteAllCustom()

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM categories WHERE type = :type")
    suspend fun getCountByType(type: String): Int

    @Query("SELECT COUNT(*) FROM categories WHERE isDefault = 0")
    suspend fun getCustomCount(): Int
}

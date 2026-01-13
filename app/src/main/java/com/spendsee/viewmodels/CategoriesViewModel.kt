package com.spendsee.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spendsee.data.local.entities.Category
import com.spendsee.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CategoryRepository.getInstance(application)

    val allCategories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> = repository.getCategoriesByType("income")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> = repository.getCategoriesByType("expense")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addCategory(
        name: String,
        icon: String,
        colorHex: String,
        type: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Check if category with same name exists
                val existing = repository.getCategoryByName(name)
                if (existing != null) {
                    _errorMessage.value = "Category with this name already exists"
                    return@launch
                }

                val category = Category(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    icon = icon,
                    colorHex = colorHex,
                    type = type,
                    isDefault = false,
                    sortOrder = 999,
                    createdAt = System.currentTimeMillis()
                )

                repository.insertCategory(category)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                repository.updateCategory(category)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // Don't allow deleting default categories
                if (category.isDefault) {
                    _errorMessage.value = "Cannot delete default categories"
                    return@launch
                }

                repository.deleteCategory(category)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

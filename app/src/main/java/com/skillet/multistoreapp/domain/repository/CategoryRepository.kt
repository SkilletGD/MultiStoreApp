package com.skillet.multistoreapp.domain.repository

import com.skillet.multistoreapp.core.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun createCategory(
        category: Category
    ): Result<Unit>

    fun getCategoriesByStoreFlow(
        storeId: String
    ): Flow<List<Category>>

    suspend fun updateCategory(
        category: Category
    ): Result<Unit>

    suspend fun deleteCategory(
        categoryId: String
    ): Result<Unit>

    suspend fun getCategoriesById(
        categoryId: String
    ): Result<Category?>
}
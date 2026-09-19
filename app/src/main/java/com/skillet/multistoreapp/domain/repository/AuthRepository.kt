package com.skillet.multistoreapp.domain.repository

import androidx.compose.ui.semantics.Role
import com.skillet.multistoreapp.core.model.AppUser
import com.skillet.multistoreapp.core.model.UserRole

interface AuthRepository {

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<AppUser>

    suspend fun Login(
        email: String,
        password: String
    ): Result<AppUser>

    suspend fun getCurrentUser(): AppUser?

    fun logOut()

    suspend fun getUserById(userId: String): Result<AppUser>
}
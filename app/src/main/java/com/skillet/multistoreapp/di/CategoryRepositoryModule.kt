package com.skillet.multistoreapp.di

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.data.repository.CategoryRepositoryImpl
import com.skillet.multistoreapp.domain.repository.CategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CategoryRepositoryModule {
    @Provides
    @Singleton
    fun provideCategoryRepository(
        firestore: FirebaseFirestore
    ): CategoryRepository {
        return CategoryRepositoryImpl(firestore)
    }

}
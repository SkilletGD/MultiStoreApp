package com.skillet.multistoreapp.di

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.data.repository.StoreRepositoryImpl
import com.skillet.multistoreapp.domain.repository.StoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object StoreRepositoryModule {
    @Provides
    @Singleton
    fun provideStoreRepository(
        firestore: FirebaseFirestore
    ): StoreRepository = StoreRepositoryImpl(firestore)
}
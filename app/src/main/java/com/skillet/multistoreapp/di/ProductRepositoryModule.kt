package com.skillet.multistoreapp.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skillet.multistoreapp.data.repository.ProductRepositoryImpl
import com.skillet.multistoreapp.data.repository.ProductStorageRepositoryImpl
import com.skillet.multistoreapp.domain.repository.ProductRepository
import com.skillet.multistoreapp.domain.repository.ProductStorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProductRepositoryModule {
    @Provides
    @Singleton
    fun provideProductStorageRepository(
        storage: FirebaseStorage
    ): ProductStorageRepository =
        ProductStorageRepositoryImpl(
            storage = storage
        )

    @Provides
    @Singleton
    fun provideProductFirestoreRepository(
        firestore: FirebaseFirestore
    ): ProductRepository = ProductRepositoryImpl(
        firestore = firestore
    )
}
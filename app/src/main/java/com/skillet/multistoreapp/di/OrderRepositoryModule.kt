package com.skillet.multistoreapp.di

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.data.repository.OrderRepositoryImpl
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.OrderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OrderRepositoryModule {
    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): OrderRepository {
        return OrderRepositoryImpl(
            firestore = firestore,
            authRepository = authRepository
        )
    }

}
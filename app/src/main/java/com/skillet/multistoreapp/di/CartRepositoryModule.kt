package com.skillet.multistoreapp.di

import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.data.repository.CartRepositoryImpl
import com.skillet.multistoreapp.domain.repository.AuthRepository
import com.skillet.multistoreapp.domain.repository.CartRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CartRepositoryModule {
    @Provides
    @Singleton
    fun provideCartRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): CartRepository {
        return CartRepositoryImpl(
            firestore = firestore,
            authRepository = authRepository
        )
    }

}
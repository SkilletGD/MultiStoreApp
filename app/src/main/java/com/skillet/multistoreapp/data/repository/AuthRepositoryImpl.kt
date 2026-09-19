package com.skillet.multistoreapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skillet.multistoreapp.core.model.AppUser
import com.skillet.multistoreapp.core.model.UserRole
import com.skillet.multistoreapp.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
): AuthRepository {

    private val collectionUsers: String = "users"

    private fun usersRef() =
        firestore.collection(collectionUsers)

    override suspend fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<AppUser> {
        return runCatching {
            val firstNameTrimed: String = firstName.trim()
            val lastNameTrimed: String = lastName.trim()
            val emailTrimed: String = email.trim()
            val phoneTrimed: String = phone.trim()

            if (firstNameTrimed.isEmpty()) {
                throw IllegalArgumentException("El nombre no puede estar vacío")
            }
            if (lastNameTrimed.isEmpty()) {
                throw IllegalArgumentException("El apellido no puede estar vacío")
            }
            if (emailTrimed.isEmpty()) {
                throw IllegalArgumentException("El correo no puede estar vacío")
            }
            if (password.isEmpty()) {
                throw IllegalArgumentException("La contraseña no puede estar vacía")
            }

            val createdAt: Long = System.currentTimeMillis()

            val authResult = auth
                .createUserWithEmailAndPassword(
                    emailTrimed,
                    password
                ).await()

            val firebaseUser = authResult.user
                ?: throw IllegalStateException("No se puedo obtener el usuario registrado de Firebase")

            val finalUser = AppUser(
                uid = firebaseUser.uid,
                firstName = firstNameTrimed,
                lastName = lastNameTrimed,
                email = emailTrimed,
                phone = phoneTrimed,
                role = role.name,
                createdAt = createdAt
            )

            val data = mapOf(
                "uid" to finalUser.uid,
                "firstName" to finalUser.firstName,
                "lastName" to finalUser.lastName,
                "email" to finalUser.email,
                "phone" to finalUser.phone,
                "role" to finalUser.role,
                "createdAt" to finalUser.createdAt
            )

            usersRef()
                .document(finalUser.uid)
                .set(data)
                .await()

            finalUser

        }
    }

    override suspend fun Login(
        email: String,
        password: String
    ): Result<AppUser>{
        return runCatching {
            val emailTrimed: String = email.trim()

            if (emailTrimed.isEmpty()) {
                throw IllegalArgumentException("El correo no puede estar vacío")
            }
            if (password.isEmpty()) {
                throw IllegalArgumentException("La contraseña no puede estar vacía")
            }

            auth.signInWithEmailAndPassword(
                emailTrimed,
                password
            ).await()

            val currentUser = auth.currentUser
                ?: throw IllegalStateException("No se pudo obtener el usuario actual")

            val snapshot = usersRef()
                .document(currentUser.uid)
                .get()
                .await()

            val appUser: AppUser = snapshot
                .toObject(AppUser::class.java)
                ?: throw IllegalStateException("No se pudo obtener la información del usuario")

            appUser
        }
    }

    override suspend fun getCurrentUser(): AppUser? {
        return runCatching {
            val currentUser = auth.currentUser ?: return null

            val snapshot = usersRef()
                .document(currentUser.uid)
                .get()
                .await()

            snapshot.toObject(AppUser::class.java)
        }.getOrNull()
    }

    override fun logOut() {
        auth.signOut()
    }

    override suspend fun getUserById(userId: String): Result<AppUser> =
        runCatching {
            val userIdTrimmed = userId.trim()

            if(userIdTrimmed.isEmpty()) throw IllegalArgumentException("El id del usuario no puede estar vacío")

            val snapshot = usersRef().document(userIdTrimmed).get().await()

            if(!snapshot.exists()) throw Exception("No se encontro la informacion del usuario")

            snapshot.toObject(AppUser::class.java) ?: throw Exception("No se pudo convertir la informacion del usuario")

        }
}
package com.skillet.multistoreapp.data.repository

import com.google.firebase.storage.FirebaseStorage
import com.skillet.multistoreapp.domain.repository.ProductStorageRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductStorageRepositoryImpl(
    private val storage: FirebaseStorage
): ProductStorageRepository {
    private fun fileRef(path: String) =
        storage.reference.child(path)


    override suspend fun uploadProductImage(
        sellerId: String?,
        storeId: String,
        byteArray: ByteArray,
        extension: String
    ): Result<Pair<String, String>> =
        runCatching {
            val cleanExtension = extension.trim().removePrefix(".")

            val imageUid = UUID.randomUUID().toString().take(8)

            val path = "products/$sellerId/$storeId/$imageUid.$cleanExtension"

            val ref = fileRef(path)

            ref.putBytes(byteArray).await()

            val imageUrl = ref.downloadUrl.await().toString()

            imageUrl to path

        }

    override suspend fun deleteProductImage(storagePath: String): Result<Unit> =
        runCatching {
            val ref = fileRef(storagePath)
            ref.delete().await()

            Unit
        }


    override suspend fun updateProductImage(
        sellerId: String,
        oldStoragePath: String,
        newByteArray: ByteArray,
        extension: String
    ): Result<Pair<String, String>> =
        runCatching {
            val cleanExtension = extension.trim().removePrefix(".")

            val newImageUid = UUID.randomUUID().toString().take(8)

            val newPath = "products/$sellerId/$newImageUid.$cleanExtension"

            val newRef = fileRef(newPath)

            newRef.putBytes(newByteArray).await()

            val newImageUrl = newRef.downloadUrl.await().toString()

            if(oldStoragePath.isBlank()){
                return@runCatching newImageUrl to newPath
            }

            deleteProductImage(oldStoragePath).getOrThrow()
            newImageUrl to newPath

        }
}
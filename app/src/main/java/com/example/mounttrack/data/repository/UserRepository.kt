package com.example.mounttrack.data.repository

import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseHelper.firestore

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = firestore.collection(FirebaseHelper.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            val user = document.toObject(User::class.java)
            user?.let {
                Result.success(it.copy(uid = userId))
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection(FirebaseHelper.COLLECTION_USERS)
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


package com.example.mounttrack.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = currentUser?.uid

    fun isLoggedIn(): Boolean = currentUser != null

    // Collection references
    const val COLLECTION_USERS = "users"
    const val COLLECTION_MOUNTAINS = "mountains"
    const val COLLECTION_REGISTRATIONS = "registrations"
    const val COLLECTION_NEWS = "news"
}


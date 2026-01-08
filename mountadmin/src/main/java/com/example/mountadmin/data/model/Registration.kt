package com.example.mountadmin.data.model

import com.google.firebase.Timestamp

data class Registration(
    val registrationId: String = "",
    val userId: String = "",
    val mountainId: String = "",
    val mountainName: String = "",
    val route: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
    }
}


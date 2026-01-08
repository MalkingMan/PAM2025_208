package com.example.mounttrack.data.model

import com.google.firebase.Timestamp

data class Registration(
    val registrationId: String = "",
    val userId: String = "",
    val mountainId: String = "",
    val mountainName: String = "",
    val routeId: String = "",  // NEW: Route ID for capacity tracking
    val route: String = "",    // Route name for display
    val fullName: String = "",
    val dob: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val idCardUri: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}


package com.example.mountadmin.data.model

import com.google.firebase.Timestamp

data class Admin(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ROLE_MOUNTAIN_ADMIN,
    val assignedMountainId: String? = null,
    val assignedMountainName: String? = null,
    val status: String = STATUS_ACTIVE,
    val createdAt: Timestamp? = null,
    val avatarUrl: String = ""
) {
    companion object {
        const val ROLE_SUPER_ADMIN = "SUPER_ADMIN"
        const val ROLE_MOUNTAIN_ADMIN = "MOUNTAIN_ADMIN"
        const val ROLE_NEWS_ADMIN = "NEWS_ADMIN"
        
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_DISABLED = "DISABLED"
    }
    
    fun getRoleDisplayName(): String {
        return when (role) {
            ROLE_SUPER_ADMIN -> "Super Admin"
            ROLE_MOUNTAIN_ADMIN -> "Mountain Admin"
            ROLE_NEWS_ADMIN -> "News Admin"
            else -> role
        }
    }
    
    fun isActive(): Boolean = status == STATUS_ACTIVE
    
    fun isSuperAdmin(): Boolean = role == ROLE_SUPER_ADMIN
    
    fun isMountainAdmin(): Boolean = role == ROLE_MOUNTAIN_ADMIN
    
    fun isNewsAdmin(): Boolean = role == ROLE_NEWS_ADMIN
}


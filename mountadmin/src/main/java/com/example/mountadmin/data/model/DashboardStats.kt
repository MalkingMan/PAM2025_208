package com.example.mountadmin.data.model

data class DashboardStats(
    val totalHikers: Long = 0,
    val registeredThisMonth: Long = 0,
    val mostClimbedMountain: String = "",
    val mountainRegistrations: List<MountainRegistrationStat> = emptyList()
)

data class MountainRegistrationStat(
    val mountainName: String = "",
    val count: Int = 0
)


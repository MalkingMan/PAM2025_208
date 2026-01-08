package com.example.mountadmin.data.model

import com.google.firebase.Timestamp

data class Mountain(
    val mountainId: String = "",
    val name: String = "",
    val province: String = "",
    val country: String = "Indonesia",
    val elevation: Int = 0,
    val description: String = "",
    val imageUrl: String = "",
    val routes: List<HikingRoute> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
) {
    // Alias for id
    val id: String
        get() = mountainId

    // For Firestore location field compatibility
    val location: String
        get() = "$province, $country"

    // Alias for height
    val height: Int
        get() = elevation
}

data class HikingRoute(
    val routeId: String = "",
    val name: String = "",
    val difficulty: String = DIFFICULTY_MODERATE,
    val estimatedTime: String = "",
    val distance: String = ""
) {
    companion object {
        const val DIFFICULTY_EASY = "Easy"
        const val DIFFICULTY_MODERATE = "Moderate"
        const val DIFFICULTY_DIFFICULT = "Difficult"
        const val DIFFICULTY_EXPERT = "Expert"
    }
}


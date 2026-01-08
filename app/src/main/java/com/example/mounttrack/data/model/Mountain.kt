package com.example.mounttrack.data.model

data class Mountain(
    val mountainId: String = "",
    val name: String = "",
    val province: String = "",
    val country: String = "Indonesia",
    val elevation: Int = 0,
    val description: String = "",
    val imageUrl: String = "",
    val routes: List<Map<String, Any>> = emptyList(),
    val isActive: Boolean = true
) {
    // Computed property for location display
    val location: String
        get() = if (province.isNotEmpty()) "$province, $country" else country

    // Computed property for height (alias for elevation)
    val height: Int
        get() = elevation

    // Get route names as list of strings for display
    fun getRouteNames(): List<String> {
        return routes.mapNotNull { route ->
            route["name"] as? String
        }
    }

    // Get routes as HikingRoute objects
    fun getHikingRoutes(): List<HikingRoute> {
        return routes.map { route ->
            HikingRoute(
                routeId = route["routeId"] as? String ?: "",
                name = route["name"] as? String ?: "",
                difficulty = route["difficulty"] as? String ?: "Moderate",
                estimatedTime = route["estimatedTime"] as? String ?: "",
                distance = route["distance"] as? String ?: "",
                maxCapacity = (route["maxCapacity"] as? Long)?.toInt() ?: 0,
                usedCapacity = (route["usedCapacity"] as? Long)?.toInt() ?: 0,
                status = route["status"] as? String ?: HikingRoute.STATUS_OPEN
            )
        }
    }
}

data class HikingRoute(
    val routeId: String = "",
    val name: String = "",
    val difficulty: String = "Moderate",
    val estimatedTime: String = "",
    val distance: String = "",
    // Capacity fields - NEW
    val maxCapacity: Int = 0,
    val usedCapacity: Int = 0,
    val status: String = STATUS_OPEN
) {
    // Computed remaining capacity
    val remainingCapacity: Int
        get() = maxOf(0, maxCapacity - usedCapacity)

    // Check if route is available for registration
    val isAvailable: Boolean
        get() = status == STATUS_OPEN && remainingCapacity > 0

    // Check if route is full
    val isFull: Boolean
        get() = remainingCapacity <= 0

    companion object {
        const val DIFFICULTY_EASY = "Easy"
        const val DIFFICULTY_MODERATE = "Moderate"
        const val DIFFICULTY_HARD = "Hard"
        const val DIFFICULTY_DIFFICULT = "Difficult"
        const val DIFFICULTY_EXPERT = "Expert"

        const val STATUS_OPEN = "open"
        const val STATUS_CLOSED = "closed"
    }

    // Override toString to display only route name in AutoCompleteTextView
    override fun toString(): String {
        return name
    }
}


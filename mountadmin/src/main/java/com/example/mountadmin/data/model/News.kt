package com.example.mountadmin.data.model

import com.google.firebase.Timestamp

data class News(
    val newsId: String = "",
    val title: String = "",
    val category: String = "",
    val content: String = "",
    val tags: String = "",
    val coverImageUrl: String = "",
    val status: String = STATUS_DRAFT,
    val published: Boolean = false,
    val isFeatured: Boolean = false,
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // Alias for id
    val id: String
        get() = newsId

    companion object {
        const val STATUS_PUBLISHED = "PUBLISHED"
        const val STATUS_DRAFT = "DRAFT"

        val CATEGORIES = listOf(
            "Safety",
            "Trail Update",
            "Event",
            "Weather"
        )
    }
}


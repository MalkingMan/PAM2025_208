package com.example.mounttrack.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class HikingNews(
    @DocumentId
    val documentId: String = "",
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    @get:PropertyName("newsId") @set:PropertyName("newsId")
    var newsId: String = "",
    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",
    @get:PropertyName("category") @set:PropertyName("category")
    var category: String = "",
    @get:PropertyName("content") @set:PropertyName("content")
    var content: String = "",
    @get:PropertyName("tags") @set:PropertyName("tags")
    var tags: String = "",
    @get:PropertyName("coverImageUrl") @set:PropertyName("coverImageUrl")
    var coverImageUrl: String = "",
    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "",
    @get:PropertyName("published") @set:PropertyName("published")
    var published: Boolean = false,
    @get:PropertyName("isFeatured") @set:PropertyName("isFeatured")
    var isFeatured: Boolean = false,
    @get:PropertyName("authorId") @set:PropertyName("authorId")
    var authorId: String = "",
    @get:PropertyName("authorName") @set:PropertyName("authorName")
    var authorName: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Timestamp? = null,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Timestamp? = null
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", false, false, "", "", null, null)

    // Get actual ID (support both id, newsId, and documentId fields)
    val actualId: String
        get() = when {
            id.isNotEmpty() -> id
            newsId.isNotEmpty() -> newsId
            else -> documentId
        }

    val description: String
        get() = if (content.length > 100) {
            content.take(100) + "..."
        } else {
            content
        }

    val imageUrl: String
        get() = coverImageUrl
}


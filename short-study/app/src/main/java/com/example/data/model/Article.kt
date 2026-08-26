package com.example.data.model

data class Article(
    val id: String,
    val title: String,
    val content: String,
    val summary: String,
    val author: String = "Short-Study Team",
    val publishedDate: String,
    val updatedDate: String = "",
    val url: String,
    val labels: List<String> = emptyList(),
    val category: Category = Category.ALL,
    val isBookmarked: Boolean = false,
    val readTimeMinutes: Int = 4,
    val readingProgressPercent: Int = 0,
    val isFeatured: Boolean = false
)

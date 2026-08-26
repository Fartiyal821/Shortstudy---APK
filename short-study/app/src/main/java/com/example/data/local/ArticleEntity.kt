package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Article
import com.example.data.model.Category

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val summary: String,
    val author: String,
    val publishedDate: String,
    val updatedDate: String,
    val url: String,
    val labelsCsv: String,
    val categorySlug: String,
    val isBookmarked: Boolean,
    val readTimeMinutes: Int,
    val readingProgressPercent: Int,
    val isFeatured: Boolean,
    val cachedTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): Article {
        val labelList = if (labelsCsv.isBlank()) emptyList() else labelsCsv.split(",")
        return Article(
            id = id,
            title = title,
            content = content,
            summary = summary,
            author = author,
            publishedDate = publishedDate,
            updatedDate = updatedDate,
            url = url,
            labels = labelList,
            category = Category.fromSlug(categorySlug),
            isBookmarked = isBookmarked,
            readTimeMinutes = readTimeMinutes,
            readingProgressPercent = readingProgressPercent,
            isFeatured = isFeatured
        )
    }

    companion object {
        fun fromDomain(article: Article): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                title = article.title,
                content = article.content,
                summary = article.summary,
                author = article.author,
                publishedDate = article.publishedDate,
                updatedDate = article.updatedDate,
                url = article.url,
                labelsCsv = article.labels.joinToString(","),
                categorySlug = article.category.slug,
                isBookmarked = article.isBookmarked,
                readTimeMinutes = article.readTimeMinutes,
                readingProgressPercent = article.readingProgressPercent,
                isFeatured = article.isFeatured
            )
        }
    }
}

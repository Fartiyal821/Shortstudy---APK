package com.example.data.repository

import android.util.Log
import com.example.data.local.ArticleDao
import com.example.data.local.ArticleEntity
import com.example.data.local.PreferencesManager
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.remote.BloggerApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

interface ArticleRepository {
    fun getAllArticles(): Flow<List<Article>>
    fun getFeaturedArticles(): Flow<List<Article>>
    fun getArticlesByCategory(category: Category): Flow<List<Article>>
    fun getBookmarkedArticles(): Flow<List<Article>>
    fun getHostedArticles(): Flow<List<Article>>
    fun getArticleById(id: String): Flow<Article?>
    fun searchArticles(query: String): Flow<List<Article>>
    suspend fun toggleBookmark(id: String, isBookmarked: Boolean)
    suspend fun updateReadingProgress(id: String, progress: Int)
    suspend fun publishArticle(article: Article): Result<Article>
    suspend fun deleteArticle(id: String): Result<Unit>
    suspend fun refreshFromBlogger(apiKey: String, blogId: String? = null): Result<Int>
    suspend fun initializeDatabaseIfEmpty()
}

class ArticleRepositoryImpl(
    private val articleDao: ArticleDao,
    private val bloggerApiService: BloggerApiService,
    private val preferencesManager: PreferencesManager
) : ArticleRepository {

    override fun getAllArticles(): Flow<List<Article>> {
        return articleDao.getAllArticles().map { list -> list.map { it.toDomain() } }
    }

    override fun getFeaturedArticles(): Flow<List<Article>> {
        return articleDao.getFeaturedArticles().map { list -> list.map { it.toDomain() } }
    }

    override fun getArticlesByCategory(category: Category): Flow<List<Article>> {
        return if (category == Category.ALL) {
            getAllArticles()
        } else {
            articleDao.getArticlesByCategory(category.slug).map { list -> list.map { it.toDomain() } }
        }
    }

    override fun getBookmarkedArticles(): Flow<List<Article>> {
        return articleDao.getBookmarkedArticles().map { list -> list.map { it.toDomain() } }
    }

    override fun getHostedArticles(): Flow<List<Article>> {
        return articleDao.getHostedArticles().map { list -> list.map { it.toDomain() } }
    }

    override fun getArticleById(id: String): Flow<Article?> {
        return articleDao.getArticleById(id).map { it?.toDomain() }
    }

    override fun searchArticles(query: String): Flow<List<Article>> {
        return articleDao.searchArticles(query.trim()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun publishArticle(article: Article): Result<Article> {
        return withContext(Dispatchers.IO) {
            try {
                val entity = ArticleEntity.fromDomain(article)
                articleDao.insertArticle(entity)
                Result.success(article)
            } catch (e: Exception) {
                Log.e("ArticleRepo", "Failed to publish hosted article", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteArticle(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                articleDao.deleteArticleById(id)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("ArticleRepo", "Failed to delete hosted article", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun toggleBookmark(id: String, isBookmarked: Boolean) {
        withContext(Dispatchers.IO) {
            articleDao.updateBookmarkStatus(id, isBookmarked)
        }
    }

    override suspend fun updateReadingProgress(id: String, progress: Int) {
        withContext(Dispatchers.IO) {
            articleDao.updateReadingProgress(id, progress.coerceIn(0, 100))
        }
    }

    override suspend fun initializeDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = articleDao.getArticleCount()
            if (count == 0) {
                val seedArticles = SampleCurriculum.getInitialArticles().map { ArticleEntity.fromDomain(it) }
                articleDao.insertArticles(seedArticles)
            }
        }
    }

    override suspend fun refreshFromBlogger(apiKey: String, blogId: String?): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(Exception("Blogger API Key is not set"))
                }

                // 1. Resolve Blog ID if not provided
                val targetBlogId = if (!blogId.isNullOrBlank()) {
                    blogId
                } else {
                    val blogResp = bloggerApiService.getBlogByUrl(
                        url = "https://shortstudy999.blogspot.com/",
                        apiKey = apiKey
                    )
                    if (blogResp.isSuccessful && blogResp.body() != null) {
                        blogResp.body()!!.id
                    } else {
                        return@withContext Result.failure(Exception("Failed to fetch blog metadata: ${blogResp.message()}"))
                    }
                }

                // 2. Fetch posts
                val postsResp = bloggerApiService.getPosts(
                    blogId = targetBlogId,
                    apiKey = apiKey,
                    maxResults = 30
                )

                if (!postsResp.isSuccessful || postsResp.body()?.items == null) {
                    return@withContext Result.failure(Exception("Failed to fetch posts: ${postsResp.message()}"))
                }

                val items = postsResp.body()!!.items!!
                val newEntities = items.map { dto ->
                    val existing = articleDao.getArticleByIdDirect(dto.id)
                    val rawContent = dto.content ?: ""
                    val snippet = extractSummary(rawContent)
                    val labels = dto.labels ?: emptyList()
                    val cat = if (labels.isNotEmpty()) Category.fromLabel(labels.first()) else Category.ALL

                    ArticleEntity(
                        id = dto.id,
                        title = dto.title,
                        content = rawContent,
                        summary = snippet,
                        author = dto.author?.displayName ?: "Short-Study",
                        publishedDate = dto.published?.take(10) ?: "",
                        updatedDate = dto.updated?.take(10) ?: "",
                        url = dto.url ?: "https://shortstudy999.blogspot.com/",
                        labelsCsv = labels.joinToString(","),
                        categorySlug = cat.slug,
                        isBookmarked = existing?.isBookmarked ?: false,
                        readTimeMinutes = calculateReadTime(rawContent),
                        readingProgressPercent = existing?.readingProgressPercent ?: 0,
                        isFeatured = false
                    )
                }

                articleDao.insertArticles(newEntities)
                Result.success(newEntities.size)
            } catch (e: Exception) {
                Log.e("ArticleRepo", "Blogger sync error", e)
                Result.failure(e)
            }
        }
    }

    private fun extractSummary(html: String): String {
        val noHtml = html.replace(Regex("<[^>]*>"), " ").trim()
        val condensed = noHtml.replace(Regex("\\s+"), " ")
        return if (condensed.length > 140) condensed.take(137) + "..." else condensed
    }

    private fun calculateReadTime(text: String): Int {
        val wordCount = text.split(Regex("\\s+")).size
        return (wordCount / 180).coerceAtLeast(2)
    }
}

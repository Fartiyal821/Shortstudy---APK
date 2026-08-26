package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backend.BackendServerStatus
import com.example.data.backend.HostBackendService
import com.example.data.backend.HostLessonTemplate
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HostFormState(
    val title: String = "",
    val category: Category = Category.PYTHON,
    val author: String = "Short-Study Host",
    val summary: String = "",
    val content: String = "",
    val isFeatured: Boolean = false,
    val selectedTab: Int = 0, // 0: Editor, 1: Live Preview, 2: Hosted Posts, 3: Backend Status
    val isPublishing: Boolean = false,
    val statusMessage: String? = null,
    val editingId: String? = null
)

class HostViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(HostFormState())
    val formState: StateFlow<HostFormState> = _formState.asStateFlow()

    val hostedArticles: StateFlow<List<Article>> = repository.getHostedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _serverStatus = MutableStateFlow(HostBackendService.getServerStatus(0))
    val serverStatus: StateFlow<BackendServerStatus> = _serverStatus.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _formState.update { it.copy(selectedTab = tabIndex) }
    }

    fun updateTitle(title: String) {
        _formState.update { it.copy(title = title) }
    }

    fun updateCategory(category: Category) {
        _formState.update { it.copy(category = category) }
    }

    fun updateAuthor(author: String) {
        _formState.update { it.copy(author = author) }
    }

    fun updateSummary(summary: String) {
        _formState.update { it.copy(summary = summary) }
    }

    fun updateContent(content: String) {
        _formState.update { it.copy(content = content) }
    }

    fun updateFeatured(isFeatured: Boolean) {
        _formState.update { it.copy(isFeatured = isFeatured) }
    }

    fun insertFormattingTag(tag: String) {
        val current = _formState.value.content
        val snippet = when (tag) {
            "h3" -> "\n<h3>Section Heading</h3>\n"
            "p" -> "\n<p>Enter your explanation paragraph here.</p>\n"
            "quote" -> "\n<blockquote><strong>Pro Tip:</strong> Important coding insight or note.</blockquote>\n"
            "li" -> "\n<li>Key concept or syntax rule</li>\n"
            else -> "\n<p>$tag</p>\n"
        }
        _formState.update { it.copy(content = current + snippet) }
    }

    fun insertCodeBlock(language: String) {
        val current = _formState.value.content
        val codeSnippet = when (language) {
            "python" -> "\n<pre><code class=\"language-python\">def solve_problem(data: list) -> dict:\n    # Process data and return structured result\n    result = {item: len(str(item)) for item in data}\n    return result\n</code></pre>\n"
            "c" -> "\n<pre><code class=\"language-c\">#include <stdio.h>\n\nint main() {\n    printf(\"Short-Study C Lesson Output\\n\");\n    return 0;\n}\n</code></pre>\n"
            "javascript" -> "\n<pre><code class=\"language-javascript\">async function fetchLessonData() {\n    const res = await fetch('/api/v1/posts');\n    const data = await res.json();\n    console.log(data);\n}\n</code></pre>\n"
            "html" -> "\n<pre><code class=\"language-html\"><div class=\"container\">\n    <h2>Short-Study Modern Component</h2>\n</div>\n</code></pre>\n"
            "css" -> "\n<pre><code class=\"language-css\">.card {\n    display: flex;\n    background: #1e293b;\n    border-radius: 12px;\n}\n</code></pre>\n"
            else -> "\n<pre><code class=\"language-$language\">// Code snippet here\n</code></pre>\n"
        }
        _formState.update { it.copy(content = current + codeSnippet) }
    }

    fun loadTemplate(template: HostLessonTemplate) {
        _formState.update {
            it.copy(
                title = template.title,
                category = template.category,
                summary = template.summary,
                content = template.content,
                statusMessage = "Loaded template: ${template.title}"
            )
        }
    }

    fun editArticle(article: Article) {
        _formState.update {
            it.copy(
                editingId = article.id,
                title = article.title,
                category = article.category,
                author = article.author,
                summary = article.summary,
                content = article.content,
                isFeatured = article.isFeatured,
                selectedTab = 0,
                statusMessage = "Editing '${article.title}'"
            )
        }
    }

    fun publishLesson(onSuccess: ((String) -> Unit)? = null) {
        val state = _formState.value
        if (state.title.isBlank()) {
            _formState.update { it.copy(statusMessage = "Error: Please enter an article title") }
            return
        }
        if (state.content.isBlank()) {
            _formState.update { it.copy(statusMessage = "Error: Content cannot be empty") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isPublishing = true, statusMessage = null) }

            val now = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val calculatedReadTime = (state.content.split(Regex("\\s+")).size / 150).coerceAtLeast(2)
            val articleId = state.editingId ?: HostBackendService.generateUniquePostId()
            val cleanSummary = if (state.summary.isNotBlank()) state.summary else {
                val plain = state.content.replace(Regex("<[^>]*>"), " ").trim()
                if (plain.length > 120) plain.take(117) + "..." else plain
            }

            val article = Article(
                id = articleId,
                title = state.title.trim(),
                content = state.content.trim(),
                summary = cleanSummary,
                author = state.author.ifBlank { "Short-Study Host" },
                publishedDate = now,
                updatedDate = now,
                url = "https://shortstudy-host.internal/posts/$articleId",
                labels = listOf(state.category.displayName, "Hosted", "Tutorial"),
                category = state.category,
                isBookmarked = false,
                readTimeMinutes = calculatedReadTime,
                readingProgressPercent = 0,
                isFeatured = state.isFeatured
            )

            val result = repository.publishArticle(article)
            if (result.isSuccess) {
                _formState.update {
                    it.copy(
                        isPublishing = false,
                        statusMessage = "Successfully published to Host Backend!",
                        title = "",
                        summary = "",
                        content = "",
                        editingId = null,
                        selectedTab = 2 // Move to Hosted Posts list
                    )
                }
                _serverStatus.value = HostBackendService.getServerStatus(hostedArticles.value.size + 1)
                onSuccess?.invoke(articleId)
            } else {
                _formState.update {
                    it.copy(
                        isPublishing = false,
                        statusMessage = "Failed to publish: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun deleteArticle(id: String) {
        viewModelScope.launch {
            val result = repository.deleteArticle(id)
            if (result.isSuccess) {
                _formState.update { it.copy(statusMessage = "Post deleted from Host Backend.") }
                _serverStatus.value = HostBackendService.getServerStatus((hostedArticles.value.size - 1).coerceAtLeast(0))
            } else {
                _formState.update { it.copy(statusMessage = "Error deleting post.") }
            }
        }
    }

    fun resetForm() {
        _formState.update {
            it.copy(
                editingId = null,
                title = "",
                summary = "",
                content = "",
                isFeatured = false,
                statusMessage = "Form reset."
            )
        }
    }

    fun clearStatusMessage() {
        _formState.update { it.copy(statusMessage = null) }
    }

    class Factory(
        private val repository: ArticleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HostViewModel(repository) as T
        }
    }
}

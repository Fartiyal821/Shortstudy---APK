package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.repository.ArticleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedCategory: Category = Category.ALL,
    val isRefreshing: Boolean = false,
    val syncMessage: String? = null
)

class HomeViewModel(
    private val repository: ArticleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }
    }

    val featuredArticles: StateFlow<List<Article>> = repository.getFeaturedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: StateFlow<List<Article>> = _uiState
        .flatMapLatest { state ->
            repository.getArticlesByCategory(state.selectedCategory)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article.id, !article.isBookmarked)
        }
    }

    fun refreshFromBlogger(apiKey: String, blogId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, syncMessage = null) }
            val result = repository.refreshFromBlogger(apiKey, blogId)
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    syncMessage = if (result.isSuccess) {
                        "Synced ${result.getOrNull()} articles from Blogger!"
                    } else {
                        "Sync failed: ${result.exceptionOrNull()?.message}"
                    }
                )
            }
        }
    }

    fun clearSyncMessage() {
        _uiState.update { it.copy(syncMessage = null) }
    }

    class Factory(
        private val repository: ArticleRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository, preferencesManager) as T
        }
    }
}

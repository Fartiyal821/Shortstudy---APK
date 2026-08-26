package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.local.ReaderFontFamily
import com.example.data.local.ReaderPreferences
import com.example.data.model.Article
import com.example.data.repository.ArticleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val repository: ArticleRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _articleId = MutableStateFlow<String?>(null)

    val readerPreferences: StateFlow<ReaderPreferences> = preferencesManager.readerPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReaderPreferences()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentArticle: StateFlow<Article?> = _articleId
        .filterNotNull()
        .flatMapLatest { id -> repository.getArticleById(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadArticle(id: String) {
        _articleId.value = id
    }

    fun toggleBookmark() {
        val article = currentArticle.value ?: return
        viewModelScope.launch {
            repository.toggleBookmark(article.id, !article.isBookmarked)
        }
    }

    fun updateReadingProgress(progress: Int) {
        val article = currentArticle.value ?: return
        viewModelScope.launch {
            repository.updateReadingProgress(article.id, progress)
        }
    }

    fun updateFontSize(scale: Float) {
        viewModelScope.launch {
            preferencesManager.updateFontSizeScale(scale)
        }
    }

    fun updateFontFamily(fontFamily: ReaderFontFamily) {
        viewModelScope.launch {
            preferencesManager.updateFontFamily(fontFamily)
        }
    }

    fun toggleLineNumbers(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.toggleLineNumbers(enabled)
        }
    }

    class Factory(
        private val repository: ArticleRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(repository, preferencesManager) as T
        }
    }
}

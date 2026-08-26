package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppThemeMode
import com.example.data.local.PreferencesManager
import com.example.data.local.ReaderPreferences
import com.example.data.repository.ArticleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val repository: ArticleRepository
) : ViewModel() {

    val readerPreferences: StateFlow<ReaderPreferences> = preferencesManager.readerPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReaderPreferences()
        )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesManager.updateThemeMode(mode)
        }
    }

    fun updateBloggerApiKey(key: String) {
        viewModelScope.launch {
            preferencesManager.updateBloggerApiKey(key)
        }
    }

    fun resetCurriculum() {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }
    }

    class Factory(
        private val preferencesManager: PreferencesManager,
        private val repository: ArticleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesManager, repository) as T
        }
    }
}

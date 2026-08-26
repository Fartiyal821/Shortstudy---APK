package com.example

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.local.AppThemeMode
import com.example.data.local.PreferencesManager
import com.example.data.remote.NetworkClient
import com.example.data.repository.ArticleRepositoryImpl
import com.example.ui.screens.bookmarks.BookmarksScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.host.HostScreen
import com.example.ui.screens.reader.ArticleReaderScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.webview.BloggerWebViewScreen
import com.example.ui.theme.ShortStudyTheme
import com.example.ui.viewmodel.BookmarksViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.HostViewModel
import com.example.ui.viewmodel.ReaderViewModel
import com.example.ui.viewmodel.SettingsViewModel

object Destinations {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val READER = "reader/{articleId}"
    const val BOOKMARKS = "bookmarks"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val HOST = "host"
    const val WEBVIEW = "webview/{encodedUrl}"

    fun readerRoute(articleId: String) = "reader/$articleId"
    fun webViewRoute(url: String) = "webview/${Uri.encode(url)}"
}

@Composable
fun ShortStudyApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val database = remember { AppDatabase.getInstance(context) }
    val preferencesManager = remember { PreferencesManager(context) }
    val repository = remember {
        ArticleRepositoryImpl(
            articleDao = database.articleDao(),
            bloggerApiService = NetworkClient.bloggerService,
            preferencesManager = preferencesManager
        )
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(repository, preferencesManager)
    )
    val readerViewModel: ReaderViewModel = viewModel(
        factory = ReaderViewModel.Factory(repository, preferencesManager)
    )
    val bookmarksViewModel: BookmarksViewModel = viewModel(
        factory = BookmarksViewModel.Factory(repository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(preferencesManager, repository)
    )
    val hostViewModel: HostViewModel = viewModel(
        factory = HostViewModel.Factory(repository)
    )

    val readerPrefs by settingsViewModel.readerPreferences.collectAsStateWithLifecycle()
    val isDarkTheme = when (readerPrefs.appThemeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    ShortStudyTheme(darkTheme = isDarkTheme) {
        NavHost(
            navController = navController,
            startDestination = Destinations.SPLASH,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Destinations.SPLASH) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Destinations.HOME) {
                            popUpTo(Destinations.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Destinations.HOME) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onArticleClick = { article ->
                        navController.navigate(Destinations.readerRoute(article.id))
                    },
                    onSearchClick = {
                        navController.navigate(Destinations.SEARCH)
                    },
                    onBookmarksClick = {
                        navController.navigate(Destinations.BOOKMARKS)
                    },
                    onSettingsClick = {
                        navController.navigate(Destinations.SETTINGS)
                    },
                    onHostClick = {
                        navController.navigate(Destinations.HOST)
                    }
                )
            }

            composable(
                route = Destinations.READER,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                ArticleReaderScreen(
                    articleId = articleId,
                    viewModel = readerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onOpenWebView = { url ->
                        navController.navigate(Destinations.webViewRoute(url))
                    }
                )
            }

            composable(Destinations.BOOKMARKS) {
                BookmarksScreen(
                    viewModel = bookmarksViewModel,
                    onArticleClick = { article ->
                        navController.navigate(Destinations.readerRoute(article.id))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Destinations.SEARCH) {
                SearchScreen(
                    repository = repository,
                    onArticleClick = { article ->
                        navController.navigate(Destinations.readerRoute(article.id))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onHostClick = { navController.navigate(Destinations.HOST) }
                )
            }

            composable(Destinations.HOST) {
                HostScreen(
                    viewModel = hostViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToArticle = { articleId ->
                        navController.navigate(Destinations.readerRoute(articleId))
                    }
                )
            }

            composable(
                route = Destinations.WEBVIEW,
                arguments = listOf(navArgument("encodedUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
                val decodedUrl = Uri.decode(encodedUrl)
                BloggerWebViewScreen(
                    url = decodedUrl,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

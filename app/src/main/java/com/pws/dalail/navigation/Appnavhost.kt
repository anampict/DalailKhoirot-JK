package com.pws.dalail.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pws.dalail.screens.BookmarkScreen
import com.pws.dalail.screens.ChapterDetailScreen
import com.pws.dalail.screens.DaftarIsiScreen
import com.pws.dalail.screens.DashboardScreen
import com.pws.dalail.screens.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: AppScreen = AppScreen.Splash
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination.route
    ) {

        // ── Splash ───────────────────────────────────────────────────────────
        composable(AppScreen.Splash.route) {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(AppScreen.Dashboard.route) {
                        popUpTo(AppScreen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Dashboard ────────────────────────────────────────────────────────
        composable(AppScreen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        // ── Daftar Isi ───────────────────────────────────────────────────────
        composable(AppScreen.Daftarisi.route) {
            DaftarIsiScreen(
                navController    = navController,
                onChapterClick   = { chapter ->
                    // Klik bab → masuk ke detail, bottom bar hilang
                    navController.navigate(
                        AppScreen.ChapterDetail.createRoute(chapter.number)
                    )
                }
            )
        }

        // ── Bookmark ─────────────────────────────────────────────────────────
        composable(AppScreen.Bookmark.route) {
            BookmarkScreen(navController = navController)
        }

        // ── Chapter Detail (tanpa bottom bar) ────────────────────────────────
        composable(
            route     = AppScreen.ChapterDetail.route,
            arguments = listOf(
                navArgument(AppScreen.ChapterDetail.ARG) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val chapterNumber = backStackEntry.arguments
                ?.getInt(AppScreen.ChapterDetail.ARG) ?: 1
            ChapterDetailScreen(
                chapterNumber = chapterNumber,
                navController = navController
            )
        }
    }
}
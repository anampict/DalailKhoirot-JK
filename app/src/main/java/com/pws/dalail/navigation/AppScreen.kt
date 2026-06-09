package com.pws.dalail.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pws.dalail.R


sealed class AppScreen(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val route: String
) {
    object Splash : AppScreen(
        R.string.screen_splash,
        R.drawable.ic_launcher_background,
        "splash"
    )

    object Dashboard : AppScreen(
        R.string.screen_beranda,
        R.drawable.ic_launcher_background,
        "dashboard"
    )

    object Daftarisi : AppScreen(
        R.string.screen_daftarisi,
        R.drawable.ic_launcher_background,
        "daftarisi"
    )

    // Route chapter detail: chapterdetail/{chapterNumber}
    object ChapterDetail : AppScreen(
        R.string.screen_daftarisi,
        R.drawable.ic_launcher_background,
        "chapterdetail/{chapterNumber}"
    ) {
        const val ARG = "chapterNumber"
        fun createRoute(chapterNumber: Int) = "chapterdetail/$chapterNumber"
    }
}

package com.pws.dalail.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pws.dalail.R
import com.pws.dalail.commond.jakartasans
import com.pws.dalail.navigation.AppScreen

// ─── Data Model ───────────────────────────────────────────────────────────────

data class Chapter(
    val number: Int,
    val titleArabic: String,
    val page: Int
)

val chapterList = listOf(
    Chapter(1,  "كَيْفِيَّةُ قِرَاءَةِ دَلَائِلِ الْخَيْرَاتِ", 6),
    Chapter(2,  "الأَسْمَاءُ الْحُسْنَى", 8),
    Chapter(3,  "يُقْرَأُ قَبْلَ الشُّرُوعِ", 13),
    Chapter(4,  "يَبْدَأُ الْمُصَلِّي", 15),
    Chapter(5,  "دَلَائِلُ الْخَيْرَاتِ", 17),
    Chapter(6,  "أَسْمَاءُ سَيِّدِنَا وَمَوْلَانَا مُحَمَّدٍ ﷺ", 20),
    Chapter(7,  "الْحِزْبُ الْأَوَّلُ فِي يَوْمِ الْاِثْنَيْنِ", 32),
    Chapter(8,  "الْحِزْبُ الثَّانِي فِي يَوْمِ الثَّلَاثَاءِ", 48),
    Chapter(9,  "الْحِزْبُ الثَّالِثُ فِي يَوْمِ الْأَرْبِعَاءِ", 64),
    Chapter(10, "الْحِزْبُ الرَّابِعُ فِي يَوْمِ الْخَمِيسِ", 81),
    Chapter(11, "الْحِزْبُ الْخَامِسُ فِي يَوْمِ الْجُمُعَةِ", 98),
    Chapter(12, "الْحِزْبُ السَّادِسُ فِي يَوْمِ السَّبْتِ", 118),
    Chapter(13, "الْحِزْبُ السَّابِعُ فِي يَوْمِ الْأَحَدِ", 137),
    Chapter(14, "الْحِزْبُ الثَّامِنُ فِي يَوْمِ الْاِثْنَيْنِ", 157),
    Chapter(15, "الدُّعَاءُ عَقِبَ خَتْمِ دَلَائِلِ الْخَيْرَاتِ", 166),
    Chapter(16, "فِهْرِسٌ", 171)
)

// ─── Colors ───────────────────────────────────────────────────────────────────

private val GreenPrimary      = Color(0xFF1A6B45)
private val GreenLight        = Color(0xFFE8F5EE)
private val GreenText         = Color(0xFF1A6B45)
private val TextSecondary     = Color(0xFF8E8E93)

// ─── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarIsiScreen(
    navController: NavController = rememberNavController(),
    onChapterClick: (Chapter) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredChapters = remember(searchQuery) {
        if (searchQuery.isBlank()) chapterList
        else chapterList.filter {
            it.titleArabic.contains(searchQuery, ignoreCase = true) ||
                    it.number.toString().contains(searchQuery) ||
                    it.page.toString().contains(searchQuery)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Item bottom nav — sama persis seperti di Dashboard
    val bottomNavItems = listOf(
        BottomNavItem("Beranda",    R.drawable.beranda,   AppScreen.Dashboard.route),
        BottomNavItem("Daftar Isi", R.drawable.daftarisi, AppScreen.Daftarisi.route)
    )

    Scaffold(
        topBar = {
            DaftarIsiTopBar(
                onBackClick = { navController.popBackStack() }
            )
        },
        // ── Bottom bar tampil di DaftarIsiScreen ─────────────────────────────
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (item.route != currentRoute) {
                                navController.navigate(item.route) {
                                    popUpTo(AppScreen.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter            = painterResource(id = item.iconRes),
                                contentDescription = item.label,
                                modifier           = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text       = item.label,
                                fontSize   = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                fontFamily = jakartasans
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = GreenPrimary,
                            selectedTextColor   = GreenPrimary,
                            indicatorColor      = GreenLight,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query         = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Daftar bab — klik bab navigasi ke detail (tanpa bottom bar)
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredChapters, key = { it.number }) { chapter ->
                    ChapterItem(
                        chapter = chapter,
                        onClick = { onChapterClick(chapter) }
                    )
                }
            }
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarIsiTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Daftar Isi",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF00352E),
                fontFamily = jakartasans
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.height(50.dp),
        placeholder = {
            Text(
                text     = "Cari bab...",
                fontSize = 14.sp,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        leadingIcon = {
            Icon(
                painter            = painterResource(id = android.R.drawable.ic_menu_search),
                contentDescription = "Cari",
                tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier           = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        shape      = RoundedCornerShape(50),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = GreenPrimary,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}

// ─── Chapter Item ─────────────────────────────────────────────────────────────

@Composable
fun ChapterItem(
    chapter: Chapter,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Number badge
            Box(
                modifier         = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = chapter.number.toString(),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color      = GreenText
                )
            }

            // Arabic title
            Text(
                text       = chapter.titleArabic,
                modifier   = Modifier.weight(1f),
                fontSize   = 15.sp,
                textAlign  = TextAlign.End,
                lineHeight = 22.sp,
                color      = MaterialTheme.colorScheme.onBackground,
                style      = LocalTextStyle.current.copy(
                    textDirection = TextDirection.Rtl
                )
            )

            // Page number + chevron
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text     = chapter.page.toString(),
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier           = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DaftarIsiScreenPreview() {
    MaterialTheme {
        DaftarIsiScreen()
    }
}
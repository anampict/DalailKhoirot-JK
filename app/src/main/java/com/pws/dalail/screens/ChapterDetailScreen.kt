package com.pws.dalail.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.pws.dalail.commond.jakartasans

// ─── Colors ───────────────────────────────────────────────────────────────────

private val ChapterGreen     = Color(0xFF1A6B45)
private val ChapterGreenLight = Color(0xFFE8F5EE)

// ─── Main Screen — tanpa Bottom Bar ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapterNumber: Int,
    navController: NavController = rememberNavController()
) {
    // Ambil data chapter berdasarkan nomor
    val chapter = chapterList.find { it.number == chapterNumber }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Bab ${chapter?.number ?: ""}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF00352E),
                            fontFamily = jakartasans
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        // ⬇ Tidak ada bottomBar di halaman detail bab
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (chapter == null) {
            // Fallback jika chapter tidak ditemukan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bab tidak ditemukan",
                    color = Color.Gray,
                    fontFamily = jakartasans
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = 20.dp,
                    vertical   = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header bab ───────────────────────────────────────────────
                item {
                    ChapterDetailHeader(chapter = chapter)
                }

                // ── Placeholder konten bab ───────────────────────────────────
                item {
                    ChapterContentPlaceholder(chapter = chapter)
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
fun ChapterDetailHeader(chapter: Chapter) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = ChapterGreenLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Badge nomor bab
            Box(
                modifier = Modifier
                    .background(ChapterGreen, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = "Bab ${chapter.number}",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                    fontFamily = jakartasans
                )
            }

            // Judul Arab
            Text(
                text       = chapter.titleArabic,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                lineHeight = 36.sp,
                color      = ChapterGreen,
                style      = LocalTextStyle.current.copy(
                    textDirection = TextDirection.Rtl
                )
            )

            // Info halaman
            Text(
                text       = "Mulai halaman ${chapter.page}",
                fontSize   = 13.sp,
                color      = ChapterGreen.copy(alpha = 0.7f),
                fontFamily = jakartasans
            )
        }
    }
}

// ─── Placeholder Konten ───────────────────────────────────────────────────────

@Composable
fun ChapterContentPlaceholder(chapter: Chapter) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text       = "Konten Bab",
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = ChapterGreen,
                fontFamily = jakartasans
            )
            HorizontalDivider(color = ChapterGreenLight, thickness = 1.dp)
            Text(
                text       = "Konten halaman ${chapter.page} akan ditampilkan di sini.",
                fontSize   = 14.sp,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontFamily = jakartasans,
                lineHeight = 22.sp
            )
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChapterDetailScreenPreview() {
    MaterialTheme {
        ChapterDetailScreen(chapterNumber = 7)
    }
}

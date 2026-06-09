package com.pws.dalail.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ─── Mapping chapter number → rentang halaman PDF ─────────────────────────────

private data class ChapterPageRange(
    val number    : Int,
    val startPage : Int,
    val endPage   : Int
)

private val chapterPageRanges = listOf(
    ChapterPageRange(1,   6,   7),
    ChapterPageRange(2,   8,  12),
    ChapterPageRange(3,  13,  14),
    ChapterPageRange(4,  15,  16),
    ChapterPageRange(5,  17,  19),
    ChapterPageRange(6,  20,  31),
    ChapterPageRange(7,  32,  47),
    ChapterPageRange(8,  48,  63),
    ChapterPageRange(9,  64,  80),
    ChapterPageRange(10, 81,  97),
    ChapterPageRange(11, 98, 117),
    ChapterPageRange(12,118, 136),
    ChapterPageRange(13,137, 156),
    ChapterPageRange(14,157, 165),
    ChapterPageRange(15,166, 170),
    ChapterPageRange(16,171, 172)
)

// ─── ChapterDetailScreen ──────────────────────────────────────────────────────
//
// Bridge antara DaftarIsi/Bookmark → PdfReaderScreen.
// Meneruskan startPage, endPage, DAN chapterNumber ke PdfReaderScreen
// agar bookmark & terakhir-dibaca bisa disimpan ke Room dengan benar.

@Composable
fun ChapterDetailScreen(
    chapterNumber: Int,
    navController: NavController = rememberNavController()
) {
    val chapter   = chapterList.find { it.number == chapterNumber }
    val pageRange = chapterPageRanges.find { it.number == chapterNumber }

    if (chapter == null || pageRange == null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            title = { androidx.compose.material3.Text("Bab tidak ditemukan") },
            text  = { androidx.compose.material3.Text("Chapter #$chapterNumber tidak ada dalam daftar.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    androidx.compose.material3.Text("Kembali")
                }
            }
        )
        return
    }

    PdfReaderScreen(
        startPage     = pageRange.startPage,
        endPage       = pageRange.endPage,
        chapterTitle  = chapter.titleArabic,
        chapterNumber = chapter.number,          // ← diteruskan ke Room
        totalPdfPages = 172,
        navController = navController
    )
}

package com.pws.dalail.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pws.dalail.commond.jakartasans
import com.pws.dalail.commond.uthmantaha
import com.pws.dalail.database.BookmarkEntity
import com.pws.dalail.navigation.AppScreen
import com.pws.dalail.pdf.BookmarkViewModel

private val BmGreen      = Color(0xFF1A6B45)
private val BmGreenDark  = Color(0xFF0A4D44)
private val BmGreenLight = Color(0xFFE8F5EE)
private val BmGreenMuted = Color(0xFF82BDB1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    navController: NavController,
    bookmarkVm   : BookmarkViewModel = viewModel()
) {
    val bookmarks by bookmarkVm.allBookmarks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, bookmarks) {
        if (searchQuery.isBlank()) bookmarks
        else bookmarks.filter { it.titleArabic.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = "Bookmark",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = jakartasans,
                        color      = BmGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint               = BmGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                modifier      = Modifier.fillMaxWidth().height(52.dp),
                placeholder = {
                    Text("Cari bookmark...", fontSize = 14.sp, fontFamily = jakartasans, color = Color(0xFF9E9E9E))
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = BmGreen, modifier = Modifier.size(20.dp))
                },
                singleLine = true,
                shape      = RoundedCornerShape(14.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = BmGreen,
                    unfocusedBorderColor    = Color(0xFFE8E8E8),
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (bookmarks.isNotEmpty()) {
                Text(
                    text       = "${filtered.size} bab tersimpan",
                    fontSize   = 12.sp,
                    color      = Color(0xFF9E9E9E),
                    fontFamily = jakartasans,
                    modifier   = Modifier.padding(bottom = 8.dp)
                )
            }

            when {
                bookmarks.isEmpty() -> BookmarkEmptyState()
                filtered.isEmpty()  -> {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "Tidak ada hasil untuk \"$searchQuery\"",
                            fontSize   = 14.sp,
                            color      = Color(0xFF9E9E9E),
                            fontFamily = jakartasans,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        LazyColumn {
                            itemsIndexed(items = filtered, key = { _, b -> b.chapterNumber }) { index, bookmark ->
                                BookmarkItemRow(
                                    bookmark = bookmark,
                                    isFirst  = index == 0,
                                    onClick  = {
                                        navController.navigate(AppScreen.ChapterDetail.createRoute(bookmark.chapterNumber))
                                    },
                                    onDelete = { bookmarkVm.removeBookmark(bookmark.chapterNumber) }
                                )
                                if (index < filtered.lastIndex) {
                                    HorizontalDivider(
                                        modifier  = Modifier.padding(horizontal = 16.dp),
                                        color     = Color(0xFFF0F0F0),
                                        thickness = 0.8.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkItemRow(
    bookmark : BookmarkEntity,
    isFirst  : Boolean,
    onClick  : () -> Unit,
    onDelete : () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val iconBg   by animateColorAsState(if (isFirst) BmGreenDark else BmGreenLight, tween(300), "bg")
    val iconTint by animateColorAsState(if (isFirst) BmGreenMuted else BmGreen, tween(300), "tint")

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Bookmark, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = bookmark.titleArabic,
                fontSize  = 15.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = uthmantaha,
                textAlign = TextAlign.End,
                color     = Color(0xFF1C1C1E),
                style     = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                modifier  = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text       = "Hal. ${bookmark.startPage} – ${bookmark.endPage}",
                fontSize   = 11.sp,
                color      = Color(0xFF9E9E9E),
                fontFamily = jakartasans
            )
        }
        IconButton(onClick = { showDialog = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.BookmarkRemove, "Hapus", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(18.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Hapus Bookmark", fontFamily = jakartasans, fontWeight = FontWeight.SemiBold) },
            text  = { Text("Yakin ingin menghapus bookmark bab ini?", fontFamily = jakartasans, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onDelete() }) {
                    Text("Hapus", color = Color(0xFFE53935), fontFamily = jakartasans, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", fontFamily = jakartasans)
                }
            }
        )
    }
}

@Composable
private fun BookmarkEmptyState() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier         = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)).background(BmGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Bookmark, null, tint = BmGreen, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Belum ada bookmark", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E), fontFamily = jakartasans)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text       = "Tekan ikon bookmark saat membaca\nbab untuk menyimpannya di sini.",
            fontSize   = 13.sp,
            color      = Color(0xFF9E9E9E),
            fontFamily = jakartasans,
            textAlign  = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BookmarkScreenPreview() {
    MaterialTheme { BookmarkScreen(navController = rememberNavController()) }
}
package com.pws.dalail.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pws.dalail.commond.jakartasans

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onChapterClick: (Chapter) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // dummy data, ganti dengan data bookmark yang sesungguhnya
    val bookmarks = remember {
        listOf(
            Chapter(1, "كَيْفِيَّةُ قِرَاءَةِ دَلَائِلِ الْخَيْرَاتِ", 6),
            Chapter(2, "الأسماء الحسنى", 8),
            Chapter(3, "يُقْرَأُ قَبْلَ الشُّرُوعِ", 13),
        )
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) bookmarks
        else bookmarks.filter {
            it.titleArabic.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text       = "Bookmark",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = jakartasans,
                        color      = Color(0xff00352E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint               = Color(0xff00352E)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                placeholder   = {
                    Text(
                        text       = "Cari...",
                        fontSize   = 14.sp,
                        fontFamily = jakartasans,
                        color      = Color(0xff00352E)
                    )
                },
                leadingIcon   = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint               = Color(0xff00352E),
                        modifier           = Modifier.size(20.dp)
                    )
                },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color(0xff00352E),
                    unfocusedBorderColor = Color(0xFFE8E8E8),
                    focusedContainerColor   = Color(0xFFF7F7F7),
                    unfocusedContainerColor = Color(0xFFF7F7F7)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bookmark List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(filtered, key = { it.number }) { chapter ->
                    BookmarkItem(
                        chapter  = chapter,
                        isFirst  = filtered.indexOf(chapter) == 0,
                        onClick  = { onChapterClick(chapter) }
                    )
                    if (filtered.indexOf(chapter) < filtered.size - 1) {
                        HorizontalDivider(
                            color     = Color(0xFFF0F0F0),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkItem(
    chapter : Chapter,
    isFirst : Boolean = false,
    onClick : () -> Unit
) {
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bookmark Icon
        Box(
            modifier        = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isFirst) Color(0xff0A4D44) else Color(0xFFF0F0F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Bookmark,
                contentDescription = null,
                tint               = if (isFirst) Color(0xff82BDB1) else Color(0xff00352E),
                modifier           = Modifier.size(18.dp)
            )
        }

        // Arabic Title
        Text(
            text      = chapter.titleArabic,
            modifier  = Modifier.weight(1f),
            fontSize  = 15.sp,
            textAlign = TextAlign.End,
            color     = Color(0xff191C1D),
            style     = LocalTextStyle.current.copy(
                textDirection = TextDirection.Rtl
            )
        )

        // Chevron
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = Color(0xff191C1D),
            modifier           = Modifier.size(20.dp)
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BookmarkScreenPreview() {
    BookmarkScreen(navController = rememberNavController())

}
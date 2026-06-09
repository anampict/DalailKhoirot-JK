package com.pws.dalail.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pws.dalail.commond.jakartasans
import com.pws.dalail.pdf.BookmarkViewModel
import com.pws.dalail.pdf.LastReadViewModel
import com.pws.dalail.pdf.PdfDisplayMode
import com.pws.dalail.pdf.PdfState
import com.pws.dalail.pdf.PdfViewModel
import kotlin.math.abs

// ─── Colors ───────────────────────────────────────────────────────────────────

private val ReaderGreen      = Color(0xFF1A6B45)
private val ReaderGreenLight = Color(0xFFE8F5EE)

// ─── Main Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    startPage      : Int,
    endPage        : Int,
    chapterTitle   : String,
    chapterNumber  : Int,          // ← baru: untuk bookmark & last-read
    totalPdfPages  : Int = 172,
    navController  : NavController,
    viewModel      : PdfViewModel      = viewModel(),
    bookmarkVm     : BookmarkViewModel = viewModel(),
    lastReadVm     : LastReadViewModel = viewModel()
) {
    val pdfState    by viewModel.pdfState.collectAsState()
    val pageCache   by viewModel.pageCache.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    // ── Status bookmark dari Room (reaktif) ──────────────────────────────────
    val isBookmarked by bookmarkVm.isBookmarkedFlow(chapterNumber).collectAsState(initial = false)

    // ── Inisialisasi chapter ─────────────────────────────────────────────────
    LaunchedEffect(startPage, endPage) {
        viewModel.openChapter(startPage, endPage)
    }

    // ── Simpan "terakhir dibaca" saat pertama kali bab ini dibuka ────────────
    LaunchedEffect(chapterNumber) {
        lastReadVm.saveLastRead(
            chapterNumber = chapterNumber,
            titleArabic   = chapterTitle,
            startPage     = startPage,
            endPage       = endPage
        )
    }

    // Scroll state
    val listState = rememberLazyListState()

    // Update currentPage berdasarkan scroll
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val page = startPage + listState.firstVisibleItemIndex
        viewModel.updateCurrentPage(page)
    }

    // State lokal
    var showMenu      by remember { mutableStateOf(false) }
    var showModeSheet by remember { mutableStateOf(false) }

    // Background berdasarkan mode
    val bgColor = when (displayMode) {
        PdfDisplayMode.LIGHT  -> MaterialTheme.colorScheme.background
        PdfDisplayMode.DARK   -> Color(0xFF1C1C1E)
        PdfDisplayMode.AMOLED -> Color.Black
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            PdfReaderTopBar(
                title         = chapterTitle,
                isBookmarked  = isBookmarked,
                onBack        = { navController.popBackStack() },
                onBookmark    = {
                    // Toggle bookmark via Room
                    bookmarkVm.toggleBookmark(
                        chapterNumber          = chapterNumber,
                        titleArabic            = chapterTitle,
                        startPage              = startPage,
                        endPage                = endPage,
                        isCurrentlyBookmarked  = isBookmarked
                    )
                },
                onMoreClick   = { showMenu = true },
                showMenu      = showMenu,
                onDismissMenu = { showMenu = false },
                onModeClick   = { showModeSheet = true; showMenu = false },
                displayMode   = displayMode
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bgColor)
        ) {
            when (val state = pdfState) {
                is PdfState.Idle, is PdfState.Loading -> {
                    PdfLoadingIndicator()
                }

                is PdfState.Error -> {
                    PdfErrorScreen(message = state.message)
                }

                is PdfState.Success -> {
                    // ── Daftar halaman (lazy, hanya startPage..endPage) ──────
                    LazyColumn(
                        state          = listState,
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 0.dp,
                            vertical   = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            count = endPage - startPage + 1,
                            key   = { index -> startPage + index }
                        ) { index ->
                            val pageNumber = startPage + index

                            LaunchedEffect(pageNumber, displayMode) {
                                viewModel.requestPage(pageNumber)
                            }

                            PdfPageView(
                                pageNumber  = pageNumber,
                                bitmap      = pageCache[pageNumber],
                                displayMode = displayMode,
                                bgColor     = bgColor
                            )
                        }
                    }

                    // ── Bottom page indicator ────────────────────────────────
                    PdfPageIndicator(
                        currentPage   = currentPage,
                        totalPdfPages = totalPdfPages,
                        modifier      = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    // ── Mode Sheet ───────────────────────────────────────────────────────────
    if (showModeSheet) {
        DisplayModeSheet(
            currentMode = displayMode,
            onModeSelected = { mode ->
                viewModel.setDisplayMode(mode)
                showModeSheet = false
            },
            onDismiss = { showModeSheet = false }
        )
    }
}

// ─── Top App Bar ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfReaderTopBar(
    title         : String,
    isBookmarked  : Boolean,
    displayMode   : PdfDisplayMode,
    showMenu      : Boolean,
    onBack        : () -> Unit,
    onBookmark    : () -> Unit,
    onMoreClick   : () -> Unit,
    onDismissMenu : () -> Unit,
    onModeClick   : () -> Unit
) {
    val containerColor = when (displayMode) {
        PdfDisplayMode.LIGHT  -> MaterialTheme.colorScheme.background
        PdfDisplayMode.DARK   -> Color(0xFF1C1C1E)
        PdfDisplayMode.AMOLED -> Color.Black
    }
    val contentColor = when (displayMode) {
        PdfDisplayMode.LIGHT  -> MaterialTheme.colorScheme.onBackground
        PdfDisplayMode.DARK   -> Color.White
        PdfDisplayMode.AMOLED -> Color.White
    }

    TopAppBar(
        title = {
            Text(
                text      = title,
                fontSize  = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color     = contentColor,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint               = contentColor
                )
            }
        },
        actions = {
            // Bookmark (terhubung ke Room)
            IconButton(onClick = onBookmark) {
                Icon(
                    imageVector = if (isBookmarked)
                        Icons.Filled.Bookmark
                    else
                        Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint               = if (isBookmarked) ReaderGreen else contentColor
                )
            }

            // More / dropdown menu
            Box {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector        = Icons.Filled.MoreVert,
                        contentDescription = "Lainnya",
                        tint               = contentColor
                    )
                }

                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Mode Tampilan",
                                fontFamily = jakartasans,
                                fontSize   = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (displayMode) {
                                    PdfDisplayMode.LIGHT  -> Icons.Filled.LightMode
                                    PdfDisplayMode.DARK   -> Icons.Filled.DarkMode
                                    PdfDisplayMode.AMOLED -> Icons.Filled.DarkMode
                                },
                                contentDescription = null,
                                tint = ReaderGreen
                            )
                        },
                        onClick = onModeClick
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        )
    )
}

// ─── PDF Page View (dengan Pinch-to-Zoom & Double-Tap Zoom) ──────────────────

@Composable
private fun PdfPageView(
    pageNumber  : Int,
    bitmap      : Bitmap?,
    displayMode : PdfDisplayMode,
    bgColor     : Color
) {
    var scale  by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animatedScale by animateFloatAsState(
        targetValue   = scale,
        animationSpec = tween(durationMillis = 150),
        label         = "zoom"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            // ── Double-tap zoom ──────────────────────────────────────────
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale  = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2f
                        }
                    }
                )
            }
            // ── Pinch-to-zoom: hanya intercept gestur 2+ jari ───────────
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    var zoom        = 1f
                    var pan         = Offset.Zero
                    var pastSlop    = false
                    val touchSlop   = viewConfiguration.touchSlop

                    do {
                        val event    = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }

                        if (!canceled) {
                            val zoomChange = event.calculateZoom()
                            val panChange  = event.calculatePan()

                            if (!pastSlop) {
                                zoom *= zoomChange
                                pan  += panChange
                                val centroid   = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1 - zoom) * centroid
                                val panMotion  = pan.getDistance()
                                if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                    pastSlop = true
                                }
                            }

                            if (pastSlop && event.changes.size >= 2) {
                                scale = (scale * zoomChange).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    offset += panChange
                                } else {
                                    offset = Offset.Zero
                                }
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (!canceled && event.changes.any { it.pressed })
                }
            }
    ) {
        if (bitmap != null) {
            Image(
                bitmap             = bitmap.asImageBitmap(),
                contentDescription = "Halaman $pageNumber",
                contentScale       = ContentScale.FillWidth,
                modifier           = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX       = animatedScale
                        scaleY       = animatedScale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )
        } else {
            PageSkeleton(bgColor = bgColor)
        }
    }
}

// ─── Page Skeleton ────────────────────────────────────────────────────────────

@Composable
private fun PageSkeleton(bgColor: Color) {
    val skeletonColor = if (bgColor == Color.Black || bgColor == Color(0xFF1C1C1E))
        Color(0xFF2C2C2E)
    else
        Color(0xFFEEEEEE)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.707f)
            .background(skeletonColor)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center),
            color     = ReaderGreen,
            strokeWidth = 2.dp
        )
    }
}

// ─── Bottom Page Indicator ────────────────────────────────────────────────────

@Composable
private fun PdfPageIndicator(
    currentPage   : Int,
    totalPdfPages : Int,
    modifier      : Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter   = fadeIn(),
        exit    = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xCC000000))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text       = "$currentPage / $totalPdfPages",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.White,
                fontFamily = jakartasans
            )
        }
    }
}

// ─── Loading Indicator ────────────────────────────────────────────────────────

@Composable
private fun PdfLoadingIndicator() {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = ReaderGreen, strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text       = "Memuat kitab...",
            fontSize   = 14.sp,
            color      = ReaderGreen.copy(alpha = 0.8f),
            fontFamily = jakartasans
        )
    }
}

// ─── Error Screen ─────────────────────────────────────────────────────────────

@Composable
private fun PdfErrorScreen(message: String) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text       = "Gagal memuat PDF",
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onBackground,
            fontFamily = jakartasans
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text       = message,
            fontSize   = 13.sp,
            color      = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontFamily = jakartasans
        )
    }
}

// ─── Display Mode Bottom Sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisplayModeSheet(
    currentMode    : PdfDisplayMode,
    onModeSelected : (PdfDisplayMode) -> Unit,
    onDismiss      : () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text       = "Mode Tampilan",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                fontFamily = jakartasans,
                modifier   = Modifier.padding(bottom = 8.dp)
            )

            ModeOption(
                label    = "☀️  Terang (Light)",
                selected = currentMode == PdfDisplayMode.LIGHT,
                onClick  = { onModeSelected(PdfDisplayMode.LIGHT) }
            )
            ModeOption(
                label    = "🌙  Gelap (Dark)",
                selected = currentMode == PdfDisplayMode.DARK,
                onClick  = { onModeSelected(PdfDisplayMode.DARK) }
            )
            ModeOption(
                label    = "⚫  AMOLED Hitam",
                selected = currentMode == PdfDisplayMode.AMOLED,
                onClick  = { onModeSelected(PdfDisplayMode.AMOLED) }
            )
        }
    }
}

@Composable
private fun ModeOption(
    label    : String,
    selected : Boolean,
    onClick  : () -> Unit
) {
    val bgColor  = if (selected) ReaderGreenLight else MaterialTheme.colorScheme.surface
    val txtColor = if (selected) ReaderGreen else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = bgColor,
        onClick  = onClick
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text       = label,
                fontSize   = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = txtColor,
                fontFamily = jakartasans
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ReaderGreen)
                )
            }
        }
    }
}

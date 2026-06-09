package com.pws.dalail.pdf

import android.app.Application
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk PDF Reader.
 *
 * Bertanggung jawab:
 * - Membuka / menutup PDF melalui [PdfRendererRepository]
 * - Lazy-render halaman satu per satu saat diminta oleh UI
 * - Menyimpan state halaman yang sudah dirender
 * - Mengelola mode tampilan (Light / Dark / AMOLED)
 */
class PdfViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PdfRendererRepository(application)

    // ─── UI State utama ───────────────────────────────────────────────────────

    private val _pdfState = MutableStateFlow<PdfState>(PdfState.Idle)
    val pdfState: StateFlow<PdfState> = _pdfState.asStateFlow()

    // ─── Bitmap cache per halaman (untuk UI) ─────────────────────────────────
    // Key = pageNumber (1-indexed)

    private val _pageCache = MutableStateFlow<Map<Int, Bitmap>>(emptyMap())
    val pageCache: StateFlow<Map<Int, Bitmap>> = _pageCache.asStateFlow()

    // ─── Mode tampilan ────────────────────────────────────────────────────────

    private val _displayMode = MutableStateFlow(PdfDisplayMode.LIGHT)
    val displayMode: StateFlow<PdfDisplayMode> = _displayMode.asStateFlow()

    // ─── Halaman aktif (diupdate oleh scroll) ────────────────────────────────

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    // ─── Rentang halaman yang aktif dibuka ───────────────────────────────────

    private var activeStartPage = 1
    private var activeEndPage   = 172

    // ─── Lebar layar untuk scaling ───────────────────────────────────────────

    private val screenWidthPx: Int by lazy {
        val wm = application.getSystemService(WindowManager::class.java)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm?.defaultDisplay?.getMetrics(metrics)
        metrics.widthPixels.takeIf { it > 0 } ?: 1080
    }

    // ─── Job tracking ─────────────────────────────────────────────────────────

    private val renderJobs = mutableMapOf<Int, Job>()

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Buka PDF dan siapkan untuk rentang halaman [startPage]..[endPage].
     * Dipanggil dari UI saat screen pertama kali ditampilkan.
     */
    fun openChapter(startPage: Int, endPage: Int) {
        if (_pdfState.value is PdfState.Loading) return

        activeStartPage = startPage
        activeEndPage   = endPage
        _currentPage.value = startPage

        _pdfState.value = PdfState.Loading

        viewModelScope.launch {
            try {
                repository.openPdf()
                _pdfState.value = PdfState.Success(
                    totalPages = repository.totalPageCount,
                    startPage  = startPage,
                    endPage    = endPage
                )
            } catch (e: Exception) {
                _pdfState.value = PdfState.Error(
                    message = e.message ?: "Gagal membuka PDF"
                )
            }
        }
    }

    /**
     * Minta render halaman [pageNumber].
     * Dipanggil oleh LazyColumn saat item muncul di layar.
     *
     * Aman dipanggil dari banyak coroutine — PdfRendererRepository.renderPage()
     * menggunakan Mutex untuk serialisasi akses ke PdfRenderer.
     */
    fun requestPage(pageNumber: Int) {
        // Fast path: sudah ada di cache UI, tidak perlu render
        if (_pageCache.value.containsKey(pageNumber)) return

        // Batalkan job lama untuk halaman yang sama (jika ada yang sedang antri)
        renderJobs[pageNumber]?.cancel()

        val mode = _displayMode.value

        renderJobs[pageNumber] = viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = repository.renderPage(
                    pageNumber  = pageNumber,
                    displayMode = mode,
                    screenWidth = screenWidthPx
                )
                // Hanya update jika mode belum berubah selama render berlangsung
                if (bitmap != null && _displayMode.value == mode) {
                    // Gunakan update atomik agar tidak ada race condition antar coroutine
                    _pageCache.value = _pageCache.value + (pageNumber to bitmap)
                }
            } catch (_: Exception) {
                // Abaikan error render individual (misal job di-cancel)
            }
        }
    }

    /**
     * Update halaman aktif berdasarkan scroll position.
     * Dipanggil dari UI: currentPage = startPage + firstVisibleItemIndex
     */
    fun updateCurrentPage(page: Int) {
        _currentPage.value = page
    }

    /**
     * Ganti mode tampilan (Light / Dark / AMOLED).
     * Membatalkan semua render job aktif lalu menghapus cache,
     * sehingga LaunchedEffect(pageNumber, displayMode) akan request ulang
     * dengan mode yang baru.
     */
    fun setDisplayMode(mode: PdfDisplayMode) {
        if (_displayMode.value == mode) return

        // 1. Batalkan semua job render yang sedang berjalan
        //    agar bitmap mode lama tidak menimpa cache mode baru
        renderJobs.values.forEach { it.cancel() }
        renderJobs.clear()

        // 2. Set mode baru
        _displayMode.value = mode

        // 3. Kosongkan cache UI dan repository
        _pageCache.value = emptyMap()
        repository.evictAll()
    }

    /**
     * Lepaskan halaman dari cache UI yang sudah jauh dari posisi scroll.
     * Opsional — bisa dipanggil untuk menghemat memori.
     */
    fun releasePage(pageNumber: Int) {
        if (_pageCache.value.containsKey(pageNumber)) {
            _pageCache.value = _pageCache.value - pageNumber
            repository.evictPage(pageNumber)
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        renderJobs.values.forEach { it.cancel() }
        renderJobs.clear()
        repository.closePdf()
    }
}

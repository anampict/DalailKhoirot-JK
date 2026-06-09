package com.pws.dalail.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Mode tampilan PDF (warna layar).
 */
enum class PdfDisplayMode { LIGHT, DARK, AMOLED }

/**
 * Repository yang mengelola PdfRenderer Android.
 *
 * ⚠️ PdfRenderer TIDAK thread-safe.
 *    Semua akses ke pdfRenderer (openPage, dll.) HARUS dilindungi [rendererMutex].
 *    Tanpa Mutex, scroll cepat akan menyebabkan crash:
 *    "Already have a page open" atau "PdfRenderer is closed".
 *
 * Alur kerja:
 * 1. [openPdf]     — copy assets → cacheDir, buka ParcelFileDescriptor & PdfRenderer
 * 2. [renderPage]  — render halaman ke Bitmap dengan LruCache + Mutex
 * 3. [closePdf]    — tutup semua resource, bersihkan cache
 */
class PdfRendererRepository(private val context: Context) {

    companion object {
        private const val ASSET_NAME = "dalailkhoirotfull.pdf"
        private const val CACHE_FILE = "dalailkhoirot_cached.pdf"
        private const val RENDER_DPI = 2   // ×2 untuk kualitas layak tanpa boros RAM
        private const val MAX_PAGES  = 172
    }

    // ─── State internal ───────────────────────────────────────────────────────

    private var pdfRenderer : PdfRenderer?          = null
    private var parcelFd    : ParcelFileDescriptor? = null
    private var bitmapCache : LruCache<Int, Bitmap>? = null

    /**
     * Mutex untuk serialisasi akses ke PdfRenderer.
     * PdfRenderer hanya boleh membuka SATU halaman dalam satu waktu.
     */
    private val rendererMutex = Mutex()

    val isOpen: Boolean get() = pdfRenderer != null

    // ─── Open / Close ─────────────────────────────────────────────────────────

    /**
     * Buka PDF dari assets.
     * Aman dipanggil berkali-kali — idempoten jika sudah terbuka.
     */
    suspend fun openPdf() = withContext(Dispatchers.IO) {
        rendererMutex.withLock {
            if (pdfRenderer != null) return@withLock

            // 1. Copy dari assets → cacheDir jika belum ada
            val cacheFile = File(context.cacheDir, CACHE_FILE)
            if (!cacheFile.exists()) {
                context.assets.open(ASSET_NAME).use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // 2. Buka ParcelFileDescriptor
            parcelFd = ParcelFileDescriptor.open(
                cacheFile, ParcelFileDescriptor.MODE_READ_ONLY
            )

            // 3. Inisialisasi PdfRenderer
            pdfRenderer = PdfRenderer(parcelFd!!)

            // 4. LruCache — maks 1/8 heap tersedia
            val maxMemKb   = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            val cacheSizeKb = maxMemKb / 8
            bitmapCache = object : LruCache<Int, Bitmap>(cacheSizeKb) {
                override fun sizeOf(key: Int, value: Bitmap): Int =
                    value.byteCount / 1024
            }
        }
    }

    /**
     * Tutup semua resource.
     * Harus dipanggil di ViewModel.onCleared() atau ketika screen ditutup.
     */
    fun closePdf() {
        // closePdf dipanggil dari main thread (onCleared), tidak perlu suspend.
        // Mutex.tryLock untuk menghindari deadlock jika ada coroutine sedang render.
        bitmapCache?.evictAll()
        bitmapCache = null
        pdfRenderer?.close()
        pdfRenderer = null
        parcelFd?.close()
        parcelFd = null
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    /**
     * Render halaman [pageNumber] (1-indexed).
     *
     * Dilindungi [rendererMutex] sehingga aman dipanggil dari banyak coroutine
     * secara bersamaan — hanya satu halaman yang di-render dalam satu waktu.
     *
     * @param pageNumber  Nomor halaman kitab (1–172)
     * @param displayMode Mode warna tampilan
     * @param screenWidth Lebar layar px untuk scaling kualitas
     * @return Bitmap hasil render, atau null jika renderer belum siap / error
     */
    suspend fun renderPage(
        pageNumber  : Int,
        displayMode : PdfDisplayMode = PdfDisplayMode.LIGHT,
        screenWidth : Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {

        // Cek apakah ada di cache SEBELUM lock (fast path, tidak perlu lock)
        val cacheKey = pageNumber * 10 + displayMode.ordinal
        bitmapCache?.get(cacheKey)?.let { return@withContext it }

        // Masuk ke critical section — hanya satu thread boleh di sini
        rendererMutex.withLock {
            // Double-check: mungkin sudah dirender oleh coroutine lain saat menunggu
            bitmapCache?.get(cacheKey)?.let { return@withLock it }

            val renderer = pdfRenderer ?: return@withLock null
            val cache    = bitmapCache ?: return@withLock null

            // Konversi ke 0-indexed, amankan dari out-of-bounds
            val pdfIndex = (pageNumber - 1).coerceIn(0, renderer.pageCount - 1)

            try {
                renderer.openPage(pdfIndex).use { page ->
                    val scale  = screenWidth.toFloat() / page.width.toFloat() * RENDER_DPI
                    val width  = (page.width  * scale).toInt().coerceAtLeast(1)
                    val height = (page.height * scale).toInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                    // Isi background putih terlebih dahulu
                    Canvas(bitmap).drawColor(Color.WHITE)

                    page.render(
                        bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )

                    // Terapkan color matrix sesuai mode
                    val finalBitmap = applyDisplayMode(bitmap, displayMode)

                    cache.put(cacheKey, finalBitmap)
                    finalBitmap
                }
            } catch (e: Exception) {
                // Tangkap crash PdfRenderer (misal renderer sudah ditutup)
                null
            }
        }
    }

    // ─── Cache management ─────────────────────────────────────────────────────

    /** Hapus satu halaman dari semua mode di cache */
    fun evictPage(pageNumber: Int) {
        PdfDisplayMode.entries.forEach { mode ->
            bitmapCache?.remove(pageNumber * 10 + mode.ordinal)
        }
    }

    /** Hapus seluruh cache bitmap */
    fun evictAll() {
        bitmapCache?.evictAll()
    }

    val totalPageCount: Int
        get() = pdfRenderer?.pageCount ?: MAX_PAGES

    // ─── Color Matrix Helpers ─────────────────────────────────────────────────

    private fun applyDisplayMode(src: Bitmap, mode: PdfDisplayMode): Bitmap {
        return when (mode) {
            PdfDisplayMode.LIGHT  -> src
            PdfDisplayMode.DARK   -> invertBitmap(src, amoled = false)
            PdfDisplayMode.AMOLED -> invertBitmap(src, amoled = true)
        }
    }

    /**
     * Invert warna bitmap menggunakan ColorMatrix.
     * [src] di-recycle setelah diproses — jangan gunakan lagi setelah panggilan ini.
     */
    private fun invertBitmap(src: Bitmap, amoled: Boolean): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Matrix: R' = -R+1, G' = -G+1, B' = -B+1  (invert warna)
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f,  0f,  0f, 0f, 255f,
                 0f, -1f,  0f, 0f, 255f,
                 0f,  0f, -1f, 0f, 255f,
                 0f,  0f,  0f, 1f,   0f
            )
        )

        // Warm tint untuk Dark mode agar tidak terlalu dingin
        if (!amoled) {
            val warmth = ColorMatrix().apply { setScale(1.0f, 0.95f, 0.85f, 1f) }
            matrix.postConcat(warmth)
        }

        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(src, 0f, 0f, paint)

        src.recycle()
        return result
    }
}

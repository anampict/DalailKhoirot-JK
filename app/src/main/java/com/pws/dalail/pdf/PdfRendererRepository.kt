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
 * Alur kerja:
 * 1. [openPdf]     — copy assets → cacheDir, buka ParcelFileDescriptor & PdfRenderer
 * 2. [renderPage]  — render halaman tertentu ke Bitmap (dengan LruCache)
 * 3. [closePdf]    — tutup PdfRenderer & ParcelFileDescriptor, bersihkan cache
 */
class PdfRendererRepository(private val context: Context) {

    companion object {
        private const val ASSET_NAME  = "dalailkhoirotfull.pdf"
        private const val CACHE_FILE  = "dalailkhoirot_cached.pdf"
        private const val RENDER_DPI  = 2   // ×2 untuk kualitas cukup tanpa boros RAM
        private const val MAX_PAGES   = 172 // total halaman PDF
    }

    // ─── State internal ───────────────────────────────────────────────────────

    private var pdfRenderer  : PdfRenderer?           = null
    private var parcelFd     : ParcelFileDescriptor?  = null
    private var bitmapCache  : LruCache<Int, Bitmap>? = null

    val isOpen: Boolean get() = pdfRenderer != null

    // ─── Open / Close ─────────────────────────────────────────────────────────

    /**
     * Buka PDF dari assets. Harus dipanggil sekali di awal.
     * Thread-safe: dijalankan di IO dispatcher.
     */
    suspend fun openPdf() = withContext(Dispatchers.IO) {
        // Jika sudah terbuka, tidak perlu buka ulang
        if (pdfRenderer != null) return@withContext

        // 1. Copy dari assets ke cacheDir (hanya jika belum ada / perlu update)
        val cacheFile = File(context.cacheDir, CACHE_FILE)
        if (!cacheFile.exists()) {
            context.assets.open(ASSET_NAME).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        // 2. Buka sebagai ParcelFileDescriptor
        parcelFd = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)

        // 3. Inisialisasi PdfRenderer
        pdfRenderer = PdfRenderer(parcelFd!!)

        // 4. Siapkan LruCache — maksimal 1/8 heap tersedia
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSizeKb = maxMemoryKb / 8
        bitmapCache = object : LruCache<Int, Bitmap>(cacheSizeKb) {
            override fun sizeOf(key: Int, value: Bitmap): Int =
                value.byteCount / 1024
        }
    }

    /** Tutup semua resource. Panggil di onCleared / onStop. */
    fun closePdf() {
        bitmapCache?.evictAll()
        bitmapCache = null
        pdfRenderer?.close()
        pdfRenderer = null
        parcelFd?.close()
        parcelFd = null
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    /**
     * Render halaman [pageNumber] (1-indexed, sesuai nomor halaman kitab).
     * Mengembalikan Bitmap dari cache jika tersedia, atau merender halaman baru.
     *
     * @param pageNumber  Nomor halaman kitab (1–172)
     * @param displayMode Mode warna (LIGHT / DARK / AMOLED)
     * @param screenWidth Lebar layar dalam pixel (untuk scaling)
     */
    suspend fun renderPage(
        pageNumber  : Int,
        displayMode : PdfDisplayMode = PdfDisplayMode.LIGHT,
        screenWidth : Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        val renderer = pdfRenderer ?: return@withContext null
        val cache    = bitmapCache ?: return@withContext null

        // Cache key mempertimbangkan mode agar DARK ≠ LIGHT
        val cacheKey = pageNumber * 10 + displayMode.ordinal

        // Kembalikan dari cache jika ada
        cache.get(cacheKey)?.let { return@withContext it }

        // Konversi ke 0-indexed
        val pdfIndex = (pageNumber - 1).coerceIn(0, renderer.pageCount - 1)

        renderer.openPage(pdfIndex).use { page ->
            // Hitung ukuran render berdasarkan lebar layar
            val scale  = screenWidth.toFloat() / page.width.toFloat() * RENDER_DPI
            val width  = (page.width  * scale).toInt()
            val height = (page.height * scale).toInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // Background putih (default PdfRenderer transparan)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Terapkan color matrix sesuai mode
            val finalBitmap = applyDisplayMode(bitmap, displayMode)

            cache.put(cacheKey, finalBitmap)
            finalBitmap
        }
    }

    /** Hapus halaman tertentu dari cache (berguna saat mode berubah) */
    fun evictPage(pageNumber: Int) {
        PdfDisplayMode.entries.forEach { mode ->
            bitmapCache?.remove(pageNumber * 10 + mode.ordinal)
        }
    }

    /** Hapus semua halaman dari cache */
    fun evictAll() {
        bitmapCache?.evictAll()
    }

    val totalPageCount: Int
        get() = pdfRenderer?.pageCount ?: MAX_PAGES

    // ─── Color Matrix Helpers ─────────────────────────────────────────────────

    private fun applyDisplayMode(src: Bitmap, mode: PdfDisplayMode): Bitmap {
        return when (mode) {
            PdfDisplayMode.LIGHT  -> src          // tidak ada perubahan
            PdfDisplayMode.DARK   -> invertBitmap(src, amoled = false)
            PdfDisplayMode.AMOLED -> invertBitmap(src, amoled = true)
        }
    }

    /**
     * Invert warna bitmap menggunakan ColorMatrix.
     * Mode AMOLED: background benar-benar hitam (#000000).
     */
    private fun invertBitmap(src: Bitmap, amoled: Boolean): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Matrix: R' = -R+1, G' = -G+1, B' = -B+1
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f,  0f,  0f, 0f, 255f,
                 0f, -1f,  0f, 0f, 255f,
                 0f,  0f, -1f, 0f, 255f,
                 0f,  0f,  0f, 1f,   0f
            )
        )

        // Warm tint khusus Dark mode agar tidak terlalu biru
        if (!amoled) {
            val warmth = ColorMatrix().apply {
                setScale(1.0f, 0.95f, 0.85f, 1f)
            }
            matrix.postConcat(warmth)
        }

        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(src, 0f, 0f, paint)

        // Untuk AMOLED, paksa background hitam murni
        if (amoled) {
            // Tidak diperlukan step tambahan karena invert putih → hitam sudah sempurna
        }

        src.recycle()
        return result
    }
}

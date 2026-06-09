package com.pws.dalail.pdf

import android.graphics.Bitmap

// ─── UI State ────────────────────────────────────────────────────────────────

sealed class PdfState {
    /** Belum ada PDF yang dibuka */
    object Idle : PdfState()

    /** Sedang memuat (copy assets → cache, buka PdfRenderer) */
    object Loading : PdfState()

    /** PDF berhasil dimuat, siap ditampilkan */
    data class Success(
        val totalPages: Int,
        val startPage: Int,
        val endPage: Int
    ) : PdfState()

    /** Terjadi error saat memuat / merender */
    data class Error(val message: String) : PdfState()
}

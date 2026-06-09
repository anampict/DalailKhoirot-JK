package com.pws.dalail.pdf

import android.graphics.Bitmap

/**
 * Merepresentasikan satu halaman PDF yang sudah/belum dirender.
 *
 * @param pageNumber  Nomor halaman asli dalam PDF (0-indexed secara internal,
 *                    tapi kita simpan sebagai 1-indexed sesuai nomor kitab)
 * @param bitmap      Hasil render, null jika belum dirender / gagal
 */
data class PdfPageItem(
    val pageNumber: Int,
    val bitmap: Bitmap? = null
)

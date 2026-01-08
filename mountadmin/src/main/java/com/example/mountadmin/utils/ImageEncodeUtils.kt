package com.example.mountadmin.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageEncodeUtils {
    /**
     * Encode gambar dari Uri menjadi Base64 (tanpa Firebase Storage).
     * Catatan: gambar akan di-resize & compress agar tidak terlalu besar.
     */
    fun uriToBase64(
        context: Context,
        uri: Uri,
        maxSizePx: Int = 1080,
        jpegQuality: Int = 75
    ): String? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val resized = resizeKeepingAspect(decoded, maxSizePx)

            val out = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out)
            val compressedBytes = out.toByteArray()

            Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    private fun resizeKeepingAspect(bitmap: Bitmap, maxSizePx: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxSizePx && h <= maxSizePx) return bitmap

        val scale = if (w >= h) maxSizePx.toFloat() / w.toFloat() else maxSizePx.toFloat() / h.toFloat()
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}


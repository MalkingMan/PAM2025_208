package com.example.mounttrack.utils

import android.graphics.BitmapFactory
import android.util.Base64

object ImageDecodeUtils {
    /**
     * Decode Base64 (dengan atau tanpa prefix data URI) menjadi ByteArray.
     * Return null jika input kosong/invalid.
     */
    fun decodeBase64ToBytes(value: String?): ByteArray? {
        if (value.isNullOrBlank()) return null
        val raw = value.substringAfter(",", value) // handle data:image/...;base64,
        return try {
            Base64.decode(raw, Base64.DEFAULT)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun isLikelyBase64Image(value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        return value.startsWith("data:image", ignoreCase = true) ||
            // heuristic: base64 biasanya panjang & hanya berisi charset base64
            (value.length > 200 && value.trim().matches(Regex("^[A-Za-z0-9+/=\\r\\n]+$")))
    }

    fun decodeBase64ToBitmap(value: String?) =
        decodeBase64ToBytes(value)?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
}


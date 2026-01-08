package com.example.mountadmin.utils

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

object ImageDisplayUtils {

    private const val TAG = "ImageDisplayUtils"

    /**
     * Load image from either Base64 (with/without data URI prefix) or URL into ImageView.
     * - If Base64: decode to bitmap.
     * - If URL: Glide.
     * - If empty/invalid: placeholder.
     */
    fun loadInto(
        imageView: ImageView,
        value: String?,
        @DrawableRes placeholderRes: Int
    ) {
        val ctx = imageView.context
        val v = value?.trim().orEmpty()

        Log.d(TAG, "loadInto value(length=${v.length})=${v.take(48)}")

        if (v.isBlank()) {
            imageView.setImageResource(placeholderRes)
            return
        }

        if (isLikelyBase64Image(v)) {
            val bytes = decodeBase64ToBytes(v)
            if (bytes == null || bytes.isEmpty()) {
                Log.w(TAG, "Base64 decode failed")
                imageView.setImageResource(placeholderRes)
                return
            }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                Log.w(TAG, "BitmapFactory decode returned null")
                imageView.setImageResource(placeholderRes)
            }
            return
        }

        // treat as url
        Glide.with(ctx)
            .load(v)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .into(imageView)
    }

    private fun decodeBase64ToBytes(value: String): ByteArray? {
        val raw = value.substringAfter(",", value)
        return try {
            Base64.decode(raw, Base64.DEFAULT)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun isLikelyBase64Image(value: String): Boolean {
        return value.startsWith("data:image", ignoreCase = true) ||
            (value.length > 200 && value.trim().matches(Regex("^[A-Za-z0-9+/=\\r\\n]+$")))
    }
}

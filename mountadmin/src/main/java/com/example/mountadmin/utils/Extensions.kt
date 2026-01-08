package com.example.mountadmin.utils

import android.view.View
import java.text.NumberFormat
import java.util.Locale

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Long.formatNumber(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Int.formatNumber(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}


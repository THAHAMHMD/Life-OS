package com.bloomhaven.app.core.util

import java.text.NumberFormat
import java.util.Locale

/** Shared money formatting for every module that records an amount (Money Haven, Commitments, Petrova expenses). */
object CurrencyUtils {
    private val formatter = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    fun format(amount: Double): String = "₹${formatter.format(amount)}"
}

package com.mikeisesele.clearr.ui.feature.budget

internal fun formatKobo(kobo: Long): String {
    val naira = kobo / 100.0
    return when {
        naira >= 1_000_000 -> "₦" + "%.1f".format(naira / 1_000_000).trimEnd('0').trimEnd('.') + "M"
        naira >= 100_000 -> "₦" + "%.0f".format(naira / 1_000) + "k"
        naira >= 10_000 -> "₦" + "%.1f".format(naira / 1_000).trimEnd('0').trimEnd('.') + "k"
        else -> "₦" + "%,d".format(naira.toLong())
    }
}

internal fun formatKoboFull(kobo: Long): String = "₦" + "%,d".format(kobo / 100)

package com.mikeisesele.clearr.ui.feature.budget.utils

fun formatKobo(kobo: Long): String {
    val naira = kobo / 100.0
    return when {
        naira >= 1_000_000 -> "₦${formatSingleDecimal(naira / 1_000_000)}M"
        naira >= 100_000 -> "₦${(naira / 1_000).toLong()}k"
        naira >= 10_000 -> "₦${formatSingleDecimal(naira / 1_000)}k"
        else -> "₦${groupThousands(naira.toLong())}"
    }
}

fun formatKoboFull(kobo: Long): String = "₦${groupThousands(kobo / 100)}"

private fun formatSingleDecimal(value: Double): String {
    val scaled = (value * 10).toLong()
    val whole = scaled / 10
    val fraction = scaled % 10
    return if (fraction == 0L) whole.toString() else "$whole.$fraction"
}

private fun groupThousands(value: Long): String {
    val digits = value.toString()
    val grouped = StringBuilder()
    digits.forEachIndexed { index, ch ->
        if (index > 0 && (digits.length - index) % 3 == 0) {
            grouped.append(',')
        }
        grouped.append(ch)
    }
    return grouped.toString()
}

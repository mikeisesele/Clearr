package com.mikeisesele.clearr.ui.util

import java.text.SimpleDateFormat
import java.util.*

val MONTHS = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
val MONTHS_FULL = listOf("January","February","March","April","May","June","July","August","September","October","November","December")

fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH)

fun isFuture(year: Int, monthIndex: Int): Boolean {
    val cy = currentYear()
    val cm = currentMonth()
    if (year > cy) return true
    if (year == cy && monthIndex > cm) return true
    return false
}

fun formatAmount(amount: Double): String {
    return "₦${String.format("%,.0f", amount)}"
}

fun formatTimestamp(epochMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(epochMs))
}

fun buildWhatsAppLink(phone: String, name: String, month: String, year: Int, amount: String): String {
    val clean = phone.replace(Regex("[^0-9]"), "").let {
        if (it.startsWith("0")) "234${it.substring(1)}" else it
    }
    val text = "Hi $name, your dues for $month $year are outstanding. Kindly pay $amount."
    return "https://wa.me/$clean?text=${java.net.URLEncoder.encode(text, "UTF-8")}"
}

fun buildBulkShareText(
    month: String,
    year: Int,
    dueAmount: Double,
    paidNames: List<String>,
    unpaidNames: List<String>,
    totalCollected: Double,
    outstanding: Double
): String {
    return """
📋 DUES SUMMARY — $month $year
Due: ${formatAmount(dueAmount)}

✅ Paid (${paidNames.size}): ${if (paidNames.isEmpty()) "None" else paidNames.joinToString(", ")}
❌ Unpaid (${unpaidNames.size}): ${if (unpaidNames.isEmpty()) "None" else unpaidNames.joinToString(", ")}

Total Collected: ${formatAmount(totalCollected)}
Outstanding: ${formatAmount(outstanding)}

Sent via Dues Tracker App
    """.trimIndent()
}

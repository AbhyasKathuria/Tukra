package com.xsparks.tukra.core.util

/**
 * Formats integer paise into an Indian Rupee string with proper Indian digit grouping.
 * e.g.
 * 7000 paise -> "₹70"
 * 14500 paise -> "₹145"
 * 10000000 paise -> "₹1,00,000" (1 Lakh rupees)
 * 12400000 paise -> "₹1,24,000"
 */
fun Int.toRupees(): String {
    val rupees = this / 100
    val paiseRemainder = this % 100

    val formattedRupees = formatIndianNumber(rupees.toLong())
    return if (paiseRemainder == 0) {
        "₹$formattedRupees"
    } else {
        "₹$formattedRupees.${paiseRemainder.toString().padStart(2, '0')}"
    }
}

fun Long.toRupees(): String {
    val rupees = this / 100
    val paiseRemainder = this % 100

    val formattedRupees = formatIndianNumber(rupees)
    return if (paiseRemainder == 0L) {
        "₹$formattedRupees"
    } else {
        "₹$formattedRupees.${paiseRemainder.toString().padStart(2, '0')}"
    }
}

/**
 * Formats a non-negative long using Indian digit grouping:
 * The last 3 digits are grouped, and preceding digits are grouped in pairs of 2.
 */
fun formatIndianNumber(number: Long): String {
    val isNegative = number < 0
    val absStr = kotlin.math.abs(number).toString()

    if (absStr.length <= 3) {
        return if (isNegative) "-$absStr" else absStr
    }

    val lastThree = absStr.takeLast(3)
    val remaining = absStr.dropLast(3)

    val builder = StringBuilder()
    var count = 0
    for (i in remaining.length - 1 downTo 0) {
        builder.insert(0, remaining[i])
        count++
        if (count == 2 && i != 0) {
            builder.insert(0, ',')
            count = 0
        }
    }

    builder.append(',').append(lastThree)
    return if (isNegative) "-$builder" else builder.toString()
}

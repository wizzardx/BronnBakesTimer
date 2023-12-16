package com.example.bronnbakestimer.util

/**
 * Normalises a string input to remove non-numeric characters and leading zeros.
 *
 * This function processes a string input, typically representing a number, and
 * removes any non-digit characters. It also removes leading zeros while
 * preserving the numerical value of the string. If the string does not contain
 * any digits, or only contains zeros, the result is "0". This function is
 * useful for sanitizing and standardizing numerical user inputs, ensuring they
 * are in a format suitable for further processing or conversion to an integer.
 *
 * @param s The string input to be normalised.
 * @return A normalised string with non-numeric characters removed and leading zeros stripped.
 */
fun normaliseIntInput(s: String): String {
    // Filter out non-digit characters
    val numericPart = s.filter { it.isDigit() }

    // Remove leading zeros while preserving at least one zero if the string is empty
    val normalised =
        numericPart.dropWhile { it == '0' }.let {
            it.ifEmpty { "0" }
        }

    return normalised
}

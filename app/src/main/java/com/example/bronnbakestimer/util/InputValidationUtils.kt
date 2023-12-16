package com.example.bronnbakestimer.util

/**
 * Normalizes a string input to ensure it contains only numeric digits.
 *
 * This function checks the provided `input` string and returns it if it's either
 * empty or contains only digit characters. If `input` contains any non-digit
 * characters, the function returns the `orig` string, which represents the
 * original or previous value.
 *
 * @param input The string to be normalized.
 * @param orig The original string to revert to if `input` is invalid.
 * @return A string that is either the normalized input or the original string.
 */
fun normaliseIntInput(
    input: String,
    orig: String,
): String {
    return if (input.isEmpty() || input.all { it.isDigit() }) {
        input // Use newText if it's empty or all digits
    } else {
        orig // Retain the previous value of text if the input contains non-digit characters
    }
}

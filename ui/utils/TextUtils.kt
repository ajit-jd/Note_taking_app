// File: app/src/main/java/com/example/project7/ui/utils/TextUtils.kt
package com.example.project7.ui.utils

const val BULLET_PREFIX = "•  "
const val TAB_SPACES = "    "

fun findLineStart(text: CharSequence, offset: Int): Int {
    if (text.isEmpty()) { // <<< ADD THIS CHECK
        return 0
    }
    // Ensure offset is within bounds for safety, although coerceAtLeast helps the loop
    val safeOffset = offset.coerceIn(0, text.length)

    // Iterate backwards from the character just before the safeOffset
    for (i in (safeOffset - 1).coerceAtLeast(0) downTo 0) {
        // Check if i is a valid index before accessing text[i]
        if (i < text.length && text[i] == '\n') { // Check bounds again just in case
            return i + 1
        }
    }
    return 0
}

fun findLineEnd(text: CharSequence, offset: Int): Int {
    if (text.isEmpty()) { // <<< ADD THIS CHECK
        return text.length // or 0, but text.length is consistent with non-empty case
    }
    // Ensure offset is within bounds for safety
    val safeOffset = offset.coerceIn(0, text.length)

    for (i in safeOffset until text.length) {
        if (text[i] == '\n') {
            return i
        }
    }
    return text.length
}
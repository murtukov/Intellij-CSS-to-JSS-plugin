package org.murtukov.css_to_jss.util

/**
 * Removes redundant whitespaces from string and trims it.
 */
fun String.normalizeWhitespaces(): String {
    return this.trim().replace("\\s+".toRegex(), " ")
}

/**
 * Removes code blocks from string.
 */
fun String.removeCommentBlocks(): String {
    var result = this

    while (true) {
        val start = result.indexOf("/*")
        val end = result.indexOf("*/")

        result = if (-1 != start && -1 != end) {
            result.substringBefore("/*") + result.substringAfter("*/")
        } else break;
    }

    return result;
}
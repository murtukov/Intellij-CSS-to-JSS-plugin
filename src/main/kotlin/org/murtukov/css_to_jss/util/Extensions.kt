package org.murtukov.css_to_jss.util

/**
 * TODO: Move extensions to CssConverter class
 */

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
        val end   = result.indexOf("*/")

        result = if (-1 != start && -1 != end) {
            result.substringBefore("/*") + result.substringAfter("*/")
        } else break;
    }

    return result;
}

/**
 * Removes single or double quotes from the start and the end of a string.
 */
fun String.trimQuotes() = this.removeSurrounding("'").removeSurrounding("\"")

/**
 * Checks if given string contains only digits and max 1 point
 */
fun String.isNumeric() = this.toDoubleOrNull() != null

/**
 * Converts a kebab-cased string into a camelCased one.
 */
fun String.toCamelCase(): String {
    return this
        .split('-')
        .joinToString("") { it.capitalize() }
        .decapitalize()
}
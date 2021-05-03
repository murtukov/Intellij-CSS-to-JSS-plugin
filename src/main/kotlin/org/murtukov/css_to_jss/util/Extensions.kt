package org.murtukov.css_to_jss.util

import java.util.ArrayList

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
        .explode('-')
        .joinToString("") { it.capitalize() }
        .decapitalize()
}

fun Char.checkParenthesis(): Int {
    return when (this) {
        '('  ->  1
        ')'  -> -1
        else ->  0
    }
}

/**
 * Splits string similar to the standart `split` function, but ignores
 * separators inside parentheses, single quotes and double quotes.
 */
fun String.explode(separator: Char): ArrayList<String> {
    var buffer = ""
    val parts = ArrayList<String>()
    var parenthesis = 0
    var singleQuote = false
    var doubleQuote = false

    for (c in this) {
        when (c) {
            '('  -> parenthesis++
            ')'  -> parenthesis--
            '\'' -> singleQuote = !singleQuote
            '"'  -> doubleQuote = !doubleQuote
        }

        if (separator == c && 0 == parenthesis && !singleQuote && !doubleQuote) {
            parts.add(buffer.trim().replace('\n', ' '))
            buffer = ""
        } else {
            buffer += c
        }
    }

    if (buffer.isNotBlank()) {
        parts.add(buffer.trim().replace('\n', ' '))
    }

    return parts
}

/**
 * Checks if string has a specific char.
 */
fun String.has(char: Char): Boolean {
    var parenthesis = 0
    var singleQuote = false
    var doubleQuote = false

    for (c in this) {
        when (c) {
            '('  -> parenthesis++
            ')'  -> parenthesis--
            '\'' -> singleQuote = !singleQuote
            '"'  -> doubleQuote = !doubleQuote
        }

        if (c == char && 0 == parenthesis && !singleQuote && !doubleQuote) {
            return true
        }
    }

    return false
}

/**
 * in:  rgba(50, 50, 93,  0.25)   0px 50px 100px -20px, rgba(0, 0, 0, 0.3) 0px 30px 60px -30px
 * out: rgba(50,50,93,0.25) 0px 50px 100px -20px,rgba(0, 0, 0, 0.3) 0px 30px 60px -30px
 */
fun String.normalize(): String {
    var prev: Char? = null
    var result = ""

    for (c in this) {
        if (c == ' ' && (prev == ' ' || prev == ',' || prev == null)) {
            continue
        }

        if (c == ')') {
            result = result.trimEnd()
        }

        result += c;
        prev = c;
    }

    return result
}
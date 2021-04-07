package org.murtukov.css_to_jss

import org.murtukov.css_to_jss.util.normalizeWhitespaces
import org.murtukov.css_to_jss.util.removeCommentBlocks
import java.lang.NumberFormatException
import java.util.*

class CssConverter {
    private val rules   = arrayOf("margin", "padding")
    private var current = ""

    fun convert(css: String): String {
        val normalizedCss    = css.normalizeWhitespaces().removeCommentBlocks()
        var parenthesisScope = 0
        val stoppers         = "{:;}"
        var buffer           = ""
        var result           = ""

        for (c in normalizedCss) {
            when (c) {
                '(' -> parenthesisScope++
                ')' -> parenthesisScope--
            }

            if (stoppers.contains(c) && 0 == parenthesisScope) {
                result += when (c) {
                    '{' -> "{\n"
                    ':' -> "    " + convertRuleName(buffer.trim()) + ":"
                    ';' -> " ${CssValue(buffer.trim())},\n"
                    '}' -> "}"
                    else -> ""
                }
                buffer = ""
            } else {
                buffer += c
            }
        }

        return result;
    }

    private fun convertRuleName(token: String): String {
        return when {
            token.startsWith('-') -> "'$token'"
            else -> toCamelCase(token).also { current = it }
        }
    }

    /**
     * Converts a kebab-cased string into a camelCased one.
     */
    private fun toCamelCase(input: String): String {
        return input
            .split('-')
            .joinToString("") { it.first().toUpperCase() + it.drop(1) }
            .decapitalize()
    }

    private fun maybeWrapIntoQuotes(input: String): String {
        return if (isNumeric(input)) input else "'$input'"
    }

    private fun removeSuffix(input: String): String {
        return if (input.endsWith("px")) {
            input.dropLast(2)
        } else {
            input
        }
    }

    private fun isNumeric(str: String): Boolean {
        return try {
            str.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun trimQuotes(input: String): String {
        return input
            .removeSurrounding("'")
            .removeSurrounding("\"")
    }

    private fun split(input: String, separator: Char): ArrayList<String> {
        var buffer = "";
        val parts = ArrayList<String>()
        var parenthesis = 0
        var singleQuote = false
        var doubleQuote = false

        for (c in input) {
            when (c) {
                '('  -> parenthesis++
                ')'  -> parenthesis--
                '\'' -> singleQuote = !singleQuote
                '"'  -> doubleQuote = !doubleQuote
            }

            if (separator == c && 0 == parenthesis && !singleQuote && !doubleQuote) {
                parts.add(buffer.trim().replace('\n', ' '))
                buffer = "";
            } else {
                buffer += c;
            }
        }

        if (buffer.isNotBlank()) {
            parts.add(buffer.trim().replace('\n', ' '))
            buffer = "";
        }

        return parts
    }

    private inner class CssValue(input: String) {
        private val parts: ArrayList<String> = split(input, ',')

        override fun toString(): String {
            val result = ArrayList<String>()

            for (part in parts) {
                val part = part.normalizeWhitespaces();
                val partResult = ArrayList<String>()
                val values = split(part, ' ')

                if ("fontFamily" == current) {
                    partResult.add(processValue(part))
                } else {
                    for (value in values) {
                        partResult.add(processValue(value))
                    }
                }

                if (partResult.size > 1) {
                    val lastItem = values[partResult.size - 1]

                    if (lastItem == "!important" && rules.contains(current)) {
                        partResult.removeAt(partResult.size - 1)
                        result.add("[${join(partResult, true)}, '!important']")
                    } else {
                        result.add(join(partResult, true))
                    }
                } else {
                    result.add(join(partResult, false))
                }
            }
            return if (result.size > 1) {
                if (containsArrays(result)) {
                    result.joinToString(",\n", "[\n", "\n]")
                } else result.joinToString(", ", "[", "]")
            } else result.joinToString(", ")
        }

        private fun processValue(input: String): String {
            return maybeWrapIntoQuotes(trimQuotes(removeSuffix(input)).replace("'", "\\'"))
        }

        private fun join(input: ArrayList<String>, wrap: Boolean = false, multiline: Boolean = false): String {
            return if (wrap) {
                if (multiline)
                    input.joinToString(",\n", "[\n", "\n]")
                else
                    input.joinToString(", ", "[", "]")
            } else
                input.joinToString(", ")
        }

        private fun containsArrays(input: ArrayList<String>): Boolean {
            for (item in input) {
                if (item.startsWith('[')) {
                    return true
                }
            }
            return false
        }
    }
}
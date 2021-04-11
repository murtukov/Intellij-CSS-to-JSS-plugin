package org.murtukov.css_to_jss

import org.murtukov.css_to_jss.util.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates

class CssConverter {
    private val rules   = arrayOf("margin", "padding")
    private var lines   = HashMap<CssProperty, CssValue>()

    private lateinit var lastProperty: CssProperty

    fun convert(css: String): String {
        val normalizedCss    = css.normalizeWhitespaces().removeCommentBlocks()
        val stoppers         = "{:;}"
        var expectedStoppers = "{:"
        var parenthesisScope = 0
        var buffer           = ""

        parse@for (c in normalizedCss) {
            when (c) {
                '(' -> parenthesisScope++
                ')' -> parenthesisScope--
            }

            if (stoppers.contains(c) && 0 == parenthesisScope) {
                if (!expectedStoppers.contains(c)) {
                    return css // return unchanged
                }

                when (c) {
                    '{' -> expectedStoppers = ":"
                    ':' -> {
                        lastProperty = CssProperty(buffer)
                        expectedStoppers = ";";
                    }
                    ';' -> {
                        lines[lastProperty] = CssValue(buffer, lastProperty)
                        expectedStoppers = ":}"
                    }
                    '}' -> break@parse
                }

                buffer = ""
            } else {
                buffer += c
            }
        }

        // Check, if selection was partial


        return generate()
    }

    private fun generate(): String {
        val stringifiedRules = lines
            .map { "${it.key}: ${it.value}," }
            .joinToString("\n")
            .prependIndent()

        return "{\n$stringifiedRules\n}"
    }

    private fun maybeWrapIntoQuotes(input: String): String {
        return if (input.isNumeric()) input else "'$input'";
    }

    private fun String.split(separator: Char): ArrayList<String> {
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

    private inner class CssProperty(name: String) {
        val cssName = name
        val jssName = when {
            cssName.startsWith('-') -> "'$cssName'"
            else -> cssName.toCamelCase()
        }

        override fun toString() = jssName
    }

    private enum class ValueType {
        SCALAR,                 // 200px
        SPACE_SEPARATED,        // 0 0 20px 30px
        COMMA_SEPARATED,        // "Helvetica Neue", Arial, sams-serif
        COMMA_SPACE_SEPARATED,  // rgba(50, 50, 93, 0.25) 0px 50px 100px -20px, rgba(0, 0, 0, 0.3) 0px 30px 60px -30px
        MIXED                   // italic bold .8em/1.2 Helvetica Neue, Arial, sans-serif
    }

    private inner class CssValue(rawValue: String, private val property: CssProperty) {
        private val parts = rawValue.split(',')
        private var isImportant = false
        private var defaultUnit = "px"
        private val valueType: ValueType = ValueType.SCALAR

        init {
            if (rawValue.contains("!important")) {
                rawValue.removeSuffix("!important")
                isImportant = true
            }
        }

        /**
         * 1. check if !important is set
         * 2. Determine the value type
         */

        override fun toString(): String {
            val result = ArrayList<String>()

            for (part in parts) {
                val normalized = part.normalizeWhitespaces()
                val partResult = ArrayList<String>()
                val values     = normalized.split(' ')

                if ("fontFamily" == property.jssName) {
                    partResult.add(processValue(normalized))
                } else {
                    for (value in values) {
                        partResult.add(processValue(value))
                    }
                }

                if (partResult.size > 1) {
                    if (values.last() == "!important" && rules.contains(property.jssName)) {
                        partResult.removeAt(partResult.size - 1)
                        result.add("[${partResult.join(true)}, '!important']")
                    } else {
                        result.add(partResult.join(true))
                    }
                } else {
                    result.add(partResult.join(false))
                }
            }
            return if (result.size > 1) {
                if (containsArrays(result)) {
                    result.joinToString(",\n", "[\n", "\n]")
                } else result.joinToString(", ", "[", "]")
            } else result.joinToString(", ")
        }

        private fun determineType() {

        }

        private fun processValue(input: String): String {
            return maybeWrapIntoQuotes(
                input.removeSuffix("px")
                    .trimQuotes()
                    .replace("'", "\\'")
            )
        }

        private fun ArrayList<String>.join(wrap: Boolean = false, multiline: Boolean = false): String {
            return when {
                wrap -> {
                    if (multiline)
                        this.joinToString(",\n", "[\n", "\n]")
                    else
                        this.joinToString(", ", "[", "]")
                }
                else -> this.joinToString(", ")
            }
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
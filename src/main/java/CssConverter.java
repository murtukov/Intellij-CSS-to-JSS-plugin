import com.google.common.primitives.Chars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CssConverter {
    private final StringBuilder buffer = new StringBuilder();
    private final StringBuilder result = new StringBuilder();
    private final char[] stoppers      = new char[]{'{', ':', ';', '}'};
    private final String[] rules       = new String[]{"margin", "padding"};
    private String currentRule;

    public String parse(String css) {
        var scope = 0;

        css = removeCommentBlocks(normalizeWhitespaces(css));

        for (final char c : css.toCharArray()) {
            if ('(' == c) {
                scope++;
            } else if (')' == c) {
                scope--;
            }

            if (Chars.contains(stoppers, c) && scope == 0) {
                result.append(convertToken(buffer.toString(), c));
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }

        return result.toString();
    }

    private String convertToken(String token, char stopper) {
        token = token.trim();

        switch (stopper) {
            case '{': // selector
                return "{\n";
            case ':': // rule name
                return "    " + convertRuleName(token) + ":";
            case ';': // rule value
                return convertRuleValue(token);
            case '}': // end of block
                return "}";
        }

        return "";
    }

    private String convertRuleName(String token) {
        if (token.charAt(0) == '-') {
            return String.format("'%s'", token);
        }

        return this.currentRule = toCamelCase(token);
    }

    private String convertRuleValue(String token) {
        return " " + new CssValue(token.trim()) + ",\n";
    }

    /**
     * Converts a kebab-cased string into a camelCased one.
     */
    private String toCamelCase(String input) {
        var temp = Arrays.stream(input.split("-"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
                .collect(Collectors.joining());

        return Character.toLowerCase(temp.charAt(0)) + temp.substring(1);
    }

    private String removeCommentBlocks(String input) {
        while (true) {
            var start = input.indexOf("/*");
            var end = input.indexOf("*/");

            if (-1 != start && -1 != end) {
                input = input.substring(0, start) + input.substring(end + 2);
            } else {
                break;
            }
        }

        return input;
    }

    private String normalizeWhitespaces(String input) {
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Wraps string into single quotes if it's not numeric.
     */
    private String maybeWrapIntoQuotes(String input) {
        if (isNumeric(input)) {
            return input;
        }

        return "'" + input + "'";
    }

    private String removeSuffix(String input) {
        if (input.endsWith("px")) {
            return input.substring(0, input.length() - 2);
        }

        return input;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private String trimQuotes(String input) {
        return input.replaceAll("^'|'$", "")
                    .replaceAll("^\"|\"$", "");
    }

    private ArrayList<String> split(String input, char separator) {
        var buffer = new StringBuilder();
        var parts  = new ArrayList<String>();
        var parenthesis  = 0;
        var singleQuote = false;
        var doubleQuote = false;

        for (final char c : input.toCharArray()) {
            if ('(' == c) {
                parenthesis++;
            } else if (')' == c) {
                parenthesis--;
            }

            if ('\'' == c) {
                singleQuote = !singleQuote;
            }

            if ('"' == c) {
                doubleQuote = !doubleQuote;
            }

            if (separator == c && 0 == parenthesis && !singleQuote && !doubleQuote) {
                parts.add(buffer.toString().trim().replace('\n', ' '));
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }

        if (buffer.length() > 0) {
            parts.add(buffer.toString().trim().replace('\n', ' '));
            buffer.setLength(0);
        }

        return parts;
    }

    private class CssValue {
        private final ArrayList<String> parts;

        CssValue(String input) {
            parts = split(input, ',');
        }

        @Override
        public String toString() {
            var result = new ArrayList<String>();

            for (String part : parts) {
                part = normalizeWhitespaces(part);
                var partResult = new ArrayList<String>();
                var values = split(part, ' ');

                if (currentRule.equals("fontFamily")) {
                    partResult.add(processValue(part));
                } else {
                    for (String value : values) {
                        partResult.add(processValue(value));
                    }
                }

                if (partResult.size() > 1) {
                    var lastItem = values.get(partResult.size() - 1);

                    if (lastItem.equals("!important") && Arrays.asList(rules).contains(currentRule)) {
                        partResult.remove(partResult.size() - 1);
                        result.add("[" + join(partResult, true, false) + ", '!important']");
                    } else {
                        result.add(join(partResult, true, false));
                    }
                } else {
                    result.add(join(partResult, false, false));
                }
            }

            if (result.size() > 1) {
                if (containsArrays(result)) {
                    return join(result, true, true);
                }
                return join(result, true, false);
            }

            return join(result, false, false);
        }

        private String processValue(String input) {
            return maybeWrapIntoQuotes(trimQuotes(removeSuffix(input)).replace("'", "\\'"));
        }

        private String join(ArrayList<String> input, boolean wrap, boolean multiline) {
            if (wrap) {
                if (multiline) {
                    return "[\n" + String.join(",\n", input) + "\n]";
                }

                return "[" + String.join(", ", input) + "]";
            }

            return String.join(", ", input);
        }

        private boolean containsArrays(ArrayList<String> input) {
            for (final var item : input) {
                if (item.charAt(0) == '[') {
                    return true;
                }
            }

            return false;
        }
    }
}

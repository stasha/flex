package com.stasha.info.flexlocalization.formatters;

/**
 * String formatting functionality such as currency, decimal, date, time.
 */
public interface FlexFormat {

    /**
     * Formats a value according to the specified type and locale.
     *
     * @param value The value to format (e.g., Date, Number, String).
     * @param formatType The format type (e.g., "fmt.date", "fmt.currency").
     * @param userFormat Optional custom pattern (e.g., "dd.MM.yy").
     * @param locale The locale tag (e.g., "en-US") or null for default.
     * @return The formatted string or an error message if formatting fails.
     */
    String format(Object value, String formatType, String userFormat, String locale);
}

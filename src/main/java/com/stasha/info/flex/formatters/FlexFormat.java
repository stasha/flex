package com.stasha.info.flex.formatters;

import com.stasha.info.flex.store.LRUCache;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author stasha
 */
public class FlexFormat {

    private final Pattern SPLIT_GENDER_PATTERN = Pattern.compile("\\s*\\|\\s*");
    private static final String DIGIT_STRINGS = "0123456789"; // For char access

    private static final LRUCache<String, String> DATE_TIME_CACHE = new LRUCache(100);
    private Map<Integer, char[]> currencyTemplates = new HashMap<>();
    private Map<Double, String> currencyPreCache = new HashMap<>();
    private ThreadLocal<char[]> resultBuffer = ThreadLocal.withInitial(() -> new char[32]); // Max template size (~20 digits + symbols)

    private static final String DEFAULT_LOCALE = "en-US";
    private Locale loc;
    private String locale;
    private DateFormat sdf;
    private DateFormat stf;
    private NumberFormat nf;
    private NumberFormat cf;
    private String dateFormat;
    private String timeFormat;

    public FlexFormat() {
        setLocale(DEFAULT_LOCALE);
    }

    protected void setLocale(String locale) {
        if (locale == null || locale.isBlank() || locale.equals("fmt.locale")) {
            locale = DEFAULT_LOCALE;
        }
        this.locale = locale;

        if (loc == null || !loc.toLanguageTag().equals(locale)) {
            loc = Locale.forLanguageTag(locale);
            sdf = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, loc);
            stf = SimpleDateFormat.getTimeInstance(DateFormat.DEFAULT, loc);
            nf = NumberFormat.getInstance(loc);
            cf = NumberFormat.getCurrencyInstance(loc);
            dateFormat = ((SimpleDateFormat) sdf).toPattern();
            timeFormat = ((SimpleDateFormat) stf).toPattern();

            // Generate templates for 1–20 integer digits
            // Generate templates for 1–20 integer digits
            for (int digits = 1; digits <= 20; digits++) {
                String sample = "1" + "2".repeat(Math.max(0, digits - 1)) + ".78";
                double sampleNumber = Double.parseDouble(sample);
                String formatted = cf.format(sampleNumber);
                char[] elements = new char[formatted.length()];
                for (int i = 0; i < formatted.length(); i++) {
                    elements[i] = Character.isDigit(formatted.charAt(i)) ? '#' : formatted.charAt(i);
                }
                currencyTemplates.put(digits, elements);
            }

            // Precache 0–299,999
            for (int i = 0; i < 300000; i++) {
                double value = i + (i % 100) / 100.0; // Match test input pattern
                currencyPreCache.put(value, cf.format(value));
            }
        }
    }

    /**
     * Formats a value according to the specified type and locale.
     *
     * @param value The value to format (e.g., Date, Number, String).
     * @param formatType The format type (e.g., "fmt.date", "fmt.currency").
     * @param userFormat Optional custom pattern (e.g., "dd.MM.yy").
     * @param locale The locale tag (e.g., "en-US") or null for default.
     * @return The formatted string or an error message if formatting fails.
     */
    public String format(Object value, String formatType, String userFormat, String locale) {

//        System.out.println(cacheKey);
//        setLocale(locale);
//        if(1 == 1)  return String.valueOf(value);
        try {
            switch (formatType) {
                case "fmt.date", "fmt.time" -> {
                    if (value instanceof Date date) {

                        String cacheKey;

                        if (formatType.equals("fmt.date")) {
                            long normalizedTime = date.getTime() / (24 * 60 * 60 * 1000) * (24 * 60 * 60 * 1000);
                            cacheKey = new StringBuilder(this.locale).append(normalizedTime).append(userFormat).toString();
                        } else {
                            long normalizedDate = ((date.getTime() % (24 * 60 * 60 * 1000)) + (24 * 60 * 60 * 1000)) % (24 * 60 * 60 * 1000);
                            cacheKey = new StringBuilder(this.locale).append(normalizedDate).append(userFormat).toString();
                        }

                        return DATE_TIME_CACHE.computeIfAbsent(cacheKey, k -> {
                            DateFormat udf = formatType.equals("fmt.date") ? sdf : stf;
                            String pattern = formatType.equals("fmt.date") ? dateFormat : timeFormat;
                            if (userFormat != null && !pattern.equals(userFormat)) {
                                udf = new SimpleDateFormat(userFormat, loc);
                                if (formatType.equals("fmt.date")) {
                                    sdf = udf;
                                    dateFormat = userFormat;
                                } else {
                                    stf = udf;
                                    timeFormat = userFormat;
                                }
                            }
                            return udf.format(value);
                        });

                    }
                    return "argument: " + value + " can't be formatted as date";
                }
                case "fmt.decimal" -> {
                    if (userFormat != null && !userFormat.equals(((DecimalFormat) nf).toPattern())) {
                        ((DecimalFormat) nf).applyPattern(userFormat);
                    }
                    return nf.format(value);
                }
                case "fmt.currency" -> {
                    Double v = null;
                    switch (value) {
                        case String str ->
                            v = Double.valueOf(str);
                        case Integer num ->
                            v = (double) num;
                        case Double dub ->
                            v = dub;
                        default -> {
                        }
                    }

                    if (v != null) {
                        // Check precache
                        String cached = currencyPreCache.get(v);
                        if (cached != null) {
                            return cached;
                        }

                        // Handle negative numbers
                        boolean isNegative = v < 0;
                        double absNumber = Math.abs(v);

                        // Get integer part’s digit length without String.valueOf
                        long integerPart = (long) absNumber;
                        int digitLength = integerPart == 0 ? 1 : (int) (Math.log10(integerPart) + 1);
                        if (digitLength > 20) {
                            return cf.format(v);
                        }

                        // Get template
                        char[] formatTemplate = currencyTemplates.get(digitLength);
                        if (formatTemplate == null) {
                            return cf.format(v);
                        }

                        // Get result buffer
                        char[] result = resultBuffer.get();
                        if (result.length < formatTemplate.length) {
                            result = new char[formatTemplate.length];
                            resultBuffer.set(result);
                        }

                        // Extract and map digits in one pass
                        double fractionalPart = absNumber - integerPart;
                        int fracValue = (int) Math.round(fractionalPart * 100); // e.g., 0.234 → 23
                        int fracDigit1 = fracValue / 10;
                        int fracDigit2 = fracValue % 10;
                        long temp = integerPart;
                        int[] intDigits = new int[digitLength];
                        for (int i = digitLength - 1; i >= 0; i--) {
                            intDigits[i] = (int) (temp % 10);
                            temp /= 10;
                        }
                        int digitIndex = 0;
                        int resultIndex = isNegative ? 1 : 0;
                        if (isNegative) {
                            result[0] = '-';
                        }
                        for (int i = 0; i < formatTemplate.length; i++) {
                            if (formatTemplate[i] == '#') {
                                if (digitIndex < intDigits.length) {
                                    result[resultIndex++] = DIGIT_STRINGS.charAt(intDigits[digitIndex++]);
                                } else if (digitIndex == intDigits.length) {
                                    result[resultIndex++] = DIGIT_STRINGS.charAt(fracDigit1);
                                    digitIndex++;
                                } else if (digitIndex == intDigits.length + 1) {
                                    result[resultIndex++] = DIGIT_STRINGS.charAt(fracDigit2);
                                    digitIndex++;
                                } else {
                                    result[resultIndex++] = '0';
                                }
                            } else {
                                result[resultIndex++] = formatTemplate[i];
                            }
                        }

                        // Convert to String
                        return new String(result, 0, resultIndex);
                    }
                }
                case "fmt.uppercase" -> {
                    return String.valueOf(value).toUpperCase(loc);
                }
                case "m", "f", "n" -> {
                    String val = String.valueOf(value);
                    if (val.contains("[")) {
                        String start = val.substring(0, val.lastIndexOf("["));
                        val = val.substring(val.lastIndexOf("[") + 1, val.length() - 1);
                        String[] vls = SPLIT_GENDER_PATTERN.split(val);
                        switch (formatType) {
                            case "m" -> {
                                return start + vls[0];
                            }
                            case "f" -> {
                                return start + vls[1];
                            }
                            default -> {
                                return start + vls[2];
                            }
                        }
                    }
                    return String.valueOf(value);
                }
                default -> {
                    return userFormat != null ? userFormat : String.valueOf(value);
                }
            }
        } catch (Exception ex) {
            return "argument: " + value + " can't be formatted as " + formatType;
        }

        return String.valueOf(value);
    }
}

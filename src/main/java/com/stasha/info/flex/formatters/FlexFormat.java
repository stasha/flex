package com.stasha.info.flex.formatters;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author stasha
 */
public class FlexFormat {
    private Pattern SPLIT_GENDER_PATTERN = Pattern.compile("\\s*\\|\\s*");
    
    private static final String DEFAULT_LOCALE = "en-US";
    private Locale loc;
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

        if (loc == null || !loc.toLanguageTag().equals(locale)) {
            loc = Locale.forLanguageTag(locale);
            sdf = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, loc);
            stf = SimpleDateFormat.getTimeInstance(DateFormat.DEFAULT, loc);
            nf = NumberFormat.getInstance(loc);
            cf = NumberFormat.getCurrencyInstance(loc);
            dateFormat = ((SimpleDateFormat) sdf).toPattern();
            timeFormat = ((SimpleDateFormat) stf).toPattern();
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

        setLocale(locale);

        try {
            switch (formatType) {
                case "fmt.date", "fmt.time" -> {
                    if (value instanceof Date) {
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
                    if (userFormat != null && !userFormat.equals(((DecimalFormat) cf).toPattern())) {
                        ((DecimalFormat) cf).applyPattern(userFormat);
                    }
                    return cf.format(value);
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
    }
}

package com.stasha.info.flexlocalization.formatters;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author stasha
 */
public class FlexFormatImpl implements FlexFormat {

    private static final String DEFAULT_LOCALE = "en-US";
    private Locale loc;
    private DateFormat sdf;
    private DateFormat stf;
    private NumberFormat nf;
    private NumberFormat cf;
    private String dateFormat;
    private String timeFormat;

    public FlexFormatImpl() {
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

    @Override
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
                default -> {
                    return userFormat != null ? userFormat : String.valueOf(value);
                }
            }
        } catch (Exception ex) {
            return "argument: " + value + " can't be formatted as " + formatType;
        }
    }
}

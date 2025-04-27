package com.stasha.info.flex.parsers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author stasha
 */
public class FlexStringTemplateOptions {

    /**
     * Pattern for parsing template options.<br />
     * Plurals are enclosed in square brackets [options delimited by
     * comma].<br />
     * <p>
     * Option pattern example:
     * <ul>
     * <li>[0=no items, 1=1 item, 2=two items, 3-7=few items, other=many
     * items]</li>
     * </ul>
     * </p>
     */
    private static final Pattern TEMPLATE_OPTIONS_PATTERN = Pattern.compile("\\s*((?<!\\\\),)\\s*");

    private static final Pattern PARTIAL_NUMBER_PATTERN = Pattern.compile("^\\s*(~)?(_)?([^-\\s_\\~]+)(_)?(~)?(\\s*-\\s*)?(~)?(_)?([^-\\s_\\~]+)?(_)?(~)?\\s*$");

    private static final Pattern KEY_VALUE_SPLIT_PATTERN = Pattern.compile("\\s*((?<!\\\\)=)\\s*");

    private String[] options;
    private Map<String, String> data;

    public FlexStringTemplateOptions() {
    }

    public FlexStringTemplateOptions(String options) {
        this.data = parse(options);
    }

    protected Map<String, String> parse(String str) {
        if (str != null) {
            // split template options at ","
            // 0=no items, 1=1 item, 2=two items, 3-7=few items, other=many items
            String[] p = TEMPLATE_OPTIONS_PATTERN.split(str);

            this.options = p;

            for (int i = 0; i < p.length; ++i) {
                String[] kv = KEY_VALUE_SPLIT_PATTERN.split(p[i], 2);
                if (data == null) {
                    data = new LinkedHashMap<>(10);
                }
                try {
                    data.put(kv[0], kv[1]);
                } catch (Exception ex) {
                    throw new ParseOptionsException("Failed to parse option", str);
                }
            }
        }
        return data;
    }

    public String getValue(Object value) {
        return this.getValue(value, this.data);
    }

    public String getValue(Object value, String optionsString) {
        return getValue(value, parse(optionsString));
    }

    public String getValue(Object value, Map<String, String> options) {

        String val = String.valueOf(value);
        if (options != null && !options.isEmpty()) {
            String key = match(value, options.keySet().toArray(new String[]{}));
            val = options.get(key);
        }

        // don't unescape it here as this is called recursively
        // with options that can have escape that should be taken
        // into consideration in different interpolation places.
        return val;
    }

    public static String match(Object value, String[] options) {
        String val = String.valueOf(value);
        if(val.startsWith(" ") || val.endsWith(" ")){
            val = val.trim();
        }

        if (options.length > 0) {
            for (int i = 0; i < options.length; ++i) {
                String k = options[i];
                if (k.equals("other")) {
                    return k;
                }

                boolean fromStartsWith = false;
                boolean fromEndsWith = false;
                boolean toStartsWith = false;
                boolean toEndsWith = false;
                boolean fromMathFloor = false;
                boolean fromMathMax = false;
                boolean toMathFlor = false;
                boolean toMathMax = false;

                String from = k;
                String to = null;

                if (k.contains("_") || k.contains("-") || k.contains("~")) {
                    Matcher m = PARTIAL_NUMBER_PATTERN.matcher(k);
                    if (m.find()) {

                        fromMathFloor = "~".equals(m.group(5));
                        fromStartsWith = "_".equals(m.group(4));
                        from = m.group(3);
                        fromEndsWith = "_".equals(m.group(2));
                        fromMathMax = "~".equals(m.group(1));

                        toMathFlor = "~".equals(m.group(7));
                        toStartsWith = ("_").equals(m.group(10));
                        to = m.group(9);
                        toEndsWith = ("_").equals(m.group(8));
                        toMathMax = "~".equals(m.group(11));

                    }
                }

                if (from != null) {
                    // not a range
                    if (to == null) {
                        if (from.equals(val)) {
                            return k;
                        } else if (fromEndsWith && val.endsWith(from)) {
                            return k;
                        } else if (fromStartsWith && val.startsWith(from)) {
                            return k;
                        } else if (fromMathFloor) {
                            if (val.equals(from)) {
                                return k;
                            } else {
                                Double fr = Double.valueOf(from);
                                Double flor = Double.valueOf(val);
                                Double v = flor == 0 ? 0 : (Math.pow(10, Math.floor(Math.log10(flor))) * Math.floor(flor / Math.pow(10, Math.floor(Math.log10(flor)))));
                                if (fr.equals(v)) {
                                    return k;
                                }
                            }
                        }
                    } else {
                        // can't combine _a-b_ and a_-_b
                        // only _a-_b or a_-b_
                        if (fromStartsWith && toEndsWith) {
                            throw new IllegalStateException("Starts with and ends with patterns can't becombined: _a-b_ or a_-_b");
                        }

                        // _11-_15 - ends with 11, 12, 13, 14, 15
                        if (fromEndsWith && toEndsWith && val.length() > from.length()) {
                            val = val.substring(val.length() - from.length(), from.length());
                        }

                        try {
                            Double f = Double.valueOf(from);
                            Double t = Double.valueOf(to);
                            Double dval = !(value instanceof Double) ? Double.valueOf(val) : (Double) value;

                            // check if value is in range
                            if (dval >= f && dval <= t) {
                                return k;
                            }
                        } catch (NumberFormatException ex) {
                            // string can't have range
                            if (from.equals(value)) {
                                return k;
                            } else if (fromEndsWith && String.valueOf(value).endsWith(from)) {
                                return k;
                            } else if (fromStartsWith && String.valueOf(value).startsWith(from)) {
                                return k;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public boolean hasOptions() {
        return this.data != null && !this.data.isEmpty();
    }

    @Override
    public String toString() {
        return Arrays.toString(options);
    }

}

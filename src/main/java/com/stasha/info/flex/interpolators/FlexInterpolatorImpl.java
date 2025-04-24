package com.stasha.info.flex.interpolators;

import com.stasha.info.flex.formatters.FlexFormat;
import com.stasha.info.flex.store.FlexStore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flex Interpolation implementation.
 *
 * @author stasha
 */
public class FlexInterpolatorImpl implements FlexInterpolator {

    private FlexFormat flexFormat;

    @Override
    public void setFlexFormat(FlexFormat format) {
        this.flexFormat = format;
    }

    /**
     * Pattern for parsing localization string containing interpolation,
     * pluralization and formatting markers.<br />
     *
     * Pattern splits interpolation markers into groups: {(index)} = (index) =
     * group 2 {(index)[(pluralization)]} = (pluralization) = group 5
     * {(index)[(pluralization)]}.(format) = (format) = group 9
     * <p>
     * Markers examples:<br />
     * <ul>
     * <li>{index} = {0}, {1}, {2}, {n...}</li>
     * <li>{index [pluralization options]} = {0 [0=no items, 1=1 item, 2=two
     * items, 3-7=few items, other=many items]}</li>
     * <li>{index}.format = {0}.fmt.currency {1}.fmt.decimal
     * {2}.fmt.uppercase</li>
     * </ul>
     * </p>
     */
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("(?<!\\\\)(\\{\\s*(\\d+)\\s*((\\[([^\\]]+)\\])(\\.([^\\}]+))?)?\\})(\\.([^\\s]+))?");

    /**
     * Simple pattern used for replacing interpolation markers (in case there is
     * no pluralization and formatting)
     */
    private static final Pattern SIMPLE_INTERPOLATION_PATTERN = Pattern.compile("\\{(\\d+)\\}");

    /**
     * Pattern for parsing plurals.<br />
     * Plurals are enclosed in square brackets [plural rules delimited by
     * comma].<br />
     * <p>
     * Plural pattern examples:
     * <ul>
     * <li>[0=no items, 1=1 item, 2=two items, 3-7=few items, other=many
     * items]</li>
     * </ul>
     * </p>
     */
    private static final Pattern PLURAL_LOCALIZATION_PATTERN = Pattern.compile("\\s*((?<!\\\\),)\\s*");

    /**
     * Pluralization.
     *
     * @param value
     * @param plurals
     * @param store
     * @return
     */
    @Override
    public String pluralize(Object value, String plurals, FlexStore<String, String> store) {

        // key under which pluralization result will be cached
        String cacheKey = new StringBuilder("plc_").append(value).append("[").append(plurals).append("]").toString();

        // get cached value for plural
        String result = store.getOrDefault(cacheKey, null);

        // if value in cace was not found
        if (result == null) {

            // split pliralization options at ","
            // 0=no items, 1=1 item, 2=two items, 3-7=few items, other=many items
            String[] options = plurals.split(PLURAL_LOCALIZATION_PATTERN.pattern());

            // check if there are some options
            PLURALIZATION:
            if (options.length > 0) {

                // loop over pluralization options
                for (String p : options) {

                    // split option into key/value pairs
                    // 0=no items
                    // k = 0
                    // v = no items
                    String[] kv = p.split("\\s*=\\s*");
                    String k = kv[0];
                    String v = kv[1];

                    switch (k) {
                        case "other" -> {
                            result = v;
                            break PLURALIZATION;
                        }
                        default -> {
                            // range option
                            // 3-7 = few items
                            if (k.contains("-")) {
                                // split range into to-from
                                String[] range = k.split("\\s*-\\s*");

                                // convert range and value into double
                                // to support ranges 1.5-2.8
                                Double from = Double.valueOf(range[0]);
                                Double to = Double.valueOf(range[1]);
                                Double val = !(value instanceof Double) ? Double.valueOf(String.valueOf(value)) : (Double) value;

                                // check if value is in range
                                if (val >= from && val <= to) {
                                    result = v;
                                    break PLURALIZATION;
                                }
                                // exact value matching
                            } else if (k.equals("" + value)) {
                                result = v;
                                break PLURALIZATION;
                            }
                        }
                    }
                }
            }
        }

        // caching plural results
        store.put(cacheKey, result);
        return result;
    }

    /**
     * Retrieves string from store based on passed key and interpolates argument
     * at specified index.<br>
     * Example: Welcome {0}, you are visitor number {1}.
     * interpolate("welcome.msg", 1, "Jon Doe", 1234) will return string:
     * Welcome {0}, you are visitor number 1234.
     *
     * @param key
     * @param index
     * @param store
     * @param arguments
     * @return
     */
    @Override
    public String interpolate(String key, Integer index, FlexStore<String, String> store, Object... arguments) {
        // retrieves string from store
        String str = store.getOrDefault(key, key);

        if (str.indexOf('{') != -1 && str.indexOf('[') == -1 && !str.contains(".fmt")) {
            // Fast path for simple {0}, {1}, etc.
            Matcher m = SIMPLE_INTERPOLATION_PATTERN.matcher(str);
            StringBuffer result = new StringBuffer();
            while (m.find()) {
                int idx = Integer.parseInt(m.group(1));
                String replacement = idx < arguments.length ? String.valueOf(arguments[idx]) : m.group(0);
                m.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(result);
            return result.toString();
        }

        // fast check if string contains markers for interpolation
        if (str.contains("{")) {

            // match localization pattern against string
            Matcher m = LOCALIZATION_PATTERN.matcher(str);

            // check if string contains interpolation markers 
            if (m.find()) {

                // extrac index {0} = 0, {n} = n from matcher
                String idx = m.group(2);

                // if passed index is null than create index from interpolation
                // one
                if (index == null && idx != null) {
                    try {
                        index = Integer.valueOf(idx);
                        index = index > arguments.length ? arguments.length - 1 : index;
                    } catch (NumberFormatException ex) {
                        return "Failed to convert index: " + idx + " to int";
                    }
                }

                // extract plurals from matcher
                String plurals = m.group(5);
                plurals = plurals != null && "".equals(plurals) ? null : plurals;

                // extract formatters from matcher
                String format = m.group(9);
                format = format != null && "".equals(format) ? null : format;

                // extract value at specified index from passed arguments
                Object argAtIndex = arguments[index];

                // check if plurals are present
                if (plurals != null) {
                    // pluralize: artAtIndex = 3
                    // plurals: 0=no items, 1=1 item, 2=two items, 3-7=few items, other=many items
                    // returned value: few items
                    String option = pluralize(argAtIndex, plurals, store);
                    if (option != null) {
                        // returned option could contain interpolation markers {0}
                        // so it's sent to further interpolation.
                        argAtIndex = interpolate(option, index, store, arguments);
                    }
                }

                // check if format is present
                if (format != null) {

                    // determine what type of fomrat to use.
                    // user can specify custom formats for date, time like:
                    // fmt.date.short = dd.MM.yy
                    // fmt.time.hours = HH
                    // ...
                    String formatType = format.replaceAll("(fmt\\.(date|time|decimal|currency)).*", "$1");

                    // find user specified format.
                    // for example user could override currency format
                    // with fmt.currency="${0}.fmt.decimal
                    String userFormat = interpolate(format, index, store, arguments);

                    // get user specified locale
                    // user can specify custom locale in localization file with
                    // fmt.locale=fr-FR
                    String locale = interpolate("fmt.locale", index, store, arguments);

                    // format argument
                    argAtIndex = flexFormat.format(argAtIndex, formatType, userFormat, locale);
                }

                // after input string is interpolated, interpolation marker {n} 
                // is replaced by it's value and then new interpolation is attempted
                // in case string contains additional interpolation markers.
                // This allows arbitrary interpolation marker nesting for more
                // complex localization logic.
                str = interpolate(new StringBuilder(str).replace(m.start(), m.end(), String.valueOf(argAtIndex)).toString(), null, store, arguments);
            }
        }

        // returns fully localized string.
        return str;
    }

}

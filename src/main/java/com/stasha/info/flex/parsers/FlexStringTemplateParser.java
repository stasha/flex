package com.stasha.info.flex.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author stasha
 */
public class FlexStringTemplateParser {

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
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("(?<!\\\\)\\$?(\\{\\s*([^\\[\\}]+)\\s*((\\[([^\\]]*)\\])(\\.([^\\}]+))?)?\\})(\\.([^\\s]+))?");

    /**
     * Simple pattern used for replacing interpolation markers (in case there is
     * no pluralization and formatting)
     */
    private static final Pattern SIMPLE_INTERPOLATION_PATTERN = Pattern.compile("(?<!\\\\)\\{\\s*(\\d+)\\s*\\}");

    private static final Pattern FORMAT_SPLIT_PATTERN = Pattern.compile("&");

    public FlexParsedTemplate parse(String str) {
        if (str != null && str.contains("{")) {
            if (str.contains("{") && !str.contains("[") && !str.contains("}.")) {
                // Fast path for simple {0}, {1}, etc.
                Matcher m = SIMPLE_INTERPOLATION_PATTERN.matcher(str);
                if (m.find()) {
                    String strToReplace = m.group();
                    String idx = m.group(1).trim();
                    return new FlexParsedTemplate(strToReplace, idx, null, null);
                }
            }

            // match localization pattern against string
            Matcher m = LOCALIZATION_PATTERN.matcher(str);

            // check if string contains interpolation markers 
            if (m.find()) {
                String strToReplace = m.group();
                // extrac index {0} = 0, {n} = n from matcher
                String value = m.group(2).trim();

                // extract plurals from matcher
                FlexStringTemplateOptions options = null;
                try {
                    options = new FlexStringTemplateOptions(m.group(5));
                } catch (ParseOptionsException ex) {
                    String g = m.group(1);
                    if (g.startsWith("{") && g.endsWith("}")) {
                        value = g.substring(1, g.length() - 1);
                    }
                }

                // extract formatters from matcher
                String fmt = m.group(9);

                String[] format = null;
                if (fmt != null) {
                    if (fmt.endsWith(".")) {
                        fmt = fmt.substring(0, fmt.length() - 1);
                    }

                    if (fmt != null) {
                        if (fmt.contains("&")) {
                            format = FORMAT_SPLIT_PATTERN.split(fmt);
                        } else {
                            format = new String[]{fmt};
                        }
                    }
                }

                return new FlexParsedTemplate(strToReplace, value, options, format);
            }
        }

        return new FlexParsedTemplate(null, null, null, null);
    }

}

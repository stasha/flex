package com.stasha.info.flex.interpolators;

import com.stasha.info.flex.formatters.FlexFormat;
import com.stasha.info.flex.parsers.FlexParsedTemplate;
import com.stasha.info.flex.parsers.FlexStringTemplateOptions;
import com.stasha.info.flex.parsers.FlexStringTemplateParser;
import com.stasha.info.flex.store.FlexStore;
import com.stasha.info.flex.store.FlexStringStore;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Flex Interpolation implementation.
 *
 * @author stasha
 */
public class FlexInterpolator {

    private FlexFormat flexFormat;
    private FlexStringTemplateParser parser = new FlexStringTemplateParser();
    
    private static final Pattern FORMAT_TYPE_PATTERN = Pattern.compile("(fmt\\.(date|time|decimal|currency)).*");

    private static final Pattern UNESCAPE = Pattern.compile("\\\\");

    /**
     * Sets formatter that will be used for formatting date, time, currency or
     * any other type.
     *
     * @param format
     */
    public void setFlexFormat(FlexFormat format) {
        this.flexFormat = format;
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
    public String interpolate(String key, Integer index, FlexStore<String, String> store, Object... arguments) {
        // retrieves string from store
        String str = (String) store.getOrDefault(key, key);
        FlexStore<String, Object> objectStore = ((FlexStringStore) store).getObjectStore();
        FlexParsedTemplate parsedTpl = (FlexParsedTemplate) objectStore.get(key);

        if (parsedTpl == null) {
            parsedTpl = parser.parse(str);
            objectStore.put(key, parsedTpl);
        }

        String indexValue = parsedTpl.getValue();
        // fast check if string contains markers for interpolation
        if (indexValue != null) {
            indexValue = indexValue.trim();
            Object argAtIndex = indexValue;

            try {
                index = index == null ? Integer.valueOf(indexValue) : index;
                if (index <= arguments.length - 1 && index >= 0) {
                    argAtIndex = arguments.length > 0 ? arguments[index] : null;
                }
            } catch (NumberFormatException ex) {
                argAtIndex = interpolate(indexValue, null, store, arguments);
            }

            // extract value at specified index from passed arguments
            FlexStringTemplateOptions options = parsedTpl.getOptions();
            // check if plurals are present
            if (options != null && options.hasOptions()) {
                // key under which pluralization result will be cached
                String cacheKey = new StringBuilder("plc_").append(argAtIndex).append(options.toString()).toString();

                // get cached value for plural
                String result = (String) store.getOrDefault(cacheKey, null);
                if (result == null) {
                    String option = options.getValue(argAtIndex);
                    argAtIndex = interpolate(option, index, store, arguments);
                    // caching plural results
                    store.put(cacheKey, String.valueOf(argAtIndex));
                } else {
                    argAtIndex = result;
                }
            }

            // check if format is present
            String[] formatters = parsedTpl.getFormats();
            if (formatters != null) {
                for (String format : formatters) {

                    // determine what type of fomrat to use.
                    // user can specify custom formats for date, time like:
                    // fmt.date.short = dd.MM.yy
                    // fmt.time.hours = HH
                    // ...
                    //String formatType = format.replaceAll("(fmt\\.(date|time|decimal|currency)).*", "$1");
                    String formatType = FORMAT_TYPE_PATTERN.matcher(format).replaceAll("$1");
                    List<String> op = ((List<String>) ((FlexStringStore) store).getObject(new StringBuilder(format).append("$options").toString()));
                    if (op != null) {
                        Object obj = null;
                        if (index != null && index <= arguments.length - 1 && index >= 0) {
                            obj = arguments[index];
                        } else {
                            obj = argAtIndex;
                        }
                        final String mformat = FlexStringTemplateOptions.match(obj, op.toArray(new String[]{}));
                        if (mformat != null) {
                            format = new StringBuilder(format).append("[").append(mformat).append("]").toString();
                        }
                    }

                    if (format.startsWith("{") && format.endsWith("}")) {
                        String iv = format.substring(1, format.length() - 1);
                        try {
                            index = Integer.valueOf(iv);
                            formatType = null;
                        } catch (NumberFormatException ex) {
                            //noop
                        }
                    }

                    // find user specified format.
                    // for example user could override currency format
                    // with fmt.currency="${0}.fmt.decimal
                    String userFormat = interpolate(format, index, store, arguments);
                    if(formatType == null){
                        formatType = userFormat;
                    }

                    // get user specified locale
                    // user can specify custom locale in localization file with
                    // fmt.locale=fr-FR
                    String locale = interpolate("fmt.locale", index, store, arguments);

                    // format argument
                    argAtIndex = flexFormat.format(argAtIndex, formatType, userFormat, locale);
                }
            }

            // after input string is interpolated, interpolation marker {n} 
            // is replaced by it's value and then new interpolation is attempted
            // in case string contains additional interpolation markers.
            // This allows arbitrary interpolation marker nesting for more
            // complex localization logic.
//            str = interpolate(new StringBuilder(str).replace(m.start(), m.end(), String.valueOf(argAtIndex)).toString(), null, store, arguments);
            str = interpolate(str.replace(parsedTpl.getStrToReplace(), String.valueOf(argAtIndex)), null, store, arguments);
        }

        // returns fully localized string.
        if (str != null && str.contains("\\")) {
            return UNESCAPE.matcher(str).replaceAll("");
        }
        return str;
    }

}

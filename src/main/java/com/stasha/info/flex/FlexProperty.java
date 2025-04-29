package com.stasha.info.flex;

import com.stasha.info.flex.parsers.FlexParsedTemplate;
import com.stasha.info.flex.parsers.FlexStringTemplateParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author stasha
 */
public class FlexProperty {

    public static final Pattern GENDER_PATTERN = Pattern.compile("^([^\\[]+)?\\s*\\[([^\\|]+)?\\|([^\\|]+)?\\|([^\\]]+)?]$");

    private FlexStringTemplateParser parser = new FlexStringTemplateParser();

    private String key;
    private String name;
    private String index;
    private String value;
    private Map<Object, String> options = new LinkedHashMap<>();
    private List<FlexProperty> properties = new ArrayList<>();
    private String[] formatters;
    private boolean isPartOfCollection = false;

    public FlexProperty() {
    }

    public FlexProperty(String key, String value) {
//        System.out.println(key + " : " + value);
        this.key = key;
        this.value = value;

        if (key != null && key.endsWith("]")) {
            String option = key.substring(0, key.lastIndexOf("["));
            String optionIndex = key.substring(key.lastIndexOf("[") + 1, key.lastIndexOf("]"));

            this.name = option;
            this.index = optionIndex;
            this.value = value;
        }

        if (value != null) {
            if (value.endsWith("]")) {
                Matcher m = GENDER_PATTERN.matcher(value);
                if (m.find()) {
                    String gn = m.group(1);
                    this.options.put("m", gn + m.group(2));
                    this.options.put("f", gn + m.group(3));
                    this.options.put("u", gn + m.group(4));
                    this.isPartOfCollection = true;
                }
            } else if (value.contains("{")) {
                FlexParsedTemplate t = parser.parse(value);
                if (t != null) {
//                    templates.forEach(t -> {
                        FlexProperty prop = new FlexProperty();
                        prop.key = this.key;
                        prop.value = t.getStrToReplace();
                        prop.index = t.getValue();
                        if (t.getOptions() != null && t.getOptions().getData() != null) {
                            prop.options.putAll(t.getOptions().getData());
                        }
                        prop.formatters = t.getFormats();
                        this.properties.add(prop);
//                    });
                }
//                if (template != null && template.getOptions().hasOptions()) {
//                    this.values = new HashMap<>();
//                    this.values.putAll(template.getOptions().getData());
//                }
            }
        }

    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getValue() {
        return value;
    }

    public Map<Object, String> getOptions() {
        return options;
    }

    public List<FlexProperty> getProperties() {
        return properties;
    }

    public String[] getFormatters() {
        return formatters;
    }

    public boolean isPartOfCollection() {
        return this.isPartOfCollection;
    }

    public String getValue(String option) {
        if (this.options != null) {
            return this.options.getOrDefault(option, this.options.getOrDefault("other", value));
        }
        return value;
    }

    @Override
    public String toString() {
        return "FlexProperty{" + "key=" + key + ", name=" + name + ", index=" + index + ", value=" + value + ", options=" + options + ", properties=" + properties + ", formatters=" + Arrays.toString(formatters) + ", isPartOfCollection=" + isPartOfCollection + '}';
    }

}

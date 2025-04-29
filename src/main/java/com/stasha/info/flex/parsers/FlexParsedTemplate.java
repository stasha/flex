package com.stasha.info.flex.parsers;

import java.util.Arrays;

/**
 *
 * @author stasha
 */
public class FlexParsedTemplate {

    private final String value;
    private final String strToReplace;
    private final FlexStringTemplateOptions options;
    private final String[] formats;

    public FlexParsedTemplate(String strToReplace, String value, FlexStringTemplateOptions options, String[] formats) {
        this.strToReplace = strToReplace;
        this.value = value;
        this.options = options;
        this.formats = formats;
    }

    public String getStrToReplace() {
        return strToReplace;
    }

    public String getValue() {
        return this.value;
    }

    public FlexStringTemplateOptions getOptions() {
        return options;
    }

    public String[] getFormats() {
        return formats;
    }
    
    public boolean isSimple() {
        return (options == null || !options.hasOptions()) && (formats == null || formats.length > 0);
    }

    @Override
    public String toString() {
        return "value=" + value + ", options=" + options + ", formats=" + Arrays.toString(formats);
    }

}

package com.stasha.info.flexlocalization.interpolators;

import com.stasha.info.flexlocalization.formatters.FlexFormat;
import com.stasha.info.flexlocalization.store.FlexStore;

/**
 * Contract for interpolating strings.
 *
 * @author stasha
 */
public interface FlexInterpolator {

    /**
     * Sets formatter that will be used for formatting date, time, currency or
     * any other type.
     *
     * @param format
     */
    void setFlexFormat(FlexFormat format);

    /**
     * Pluralizes string based on pluralization rules.
     *
     * @param value Value to use in pluralized string
     * @param plurals Plurals string that will be parsed and than pluralized
     * based on pluralization rules
     * @param store Store in which to search for keys/values in case of
     * recursive lookup
     * @return pluralized string
     */
    String pluralize(Object value, String plurals, FlexStore<String, String> store);

    /**
     * Replaces interpolation {n} markers with values.
     *
     * @param key key associated with which localized string
     * @param index index to interpolate {0}|{1}|{2}...{n}
     * @param store FlexStore where data are stored
     * @param arguments array of values that will replace interpolation markers
     * @return
     */
    String interpolate(String key, Integer index, FlexStore<String, String> store, Object... arguments);
}

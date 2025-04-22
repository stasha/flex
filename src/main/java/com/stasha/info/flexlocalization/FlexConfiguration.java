package com.stasha.info.flexlocalization;

import com.stasha.info.flexlocalization.loaders.FlexDataLoader;
import com.stasha.info.flexlocalization.store.FlexStore;
import com.stasha.info.flexlocalization.store.FlexStringStore;

/**
 * Flexible configuration.
 *
 * @author stasha
 */
public class FlexConfiguration extends Flex {

    /**
     * Listener registered with loader to populate store with system properties.
     */
    private static final FlexDataLoader.FlexDataListener<FlexStringStore> loadSystemProps = store -> loadSystemProps(store);

    /**
     * Method executed when store finishes loading into store.<br>
     * This will add all System properties into the store overriding any
     * existing values.
     *
     * @param store
     */
    private static void loadSystemProps(FlexStringStore store) {
        for (String key : System.getProperties().stringPropertyNames()) {
            store.put(key, System.getProperties().getProperty(key));
        }
    }

    @Override
    public void init() {
        super.init();

        getLoader().addEventListener(loadSystemProps);
    }

    /**
     * Get configuration property value based on passed key and arguments.
     *
     * @param key key associated with the value
     * @param arguments Arguments used in string interpolation. Argument
     * example: welcome {0}. You are visitor number {1}. Arguments {0} and {1}
     * will be replaced with passed arguments to the method: getMessage("Jon
     * Doe", 1234) = welcome Jon Doe. You are visitor number 1234.
     * @return Localized string
     */
    public String getValue(String key, Object... arguments) {
        return getFlexInterpolator().interpolate(key, null, getFlexStore(), arguments);
    }

    /**
     * Get configuration property value based on passed key and arguments.
     *
     * @param <T> type of the return value
     * @param key key associated with the value
     * @param returnType object type to be returned
     * @param arguments arguments used in interpolation
     * @return
     */
    public <T> T getValue(String key, Class<T> returnType, Object... arguments) {
        return (T) getValue(key, getFlexStore(), returnType, arguments);
    }

    /**
     * Get localized message based on passed key and arguments.
     *
     * @param key
     * @param store store to use in localization
     * @param arguments Arguments used in string interpolation. Argument
     * example: welcome {0}. You are visitor number {1}. Arguments {0} and {1}
     * will be replaced with passed arguments to the method: getMessage("Jon
     * Doe", 1234) = welcome Jon Doe. You are visitor number 1234.
     * @return Localized string
     */
    public String getValue(String key, FlexStore<String, String> store, Object... arguments) {
        return getFlexInterpolator().interpolate(key, null, store, arguments);
    }

    /**
     * Get configuration property value based on passed key and arguments.
     *
     * @param <T> type of the return value
     * @param store FlexStore where to search data
     * @param key key associated with the value
     * @param returnType object type to be returned
     * @param arguments arguments used in interpolation
     * @return
     */
    public <T> T getValue(String key, FlexStore<String, String> store, Class<T> returnType, Object... arguments) {
        String value = getFlexInterpolator().interpolate(key, null, store, arguments);

        if (returnType == String.class) {
            return returnType.cast(value);
        } else if (returnType == Integer.class) {
            return returnType.cast(Integer.valueOf(value));
        } else if (returnType == Double.class) {
            return returnType.cast(Double.valueOf(value));
        } else if (returnType == Float.class) {
            return returnType.cast(Float.valueOf(value));
        } else if (returnType == Long.class) {
            return returnType.cast(Long.valueOf(value));
        } else if (returnType == Boolean.class) {
            return returnType.cast(Boolean.valueOf(value));
        } else {
            throw new IllegalArgumentException("Unsupported return type: " + returnType);
        }
    }

}

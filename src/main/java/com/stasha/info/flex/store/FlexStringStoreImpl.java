package com.stasha.info.flex.store;

import com.stasha.info.flex.parsers.FlexParsedTemplate;
import com.stasha.info.flex.parsers.FlexStringTemplateParser;
import java.util.ArrayList;

/**
 * Store for storing string key/value pairs.
 *
 * @author stasha
 */
public class FlexStringStoreImpl extends FlexStoreImpl<String> implements FlexStringStore {

    private FlexStore<String, Object> objectStore = new FlexObjectStore<>();
    private FlexStore<String, Object> cacheStore = new FlexCacheStore<>();
    private FlexStringTemplateParser parser = new FlexStringTemplateParser();

    @Override
    public void put(String key, String value) {
        if (key != null && key.endsWith("]")) {
            String option = key.substring(0, key.lastIndexOf("["));
            String optionValue = key.substring(key.lastIndexOf("[") + 1, key.lastIndexOf("]"));
            String optionKey = new StringBuilder(option).append("$options").toString();
            ArrayList<String> options = (ArrayList<String>) getObjectStore().get(optionKey);
            if (options == null) {
                options = new ArrayList<>();
            }

            options.add(optionValue);
            getObjectStore().put(optionKey, options);
        }
        if (value != null && value.endsWith("]")) {
            FlexParsedTemplate template = parser.parse(value);
            getObjectStore().put(key, template);
        }
        super.put(key, value);
    }

    @Override
    public <T> void setObjectStore(FlexStore<String, T> objectStore) {
        this.objectStore = (FlexStore<String, Object>) objectStore;
    }

    @Override
    public <T> FlexStore<String, T> getObjectStore() {
        return (FlexStore<String, T>) this.objectStore;
    }

    @Override
    public <T> FlexStore<String, T> getCacheStore() {
        return (FlexStore<String, T>) cacheStore;
    }

    public <T> void setCacheStore(FlexStore<String, T> cacheStore) {
        this.cacheStore = (FlexStore<String, Object>) cacheStore;
    }

    @Override
    public String getString(String key) {
        return super.get(key);
    }

    @Override
    public <T> T getObject(String key) {
        return (T) this.objectStore.get(key);
    }

}

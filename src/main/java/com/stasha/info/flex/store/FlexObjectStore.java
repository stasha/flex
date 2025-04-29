package com.stasha.info.flex.store;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author stasha
 * @param <V>
 */
public class FlexObjectStore<V> extends FlexStoreImpl<V> {

    private final Map<String, V> objectStore = new LinkedHashMap<>(10);

    @Override
    protected Map<String, V> getStore() {
        return this.objectStore;
    }

}

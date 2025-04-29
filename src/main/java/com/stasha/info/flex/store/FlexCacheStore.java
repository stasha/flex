package com.stasha.info.flex.store;

import java.util.Map;

/**
 *
 * @author stasha
 * @param <V>
 */
public class FlexCacheStore<V> extends FlexObjectStore<V> {

    private final LRUCache<String, V> cache = new LRUCache(100000);

    @Override
    protected Map<String, V> getStore() {
        return cache;
    }

}

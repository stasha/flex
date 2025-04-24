package com.stasha.info.flex.store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * FlexStore implementation.
 *
 * @author stasha
 * @param
 * @param <V>
 */
public class FlexStoreImpl<V> implements FlexStore<String, V> {

    private final Map<String, V> store = new ConcurrentHashMap<>(500);

    protected Map<String, V> getStore() {
        return store;
    }

    @Override
    public void put(String key, V value) {
        getStore().put(key, value);
    }

    @Override
    public V remove(String key) {
        return getStore().remove(key);
    }

    @Override
    public List<String> getKeys(Predicate<String> key) {
        return getStore().keySet().stream().filter(key::test).collect(Collectors.toList());
    }

    @Override
    public List<V> getValues(Predicate<String> key) {
        return getStore().entrySet().stream().filter(e -> key.test(e.getKey())).map(e -> e.getValue()).collect(Collectors.toList());
    }

    @Override
    public Map<String, V> getSubMap(Predicate<String> key) {
        return getStore().entrySet().stream().filter(m -> key.test(m.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public V get(String key) {
        return getStore().get(key);
    }

    @Override
    public V getOrDefault(String key, V defaultValue) {
        return getStore().getOrDefault(key, defaultValue);
    }

    @Override
    public Object unwrapStore() {
        return getStore();
    }

    @Override
    public void clear() {
        getStore().clear();
    }

}

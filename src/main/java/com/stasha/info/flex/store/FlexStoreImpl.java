package com.stasha.info.flex.store;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<String, V> store = new LinkedHashMap<>(10);

    protected Map<String, V> getStore() {
        return store;
    }

    @Override
    public void put(String key, V value) {
//        System.out.println("put: " + key + "=" + value + ", size=" + store.size());
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
        return getStore().entrySet().stream().filter(m -> key.test(m.getKey())).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, repl) -> existing,
                LinkedHashMap::new
        ));
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

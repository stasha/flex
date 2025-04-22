package com.stasha.info.flexlocalization.store;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Storage containing key=value pairs
 *
 * @param <K> type of key object used for getting value
 * @param <T> type of object stored in storage
 *
 * @author stasha
 */
public interface FlexStore<K, T> {

    /**
     * Associates the specified value with the specified key.<br>
     * Putting new value at same key will replace value.
     *
     * @param key
     * @param value
     */
    void put(K key, T value);

    /**
     * If present, removes the mapping for a key from store.
     *
     * @param key
     * @return
     */
    T remove(K key);

    /**
     * Return keys from store based on passed predicate (filter).<br>
     * This is useful for filtering keys based on specific group.
     *
     * @param key
     * @return
     */
    List<K> getKeys(Predicate<K> key);

    /**
     * Returns values from store based on passed predicate (filter).<br>
     * This is useful for filtering values based on specific key group.
     *
     * @param key
     * @return
     */
    List<T> getValues(Predicate<K> key);

    /**
     * Returns sub-map based on passed predicate (filter).<br>
     *
     * @param key
     * @return
     */
    Map<K, T> getSubMap(Predicate<K> key);

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key
     *
     * @param key
     * @return
     */
    T get(K key);

    /**
     * Return value stored at specified key. If there is no value stored than
     * return default value.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    T getOrDefault(K key, T defaultValue);

    /**
     * Returns original store used for storing data.<br>
     * This can be useful for invoking actions against raw store that are not
     * exposed by FlexStore.
     *
     * @return
     */
    Object unwrapStore();

    /**
     * Empties (clears) whole store.
     */
    void clear();
}

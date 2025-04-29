package com.stasha.info.flex.store;

/**
 * StringStore containing ObjectStore cache for caching localized objects.
 *
 * @author stasha
 */
public interface FlexStringStore extends FlexStore<String, String> {

    <T> void setObjectStore(FlexStore<String, T> stringStore);

    /**
     * /**
     * Returns the store for localization strings (e.g., "money.in.wallet" ->
     * "{0 [0=zero dollars,...]}"). Must return a non-null
     * FlexStore<String, String>.
     *
     * @param <T>
     * @return
     */
    <T> FlexStore<String, T> getObjectStore();
    
    <T> FlexStore<String, T> getCacheStore();

    /**
     * Returns a string from string store.
     *
     * @param key
     * @return
     */
    String getString(String key);

    /**
     * Returns Object from object store.
     *
     * @param <T>
     * @param key
     * @return
     */
    <T> T getObject(String key);

}

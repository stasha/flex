package com.stasha.info.flex.store;

/**
 * Store for storing string key/value pairs.
 *
 * @author stasha
 */
public class FlexStringStoreImpl extends FlexStoreImpl<String> implements FlexStringStore {

    private FlexStore<String, Object> objectStore;

    @Override
    public <T> void setObjectStore(FlexStore<String, T> objectStore) {
        this.objectStore = (FlexStore<String, Object>) objectStore;
    }

    @Override
    public <T> FlexStore<String, T> getObjectStore() {
        return (FlexStore<String, T>) this.objectStore;
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

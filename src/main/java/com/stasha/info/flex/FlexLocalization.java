package com.stasha.info.flex;

import com.stasha.info.flex.store.FlexStore;
import com.stasha.info.flex.store.FlexStoreImpl;
import com.stasha.info.flex.store.FlexStringStore;

/**
 * Flexible localization.
 *
 * @author stasha
 */
public class FlexLocalization extends Flex {

    @Override
    public void setFlexStore(FlexStringStore store) {
        if (store.getObjectStore() == null) {
            store.setObjectStore(new FlexStoreImpl<>());
        }
        super.setFlexStore(store);
    }

    /**
     * Object factory interface used as argument passed to getObject methods.
     *
     * @param <T>
     */
    @FunctionalInterface
    public static interface ObjectFactory<T> {

        T create(String localizedMessage);
    }

    /**
     * Returns object stored in FlexStore based on passed key and arguments.
     *
     * @param <V>
     * @param key
     * @param arguments
     * @return
     */
    public <V> V getObject(String key, Object... arguments) {
        return getObject(key, getFlexStore(), arguments);
    }

    /**
     * Returns object stored in passed FlextStore based on passed key and
     * arguments.
     *
     * @param <V>
     * @param key
     * @param store
     * @param arguments
     * @return
     */
    public <V> V getObject(String key, FlexStore store, Object... arguments) {
        String msg = getMessage(key, arguments);
        return (V) getFlexStore().getObjectStore().get(msg);
    }

    /**
     * Returns object stored in FlextStore based on passed key and arguments. In
     * case object is not found under specified key, than new object is created
     * by passed ObjectFactory and stored into store.
     *
     * @param <V>
     * @param key
     * @param factory
     * @param arguments
     * @return
     */
    public <V> V getObject(String key, ObjectFactory<V> factory, Object... arguments) {
        return getMessage(key, factory, getFlexStore(), arguments);
    }

    /**
     * Returns object stored in passed FlextStore based on passed key and
     * arguments. In case object is not found under specified key, than new
     * object is created by passed ObjectFactory and stored into store.
     *
     * @param <V>
     * @param key
     * @param factory
     * @param store
     * @param arguments
     * @return
     */
    public <V> V getMessage(String key, ObjectFactory<V> factory, FlexStore store, Object... arguments) {
        String msg = getMessage(key, arguments);
        FlexStore objectStore = getFlexStore().getObjectStore();
        if (objectStore.get(msg) == null) {
            V obj = factory.create(msg);
            getFlexStore().getObjectStore().put(msg, obj);
            return obj;
        }
        return (V) getFlexStore().getObjectStore().get(msg);
    }

    /**
     * Get localized message based on passed key and arguments.
     *
     * @param key
     * @param arguments Arguments used in string interpolation. Argument
     * example: welcome {0}. You are visitor number {1}. Arguments {0} and {1}
     * will be replaced with passed arguments to the method: getMessage("Jon
     * Doe", 1234) = welcome Jon Doe. You are visitor number 1234.
     * @return Localized string
     */
    public String getMessage(String key, Object... arguments) {
        return getFlexInterpolator().interpolate(key, null, getFlexStore(), arguments);
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
    public String getMessage(String key, FlexStore<String, String> store, Object... arguments) {
        return getFlexInterpolator().interpolate(key, null, store, arguments);
    }

}

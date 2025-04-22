package com.stasha.info.flexlocalization;

import com.stasha.info.flexlocalization.formatters.FlexFormatImpl;
import com.stasha.info.flexlocalization.interpolators.FlexInterpolator;
import com.stasha.info.flexlocalization.interpolators.FlexInterpolatorImpl;
import com.stasha.info.flexlocalization.loaders.FlexDataLoader;
import com.stasha.info.flexlocalization.loaders.FlexDataLoader.FlexDataListener;
import com.stasha.info.flexlocalization.loaders.FlexDataLoaderImpl;
import com.stasha.info.flexlocalization.store.FlexStringStore;

/**
 * Common class for FlexConfiguration and FlexLocalization.
 *
 * @author stasha
 */
public abstract class Flex {

    /**
     * Loader used for loading data.
     */
    private FlexDataLoader loader = new FlexDataLoaderImpl();

    /**
     * Store where localization/configuration data/object will be saved/read
     * from.
     */
    protected FlexStringStore flexStore;

    /**
     * Interpolator used for interpolating string messages.
     */
    private FlexInterpolator flexInterpolator = new FlexInterpolatorImpl();

    public Flex() {
        init();
    }

    protected FlexDataListener<FlexStringStore> registerStoreListener = store -> setFlexStore(store);

    /**
     * Initialize instance using defaults.
     */
    protected void init() {
        // register listener that will be invoked when store is populated with
        // loaded data
        loader.addEventListener(registerStoreListener);

        // add formatting to interpolator responsible for formating
        // values like date, time, decimals, currency...
        flexInterpolator.setFlexFormat(new FlexFormatImpl());
    }

    /**
     * Returns loader used for loading data.
     *
     * @return
     */
    public FlexDataLoader getLoader() {
        return loader;
    }

    /**
     * Sets custom loader for loading data.
     *
     * @param loader
     */
    public void setLoader(FlexDataLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("loader may not be null");
        }
        if (this.loader != null) {
            this.loader.removeEventListener(registerStoreListener);
        }
        this.loader = loader;
    }

    /**
     * Sets custom FlexStore to be used.
     *
     * @param store
     */
    public void setFlexStore(FlexStringStore store) {
        if (store == null) {
            throw new IllegalArgumentException("store may not be null");
        }
        this.flexStore = store;
    }

    /**
     * Returns used FlexStore.
     *
     * @return
     */
    public FlexStringStore getFlexStore() {
        return this.flexStore;
    }

    /**
     * Sets custom FlexInterpolator functionality.
     *
     * @param flexInterpolator
     */
    public void setFlexInterpolator(FlexInterpolator flexInterpolator) {
        this.flexInterpolator = flexInterpolator;
    }

    /**
     * Returns used FlexInterpolator.
     *
     * @return
     */
    public FlexInterpolator getFlexInterpolator() {
        return this.flexInterpolator;
    }
}

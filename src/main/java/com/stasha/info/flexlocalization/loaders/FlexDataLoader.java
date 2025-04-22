package com.stasha.info.flexlocalization.loaders;

import com.stasha.info.flexlocalization.store.FlexStore;
import com.stasha.info.flexlocalization.parsers.FlexDataParser;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for data loading into FlexStore.
 *
 * @author stasha
 * @param <T>
 */
public interface FlexDataLoader<T> {

    /**
     * Listener executed when loading data is completed.
     * @param <T>
     */
    @FunctionalInterface
    public static interface FlexDataListener<T> {

        /**
         * Executed when data are completely loaded.
         *
         * @param store
         */
        void completed(T store);
    }

    /**
     * Adds event listener to listeners list.
     * 
     * @param listener 
     */
    void addEventListener(FlexDataListener listener);

    /**
     * Removes event listener from listeners list.
     * 
     * @param listener
     * @return 
     */
    boolean removeEventListener(FlexDataListener listener);
    
    /**
     * Execute listener notifications.
     * @param store
     * @return 
     */
    T notifyListeners(T store);

    /**
     * Register new parser for specified file extension.<br>
     * More extensions can register same parser.
     *
     * @param fileExtension extension name for which parser will be used.
     * @param parser
     */
    void registerParser(String fileExtension, FlexDataParser parser);

    /**
     * Returns map of all registered parsers.
     *
     * @return
     */
    Map<String, FlexDataParser> getParsers();

    /**
     * Returns registered parser for provided file extension.<br>
     * If no parser is found than IOException is thrown.
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    FlexDataParser getParser(String fileName) throws IOException;

    /**
     * List of search locations where loader will search for files.
     *
     * @param searchLocations
     */
    void setSearchLocations(List searchLocations);

    /**
     * Returns list of locations where loader will search for files.
     *
     * @return
     */
    List getSearchLocations();

    /**
     * Load empty/defaults.
     *
     * @return
     */
    T load();

    /**
     * Loads files parse them, populates new FlexStore and returns it.
     *
     * @param fileNames
     * @return
     * @throws IOException
     */
    T load(String... fileNames) throws IOException;

    /**
     * Loads provided data into new FlexStore and returns it.
     *
     * @param data
     * @return
     */
    T load(Map<String, String> data);

    /**
     * Loads provided data into passed FlexStore.
     *
     * @param data
     * @param store
     */
    void load(Map<String, String> data, FlexStore store);
}

package com.stasha.info.flex.parsers;

import com.stasha.info.flex.store.FlexStore;
import java.io.IOException;

/**
 * Interface for parsing data in FlexDataLoader.
 *
 * @author stasha
 * @param <T>
 */
@FunctionalInterface
public interface FlexDataParser<T> {

    /**
     * Parses passed object and populates passed store with parsed data.
     *
     * @param obj data to parse
     * @param store store that will be populated with parsed data
     * @throws IOException
     */
    void parse(T obj, FlexStore store) throws IOException;
}

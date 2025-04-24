package com.stasha.info.flex.parsers;

import com.stasha.info.flex.store.FlexStore;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Parser for files with properties extension.
 *
 * @author stasha
 */
public class FlexPropertiesParser implements FlexDataParser<InputStream> {

    @Override
    public void parse(InputStream data, FlexStore store) throws IOException {
        Properties props = new Properties();
        props.load(data);
        props.forEach(store::put);
    }
}

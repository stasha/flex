package com.stasha.info.flex.loaders.parsers;

import com.stasha.info.flex.store.FlexStore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parser for files with properties extension.
 *
 * @author stasha
 */
public class FlexPropertiesParser implements FlexDataParser<InputStream> {

    @Override
    public void parse(InputStream data, FlexStore store) throws IOException {

        Map<String, String> orderedProperties = new LinkedHashMap<>();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(data, "UTF-8"))) {

            for (String line = ""; line != null; line = in.readLine()) {
                String[] props = line.split("=", 2);
                if (props.length < 2) {
                    continue;
                }
                String k = props[0];
                String v = props[1];
//                System.out.println(k + " : " + v);
                orderedProperties.put(k, v);
            }
        }

        orderedProperties.forEach(store::put);
    }
}

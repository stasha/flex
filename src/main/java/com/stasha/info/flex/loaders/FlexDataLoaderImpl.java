package com.stasha.info.flex.loaders;

import com.stasha.info.flex.store.FlexStore;
import com.stasha.info.flex.loaders.parsers.FlexPropertiesParser;
import com.stasha.info.flex.loaders.parsers.FlexDataParser;
import com.stasha.info.flex.store.FlexStringStore;
import com.stasha.info.flex.store.FlexStringStoreImpl;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loader with default functionality for loading data from "properties" files.
 * To load data from other file types corresponding parser needs to be
 * registered using registerParser method.
 *
 * @author stasha
 */
public class FlexDataLoaderImpl implements FlexDataLoader<FlexStringStore> {

    private final Map<String, FlexDataParser> parsers = new HashMap<>();
    private final Set<FlexDataListener> listeners = new LinkedHashSet<>();
    private List searchLocations;

    public FlexDataLoaderImpl() {
        init();
    }

    @Override
    public void addEventListener(FlexDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean removeEventListener(FlexDataListener listener) {
        return listeners.remove(listener);
    }

    public void init() {
        this.searchLocations = new ArrayList<>();
        this.searchLocations.add("classpath:/");
        this.searchLocations.add(new File("."));

        parsers.put("properties", new FlexPropertiesParser());
    }

    @Override
    public void setSearchLocations(List locations) {
        this.searchLocations = locations;
    }

    @Override
    public List getSearchLocations() {
        return this.searchLocations;
    }

    @Override
    public void registerParser(String fileExtension, FlexDataParser parser) {
        this.parsers.put(fileExtension, parser);
    }

    @Override
    public Map<String, FlexDataParser> getParsers() {
        return this.parsers;
    }

    protected String getFileExtension(String fileName) {
        String[] sp = fileName.split("\\.");
        return sp[sp.length - 1];
    }

    @Override
    public FlexDataParser getParser(String fileName) throws IOException {
        String ext = getFileExtension(fileName);
        FlexDataParser parser = this.parsers.get(ext);

        if (parser == null) {
            throw new IOException("No parser found for file " + fileName + " and " + ext + " extension");
        }
        return parser;
    }

    protected InputStream getInpuStream(String name, Object location) throws IOException {
        File f = null;
        switch (location) {

            case String str -> {
                if (str.startsWith("classpath:")) {
                    String file = str.replace("classpath:", "") + name;
                    return this.getClass().getResourceAsStream(file);
                } else {
                    f = new File(str);
                }
            }
            case File file -> {
                f = file;
            }
            case Path path -> {
                f = path.toFile();
            }
            default -> {
                throw new UnsupportedOperationException("Loading from location: " + location + " is not supported");
            }
        }
        return new BufferedInputStream(new FileInputStream(f));
    }

    protected void load(String name, FlexStore store) throws IOException {
        InputStream in = null;
        try {
            for (Object location : searchLocations) {
                try {
                    in = getInpuStream(name, location);
                    break;
                } catch (IOException ex) {
                    //noop  
                }
            }

            if (in == null) {
                throw new IOException("File: " + name + " could not be found in any search location: " + getSearchLocations());
            }

            getParser(name).parse(in, store);

        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Override
    public FlexStringStore notifyListeners(FlexStringStore store) {
        listeners.forEach(l -> l.completed(store));
        return store;
    }

    @Override
    public FlexStringStore load() {
        return notifyListeners(new FlexStringStoreImpl());
    }

    @Override
    public FlexStringStore load(String... fileNames) throws IOException {
        final FlexStringStore fs = new FlexStringStoreImpl();

        for (String name : fileNames) {
            load(name, fs);
        }

        return notifyListeners(fs);
    }

    @Override
    public FlexStringStore load(Map<String, String> data) {
        final FlexStringStore fs = new FlexStringStoreImpl();
        load(data, fs);
        return notifyListeners(fs);
    }

    @Override
    public void load(Map<String, String> data, FlexStore store) {
        if (data != null) {
            data.forEach(store::put);
        }
    }

}

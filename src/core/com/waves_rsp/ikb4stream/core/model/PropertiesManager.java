package com.waves_rsp.ikb4stream.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);
    private static final Map<Class, PropertiesManager> PROPERTIES_MANAGER_HASH_MAP = new HashMap<>();
    private final Properties config = new Properties();

    private PropertiesManager(String path) {
        Path configLocation = Paths.get(path);
        try (InputStream stream = Files.newInputStream(configLocation)) {
            config.load(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static PropertiesManager getInstance(Class clazz, String path) {
        PropertiesManager propertiesManager = PROPERTIES_MANAGER_HASH_MAP.get(clazz);
        if (propertiesManager == null) {
            PropertiesManager pm = new PropertiesManager(path);
            PROPERTIES_MANAGER_HASH_MAP.put(clazz, pm);
            return pm;
        }
        return propertiesManager;
    }

    public String getProperty(String property) {
        String value = config.getProperty(property);
        if (value == null) {
            throw new IllegalArgumentException("Property not found : " + property);
        }
        return value;
    }
}

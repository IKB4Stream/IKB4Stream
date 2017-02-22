package com.waves_rsp.ikb4stream.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton PropertiesManager
 * Load property config
 */
public class PropertiesManager {
    private static final Map<Class, PropertiesManager> PROPERTIES_MANAGER_HASH_MAP = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesManager.class);
    private static final String DEFAULT_PATH = "resources/config.properties";
    private final Properties config = new Properties();

    /**
     * Singleton PropertiesManager
     * @param path Path to config file
     */
    private PropertiesManager(String path) {
        Path configLocation = Paths.get(path);
        try (InputStream stream = Files.newInputStream(configLocation)) {
            config.load(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Get instance for PropertiesManager, one per class
     * @param clazz Class in which PropertiesManager will be instantiate
     * @return An instance of PropertiesManager for {@param clazz}
     */
    public static PropertiesManager getInstance(Class clazz) {
        return getInstance(clazz, DEFAULT_PATH);
    }

    /**
     * Get instance for PropertiesManager, one per class
     * @param clazz Class in which PropertiesManager will be instantiate
     * @param path Path to load configuration for this {@param clazz}
     * @return An instance of PropertiesManager for {@param clazz}
     */
    public static PropertiesManager getInstance(Class clazz, String path) {
        PropertiesManager propertiesManager = PROPERTIES_MANAGER_HASH_MAP.get(clazz);
        if (propertiesManager == null) {
            PropertiesManager pm = new PropertiesManager(path);
            PROPERTIES_MANAGER_HASH_MAP.put(clazz, pm);
            return pm;
        }
        return propertiesManager;
    }

    /**
     * Get property
     * @param property Property to get from configuration file
     * @return Value of property
     * @throws IllegalArgumentException if {@param property} is not set in property file
     */
    public String getProperty(String property) {
        Objects.requireNonNull(property);
        String value = config.getProperty(property);
        if (value == null) {
            throw new IllegalArgumentException("Property not found : " + property);
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Property is empty : " + property);
        }
        return value;
    }
}
